package id.onyet.app.notifylog

import android.app.Application
import id.onyet.app.notifylog.data.local.AppDatabase
import id.onyet.app.notifylog.data.preferences.UserPreferences
import id.onyet.app.notifylog.data.repository.NotificationRepository

class NotifyLogApp : Application() {
    
    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { NotificationRepository(database.notificationDao()) }
    val userPreferences by lazy { UserPreferences(this) }
    
    override fun onCreate() {
        super.onCreate()
    }
}
