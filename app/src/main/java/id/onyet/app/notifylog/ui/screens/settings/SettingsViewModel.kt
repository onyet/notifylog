package id.onyet.app.notifylog.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import id.onyet.app.notifylog.data.preferences.UserPreferences
import id.onyet.app.notifylog.data.repository.NotificationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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
    
    fun clearAllHistory() {
        viewModelScope.launch {
            repository.deleteAll()
        }
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
