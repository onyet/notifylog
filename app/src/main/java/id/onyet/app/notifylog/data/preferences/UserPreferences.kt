package id.onyet.app.notifylog.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferences(private val context: Context) {
    
    companion object {
        private val LOGGING_ENABLED = booleanPreferencesKey("logging_enabled")
        private val IGNORE_SYSTEM_APPS = booleanPreferencesKey("ignore_system_apps")
        private val AUTO_DELETE_DAYS = intPreferencesKey("auto_delete_days")
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val DARK_MODE = booleanPreferencesKey("dark_mode")
        private val LANGUAGE_CODE = stringPreferencesKey("language_code")
        private val SAVE_NOTIFICATION_IMAGES = booleanPreferencesKey("save_notification_images")
        private val IGNORED_CUSTOM_APPS = stringSetPreferencesKey("ignored_custom_apps")
    }
    
    val isLoggingEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[LOGGING_ENABLED] ?: true
    }
    
    val ignoreSystemApps: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IGNORE_SYSTEM_APPS] ?: false
    }
    
    val autoDeleteDays: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[AUTO_DELETE_DAYS] ?: 30
    }
    
    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED] ?: false
    }
    
    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DARK_MODE] ?: true
    }
    
    val languageCode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LANGUAGE_CODE] ?: "en"
    }

    /** Whether to capture and save image previews from notifications. Default true. */
    val saveNotificationImages: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SAVE_NOTIFICATION_IMAGES] ?: true
    }

    /** Set of package names the user chose to ignore. Default empty. */
    val ignoredCustomApps: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[IGNORED_CUSTOM_APPS] ?: emptySet()
    }

    suspend fun setLoggingEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[LOGGING_ENABLED] = enabled
        }
    }
    
    suspend fun setIgnoreSystemApps(ignore: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IGNORE_SYSTEM_APPS] = ignore
        }
    }
    
    suspend fun setAutoDeleteDays(days: Int) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_DELETE_DAYS] = days
        }
    }
    
    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
        }
    }
    
    suspend fun setDarkMode(darkMode: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE] = darkMode
        }
    }

    suspend fun setSaveNotificationImages(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SAVE_NOTIFICATION_IMAGES] = enabled
        }
    }

    suspend fun setIgnoredCustomApps(packageNames: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[IGNORED_CUSTOM_APPS] = packageNames
        }
    }

    suspend fun setLanguageCode(code: String) {
        // Also save to SharedPreferences synchronously for attachBaseContext
        context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
            .edit()
            .putString("language_code", code)
            .apply()

        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_CODE] = code
        }
    }
}
