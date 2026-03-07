package id.onyet.app.notifylog.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import id.onyet.app.notifylog.data.local.AppInfo
import id.onyet.app.notifylog.data.preferences.UserPreferences
import id.onyet.app.notifylog.data.repository.NotificationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(
    private val userPreferences: UserPreferences,
    private val repository: NotificationRepository
) : ViewModel() {
    
    // Loading state: becomes true after initial data is loaded
    private val _isLoaded = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isLoaded: kotlinx.coroutines.flow.StateFlow<Boolean> = _isLoaded.asStateFlow()

    val isLoggingEnabled: StateFlow<Boolean> = userPreferences.isLoggingEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    
    val ignoreSystemApps: StateFlow<Boolean> = userPreferences.ignoreSystemApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    val autoDeleteDays: StateFlow<Int> = userPreferences.autoDeleteDays
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 30)
    
    val notificationCount: StateFlow<Int> = repository.notificationCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val isDarkMode: StateFlow<Boolean> = userPreferences.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val saveNotificationImages: StateFlow<Boolean> = userPreferences.saveNotificationImages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val ignoredCustomApps: StateFlow<Set<String>> = userPreferences.ignoredCustomApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val distinctApps: StateFlow<List<AppInfo>> = repository.distinctApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSaveNotificationImages(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setSaveNotificationImages(enabled)
        }
    }

    fun toggleIgnoredApp(packageName: String) {
        viewModelScope.launch {
            val current = ignoredCustomApps.value.toMutableSet()
            if (current.contains(packageName)) {
                current.remove(packageName)
            } else {
                current.add(packageName)
            }
            userPreferences.setIgnoredCustomApps(current)
        }
    }

    // Image storage usage in bytes — refreshed after init and after clearing images
    private val _imageStorageBytes = MutableStateFlow(0L)
    val imageStorageBytes: StateFlow<Long> = _imageStorageBytes.asStateFlow()

    private fun refreshImageStorage() {
        viewModelScope.launch(Dispatchers.IO) {
            _imageStorageBytes.value = repository.getImageStorageBytes()
        }
    }

    fun clearSavedImages() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repository.clearAllImages() }
            refreshImageStorage()
        }
    }

    fun setLoggingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setLoggingEnabled(enabled)
        }
    }

    init {
        // Wait for first emission of key flows and mark loaded
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                userPreferences.isLoggingEnabled,
                userPreferences.ignoreSystemApps,
                repository.notificationCount
            ) { a, b, c -> Triple(a, b, c) }
                .first()
            _isLoaded.value = true
        }
        refreshImageStorage()
    }
    
    fun setIgnoreSystemApps(ignore: Boolean) {
        viewModelScope.launch {
            userPreferences.setIgnoreSystemApps(ignore)
        }
    }
    
    fun setAutoDeleteDays(days: Int) {
        viewModelScope.launch {
            userPreferences.setAutoDeleteDays(days)
        }
    }

    fun setDarkMode(dark: Boolean) {
        viewModelScope.launch {
            userPreferences.setDarkMode(dark)
        }
    }
    
    fun clearAllHistory() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repository.deleteAll() }
            refreshImageStorage()
        }
    }

    suspend fun getAllNotifications(): List<id.onyet.app.notifylog.data.local.NotificationLog> {
        return repository.allNotifications.first()
    }
}

class SettingsViewModelFactory(
    private val userPreferences: UserPreferences,
    private val repository: NotificationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(userPreferences, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
