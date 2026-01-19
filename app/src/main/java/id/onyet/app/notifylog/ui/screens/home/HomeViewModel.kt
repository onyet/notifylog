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
import kotlinx.coroutines.flow.debounce
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
    
    // Notifications exposed as a mutable state flow to support paging (infinite loading)
    private val _notifications = MutableStateFlow<List<NotificationLog>>(emptyList())
    val notifications: StateFlow<List<NotificationLog>> = _notifications.asStateFlow()

    private val _isLoadingInitial = MutableStateFlow(true)
    val isLoadingInitial: StateFlow<Boolean> = _isLoadingInitial.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val pageSize = 50
    private var currentOffset = 0
    private var endReached = false

    // For filtered paging
    private var filteredOffset = 0
    private var filteredEndReached = false

    // Debounced search input
    private val _rawSearchQuery = kotlinx.coroutines.flow.MutableStateFlow("")

    init {
        // Debounce raw search input and update filter state
        viewModelScope.launch {
            _rawSearchQuery
                .debounce(300)
                .collect { value ->
                    _filterState.value = _filterState.value.copy(searchQuery = value)
                }
        }

        // React to filter changes (initial load per mode)
        viewModelScope.launch {
            _filterState.collect { filter ->
                // Reset paging
                currentOffset = 0
                endReached = false
                filteredOffset = 0
                filteredEndReached = false
                _notifications.value = emptyList()

                if (!filter.isActive) {
                    // Load initial page (paged)
                    _isLoadingInitial.value = true
                    val page = repository.getPage(pageSize, currentOffset)
                    _notifications.value = page
                    currentOffset += page.size
                    endReached = page.size < pageSize
                    _isLoadingInitial.value = false
                } else {
                    // Load first filtered page (paged)
                    _isLoadingInitial.value = true
                    val page = repository.getFilteredPage(
                        query = filter.searchQuery.ifBlank { "%" },
                        packageName = filter.selectedPackage,
                        startDate = filter.startDate,
                        endDate = filter.endDate,
                        limit = pageSize,
                        offset = 0
                    )
                    _notifications.value = page
                    filteredOffset = page.size
                    filteredEndReached = page.size < pageSize
                    _isLoadingInitial.value = false
                }
            }
        }

        // Listen for new notifications to refresh when no filter is active
        viewModelScope.launch {
            repository.allNotifications.collect {
                if (!_filterState.value.isActive) {
                    currentOffset = 0
                    val firstPage = repository.getPage(pageSize, 0)
                    _notifications.value = firstPage
                    currentOffset = firstPage.size
                    endReached = firstPage.size < pageSize
                }
            }
        }
    }

    fun loadMore() {
        if (_isLoadingMore.value || _isLoadingInitial.value) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            if (_filterState.value.isActive) {
                if (filteredEndReached) {
                    _isLoadingMore.value = false
                    return@launch
                }
                val filter = _filterState.value
                val page = repository.getFilteredPage(
                    query = filter.searchQuery.ifBlank { "%" },
                    packageName = filter.selectedPackage,
                    startDate = filter.startDate,
                    endDate = filter.endDate,
                    limit = pageSize,
                    offset = filteredOffset
                )
                if (page.isNotEmpty()) {
                    _notifications.value = _notifications.value + page
                    filteredOffset += page.size
                    if (page.size < pageSize) filteredEndReached = true
                } else {
                    filteredEndReached = true
                }
            } else {
                if (endReached) {
                    _isLoadingMore.value = false
                    return@launch
                }
                val page = repository.getPage(pageSize, currentOffset)
                if (page.isNotEmpty()) {
                    _notifications.value = _notifications.value + page
                    currentOffset += page.size
                    if (page.size < pageSize) endReached = true
                } else {
                    endReached = true
                }
            }
            _isLoadingMore.value = false
        }
    }
    
    fun updateSearchQuery(query: String) {
        // Debounced: update the raw flow, actual filter state will be updated after debounce
        _rawSearchQuery.value = query
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
