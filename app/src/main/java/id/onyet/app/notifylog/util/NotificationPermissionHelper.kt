package id.onyet.app.notifylog.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import id.onyet.app.notifylog.service.NotificationLogService

object NotificationPermissionHelper {
    
    fun hasNotificationListenerPermission(context: Context): Boolean {
        val componentName = ComponentName(context, NotificationLogService::class.java)
        val flat = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        return flat != null && flat.contains(componentName.flattenToString())
    }
    
    fun openNotificationListenerSettings(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
