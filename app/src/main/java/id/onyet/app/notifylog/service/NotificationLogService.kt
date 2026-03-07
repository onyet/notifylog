package id.onyet.app.notifylog.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import id.onyet.app.notifylog.MainActivity
import id.onyet.app.notifylog.NotifyLogApp
import id.onyet.app.notifylog.R
import id.onyet.app.notifylog.data.local.NotificationLog
import id.onyet.app.notifylog.util.NotificationImageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NotificationLogService : NotificationListenerService() {
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // De-duplication cache: stores hash of recent notifications
    // Key = hash of (packageName + title + content), Value = timestamp
    private val recentNotifications = mutableMapOf<String, Long>()
    private val deduplicationWindowMs = 3000L // 3 seconds window
    
    companion object {
        private const val CHANNEL_ID = "notifylog_service_channel"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForegroundServiceNotification()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "NotifyLog Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Keeps NotifyLog running to capture notifications"
            setShowBadge(false)
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun startForegroundServiceNotification() {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.privacy_focused_logging))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        serviceScope.launch {
            try {
                val app = applicationContext as NotifyLogApp
                
                // Check if logging is enabled
                val isLoggingEnabled = app.userPreferences.isLoggingEnabled.first()
                if (!isLoggingEnabled) return@launch
                
                // Check if we should ignore system apps
                val ignoreSystemApps = app.userPreferences.ignoreSystemApps.first()
                if (ignoreSystemApps && isSystemApp(sbn.packageName)) return@launch
                
                // Skip our own notifications
                if (sbn.packageName == packageName) return@launch
                
                val extras = sbn.notification.extras
                
                val title = extras.getString(Notification.EXTRA_TITLE)
                    ?: extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
                
                val content = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
                    ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
                
                // Skip notifications without meaningful content
                if (title.isNullOrBlank() && content.isNullOrBlank()) return@launch
                
                // De-duplication check
                val notificationHash = generateNotificationHash(sbn.packageName, title, content)
                val currentTime = System.currentTimeMillis()
                
                synchronized(recentNotifications) {
                    // Clean up old entries
                    recentNotifications.entries.removeIf { 
                        currentTime - it.value > deduplicationWindowMs 
                    }
                    
                    // Check if this notification was recently logged
                    val lastSeen = recentNotifications[notificationHash]
                    if (lastSeen != null && currentTime - lastSeen < deduplicationWindowMs) {
                        // Duplicate notification, skip it
                        return@launch
                    }
                    
                    // Record this notification
                    recentNotifications[notificationHash] = currentTime
                }
                
                // Extract and save image preview only if user has enabled it
                val saveImages = app.userPreferences.saveNotificationImages.first()
                val imagePath = if (saveImages) {
                    extractAndSaveNotificationImage(
                        extras = extras,
                        filename = "notif_${sbn.postTime}_${sbn.packageName.hashCode()}"
                    )
                } else null

                val log = NotificationLog(
                    packageName = sbn.packageName,
                    appName = getAppName(sbn.packageName),
                    title = title,
                    content = content,
                    postedTime = sbn.postTime,
                    receivedTime = System.currentTimeMillis(),
                    isCleared = 0,
                    imagePath = imagePath
                )
                
                app.repository.insert(log)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        serviceScope.launch {
            try {
                val app = applicationContext as NotifyLogApp
                app.repository.markAsCleared(sbn.packageName, sbn.postTime)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun getAppName(packageName: String): String {
        return try {
            val packageManager = applicationContext.packageManager
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }
    
    private fun isSystemApp(packageName: String): Boolean {
        return try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
    
    /**
     * Generate a hash for deduplication based on package, title, and content.
     * Notifications with same hash within the deduplication window are considered duplicates.
     */
    private fun generateNotificationHash(packageName: String, title: String?, content: String?): String {
        return "${packageName}_${title.orEmpty()}_${content.orEmpty()}".hashCode().toString()
    }

    /**
     * Extracts an image bitmap from notification extras and saves it to internal storage.
     *
     * Priority order:
     *  1. EXTRA_PICTURE       — BigPictureStyle image (e.g. WhatsApp image message)
     *  2. EXTRA_LARGE_ICON_BIG — large icon in expanded view
     *  3. EXTRA_LARGE_ICON    — standard large icon (profile picture, etc.)
     *
     * Returns the saved file path, or null if no image was found or saving failed.
     */
    private fun extractAndSaveNotificationImage(extras: Bundle, filename: String): String? {
        val bitmap = extractBigPicture(extras)
            ?: extractLargeIcon(extras, Notification.EXTRA_LARGE_ICON_BIG)
            ?: extractLargeIcon(extras, Notification.EXTRA_LARGE_ICON)
            ?: return null

        return NotificationImageManager.saveBitmap(applicationContext, bitmap, filename)
    }

    /**
     * Extracts the BigPictureStyle image from EXTRA_PICTURE.
     * API 31+: stored as [Icon]; older: stored directly as [Bitmap].
     */
    @Suppress("DEPRECATION")
    private fun extractBigPicture(extras: Bundle): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // API 31+: EXTRA_PICTURE is an Icon
                (extras.getParcelable(Notification.EXTRA_PICTURE) as? Icon)
                    ?.loadDrawable(applicationContext)?.toBitmapOrNull()
            } else {
                // Below API 31: EXTRA_PICTURE is a Bitmap
                extras.getParcelable(Notification.EXTRA_PICTURE) as? Bitmap
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Extracts a large icon from the given [extraKey] (EXTRA_LARGE_ICON or EXTRA_LARGE_ICON_BIG).
     * API 23+: stored as [Icon]; older: stored as [Bitmap].
     */
    @Suppress("DEPRECATION")
    private fun extractLargeIcon(extras: Bundle, extraKey: String): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // API 23+: large icon is an Icon
                (extras.getParcelable(extraKey) as? Icon)
                    ?.loadDrawable(applicationContext)?.toBitmapOrNull()
            } else {
                // Below API 23: large icon is a Bitmap
                extras.getParcelable(extraKey) as? Bitmap
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Converts an [android.graphics.drawable.Drawable] to a [Bitmap].
     * Returns null if the conversion fails or the drawable has no intrinsic size.
     */
    private fun android.graphics.drawable.Drawable.toBitmapOrNull(): Bitmap? {
        if (this is BitmapDrawable) return this.bitmap
        val w = intrinsicWidth.takeIf { it > 0 } ?: return null
        val h = intrinsicHeight.takeIf { it > 0 } ?: return null
        return try {
            val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            setBounds(0, 0, canvas.width, canvas.height)
            draw(canvas)
            bmp
        } catch (e: Exception) {
            null
        }
    }
}
