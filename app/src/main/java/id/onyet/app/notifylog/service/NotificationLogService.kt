package id.onyet.app.notifylog.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import id.onyet.app.notifylog.MainActivity
import id.onyet.app.notifylog.NotifyLogApp
import id.onyet.app.notifylog.R
import id.onyet.app.notifylog.data.local.NotificationLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NotificationLogService : NotificationListenerService() {
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
            .setSmallIcon(R.drawable.ic_notification)
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
                
                val log = NotificationLog(
                    packageName = sbn.packageName,
                    appName = getAppName(sbn.packageName),
                    title = title,
                    content = content,
                    postedTime = sbn.postTime,
                    receivedTime = System.currentTimeMillis(),
                    isCleared = 0
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
}
