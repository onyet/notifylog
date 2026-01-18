package id.onyet.app.notifylog.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import id.onyet.app.notifylog.data.local.AppInfo
import id.onyet.app.notifylog.data.local.NotificationLog
import id.onyet.app.notifylog.data.repository.NotificationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FilterState(
    val searchQuery: String = "",
    val selectedPackage: String? = null,
    val startDate: Long? = null,
    val endDate: Long? = null
) {
    val isActive: Boolean
        get() = searchQuery.isNotBlank() || selectedPackage != null || startDate != null || endDate != null
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val repository: NotificationRepository
) : ViewModel() {
    
    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()
    
    private val _isFilterSheetVisible = MutableStateFlow(false)
    val isFilterSheetVisible: StateFlow<Boolean> = _isFilterSheetVisible.asStateFlow()
    
    // Multi-select mode state
    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()
    
    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()
    
    val apps: StateFlow<List<AppInfo>> = repository.distinctApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val notifications: StateFlow<List<NotificationLog>> = _filterState
        .flatMapLatest { filter ->
            if (filter.searchQuery.isBlank() && filter.selectedPackage == null && filter.startDate == null) {
                repository.allNotifications
            } else {
                repository.searchWithFilters(
                    query = filter.searchQuery.ifBlank { "%" },
                    packageName = filter.selectedPackage,
                    startDate = filter.startDate,
                    endDate = filter.endDate
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    fun updateSearchQuery(query: String) {
        _filterState.value = _filterState.value.copy(searchQuery = query)
    }
    
    fun updateSelectedPackage(packageName: String?) {
        _filterState.value = _filterState.value.copy(selectedPackage = packageName)
    }
    
    fun updateDateRange(startDate: Long?, endDate: Long?) {
        _filterState.value = _filterState.value.copy(startDate = startDate, endDate = endDate)
    }
    
    fun clearFilters() {
        _filterState.value = FilterState()
    }
    
    fun showFilterSheet() {
        _isFilterSheetVisible.value = true
    }
    
    fun hideFilterSheet() {
        _isFilterSheetVisible.value = false
    }
    
    fun deleteNotification(id: Long) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }
    
    // Multi-select functions
    fun enterSelectionMode(initialId: Long) {
        _isSelectionMode.value = true
        _selectedIds.value = setOf(initialId)
    }
    
    fun exitSelectionMode() {
        _isSelectionMode.value = false
        _selectedIds.value = emptySet()
    }
    
    fun toggleSelection(id: Long) {
        _selectedIds.value = if (_selectedIds.value.contains(id)) {
            _selectedIds.value - id
        } else {
            _selectedIds.value + id
        }
        // Exit selection mode if no items selected
        if (_selectedIds.value.isEmpty()) {
            _isSelectionMode.value = false
        }
    }
    
    fun selectAll(ids: List<Long>) {
        _selectedIds.value = ids.toSet()
    }
    
    fun deleteSelectedNotifications() {
        viewModelScope.launch {
            _selectedIds.value.forEach { id ->
                repository.delete(id)
            }
            exitSelectionMode()
        }
    }
}

class HomeViewModelFactory(
    private val repository: NotificationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
