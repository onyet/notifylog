package id.onyet.app.notifylog

import android.app.Application
import id.onyet.app.notifylog.data.local.AppDatabase
import id.onyet.app.notifylog.data.preferences.UserPreferences
import id.onyet.app.notifylog.data.repository.NotificationRepository
import id.onyet.app.notifylog.util.LocaleHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NotifyLogApp : Application() {
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { NotificationRepository(database.notificationDao()) }
    val userPreferences by lazy { UserPreferences(this) }
    
    override fun onCreate() {
        super.onCreate()
        // Apply saved locale on app start
        val prefs = getSharedPreferences("user_preferences", MODE_PRIVATE)
        val languageCode = prefs.getString("language_code", "en") ?: "en"
        LocaleHelper.setLocale(this, languageCode)
        
        // Pre-warm database in background for faster first query
        applicationScope.launch {
            try {
                database.notificationDao()
            } catch (_: Exception) {
                // Ignore initialization errors
            }
        }
    }
}
