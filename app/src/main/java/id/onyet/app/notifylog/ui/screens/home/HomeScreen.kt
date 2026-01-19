package id.onyet.app.notifylog.ui.screens.home

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.ShieldMoon
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import id.onyet.app.notifylog.NotifyLogApp
import id.onyet.app.notifylog.R
import id.onyet.app.notifylog.data.local.NotificationLog
import id.onyet.app.notifylog.ui.components.LanguageBottomSheet
import id.onyet.app.notifylog.ui.theme.Primary
import id.onyet.app.notifylog.util.LocaleHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as NotifyLogApp
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(app.repository)
    )
    val scope = rememberCoroutineScope()

    val notifications by viewModel.notifications.collectAsState()
    val apps by viewModel.apps.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val isFilterSheetVisible by viewModel.isFilterSheetVisible.collectAsState()
    val languageCode by app.userPreferences.languageCode.collectAsState(initial = "en")
    
    // Multi-select state
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val isLoadingInitial by viewModel.isLoadingInitial.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    
    // Delete confirmation dialog state
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    var isLanguageSheetVisible by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val languageSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSelectionMode) {
                        Text(
                            text = "${selectedIds.size} ${stringResource(R.string.selected)}",
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShieldMoon,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = stringResource(R.string.app_name),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (isSelectionMode) {
                        IconButton(onClick = { viewModel.exitSelectionMode() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.cancel)
                            )
                        }
                    }
                },
                actions = {
                    if (isSelectionMode) {
                        // Select all button
                        IconButton(
                            onClick = { viewModel.selectAll(notifications.map { it.id }) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.SelectAll,
                                contentDescription = stringResource(R.string.select_all)
                            )
                        }
                        // Delete selected button
                        IconButton(
                            onClick = { showDeleteConfirmation = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        IconButton(onClick = { isLanguageSheetVisible = true }) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = stringResource(R.string.language)
                            )
                        }
                        // Search/Filter button with badge when filter is active
                        Box {
                            IconButton(onClick = { viewModel.showFilterSheet() }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = stringResource(R.string.search_and_filter)
                                )
                            }
                            // Badge indicator when filter is active
                            if (filterState.isActive) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Primary)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
                )
            )
        },
        bottomBar = {
            BottomBar(onNavigateToSettings = onNavigateToSettings)
        }
    ) { paddingValues ->
        if (isLoadingInitial) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues), contentAlignment = Alignment.Center) {
                androidx.compose.material3.CircularProgressIndicator(color = Primary)
            }
        } else if (notifications.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Header
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    Text(
                        text = stringResource(R.string.notification),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = stringResource(R.string.history),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.privacy_focused_logging),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                EmptyState()
            }
        } else {
            NotificationsList(
                notifications = notifications,
                filterState = filterState,
                apps = apps,
                isSelectionMode = isSelectionMode,
                selectedIds = selectedIds,
                isLoadingInitial = isLoadingInitial,
                isLoadingMore = isLoadingMore,
                onLoadMore = viewModel::loadMore,
                onNotificationClick = { id ->
                    if (isSelectionMode) {
                        viewModel.toggleSelection(id)
                    } else {
                        onNavigateToDetail(id)
                    }
                },
                onNotificationLongClick = { id ->
                    if (!isSelectionMode) {
                        viewModel.enterSelectionMode(id)
                    }
                },
                onSelectedPackageChange = viewModel::updateSelectedPackage,
                onClearFilters = viewModel::clearFilters,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(R.string.delete_confirmation_title)) },
            text = { 
                Text(
                    stringResource(
                        R.string.delete_multiple_confirmation_message,
                        selectedIds.size
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSelectedNotifications()
                        showDeleteConfirmation = false
                    }
                ) {
                    Text(
                        stringResource(R.string.delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
    
    // Filter Bottom Sheet
    if (isFilterSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.hideFilterSheet() },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            FilterBottomSheet(
                filterState = filterState,
                apps = apps,
                onSearchQueryChange = viewModel::updateSearchQuery,
                onPackageSelect = viewModel::updateSelectedPackage,
                onDateRangeChange = viewModel::updateDateRange,
                onClearFilters = viewModel::clearFilters,
                onApply = { viewModel.hideFilterSheet() }
            )
        }
    }

    // Language Bottom Sheet
    if (isLanguageSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { isLanguageSheetVisible = false },
            sheetState = languageSheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            LanguageBottomSheet(
                currentLanguage = LocaleHelper.Language.fromCode(languageCode),
                onLanguageSelected = { language ->
                    scope.launch {
                        app.userPreferences.setLanguageCode(language.code)
                    }
                    isLanguageSheetVisible = false
                    // Recreate activity to apply new locale
                    (context as? androidx.activity.ComponentActivity)?.recreate()
                }
            )
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) Primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) Color.White
            else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NotificationsList(
    notifications: List<NotificationLog>,
    filterState: FilterState,
    apps: List<id.onyet.app.notifylog.data.local.AppInfo>,
    isSelectionMode: Boolean,
    selectedIds: Set<Long>,
    isLoadingInitial: Boolean,
    isLoadingMore: Boolean,
    onLoadMore: () -> Unit,
    prefetchDistance: Int = 15,
    onNotificationClick: (Long) -> Unit,
    onNotificationLongClick: (Long) -> Unit,
    onSelectedPackageChange: (String?) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val groupedNotifications = remember(notifications) {
        notifications.groupBy { notification ->
            val calendar = Calendar.getInstance().apply {
                timeInMillis = notification.receivedTime
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            calendar.timeInMillis
        }
    }
    
    val today = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    
    val yesterday = remember { today - 24 * 60 * 60 * 1000 }
    
    val listState = rememberLazyListState()

    // Prefetch when user is within prefetchDistance from the end
    LaunchedEffect(listState, notifications, isLoadingMore, isLoadingInitial) {
        snapshotFlow { listState.layoutInfo }
            .collectLatest { layout ->
                val total = layout.totalItemsCount
                val lastVisible = layout.visibleItemsInfo.lastOrNull()?.index ?: 0
                if (!isLoadingMore && !isLoadingInitial && total > 0 && total - lastVisible <= prefetchDistance) {
                    onLoadMore()
                }
            }
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(bottom = 80.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // Header (scrolls away)
        item {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Text(
                    text = stringResource(R.string.notification),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = stringResource(R.string.history),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.privacy_focused_logging),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Filter active indicator (scrolls away)
        if (filterState.isActive) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Primary.copy(alpha = 0.1f))
                        .clickable { onClearFilters() }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.filter_active),
                                fontWeight = FontWeight.SemiBold,
                                color = Primary,
                                fontSize = 14.sp
                            )
                            Text(
                                text = stringResource(R.string.clear_filter_hint),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.clear_filter),
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        // Filter chips - STICKY
        stickyHeader {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(vertical = 8.dp)
            ) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        FilterChip(
                            label = stringResource(R.string.all_logs),
                            isSelected = filterState.selectedPackage == null,
                            onClick = { onSelectedPackageChange(null) }
                        )
                    }
                    items(apps) { appInfo ->
                        FilterChip(
                            label = appInfo.appName ?: appInfo.packageName,
                            isSelected = filterState.selectedPackage == appInfo.packageName,
                            onClick = { onSelectedPackageChange(appInfo.packageName) }
                        )
                    }
                }
            }
        }
        
        // Notifications grouped by date
        groupedNotifications.forEach { (dateMillis, notificationsForDate) ->
            item {
                val dateLabel = when {
                    dateMillis >= today -> "Today"
                    dateMillis >= yesterday -> "Yesterday"
                    else -> SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
                        .format(Date(dateMillis))
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dateLabel.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                }
            }
            
            items(notificationsForDate, key = { it.id }) { notification ->
                NotificationItem(
                    notification = notification,
                    isSelectionMode = isSelectionMode,
                    isSelected = selectedIds.contains(notification.id),
                    onClick = { onNotificationClick(notification.id) },
                    onLongClick = { onNotificationLongClick(notification.id) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }

        // Loading more indicator
        item {
            if (isLoadingMore) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator(color = Primary)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NotificationItem(
    notification: NotificationLog,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appIcon = remember(notification.packageName) {
        try {
            context.packageManager.getApplicationIcon(notification.packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                Primary.copy(alpha = 0.15f)
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Checkbox for selection mode
            if (isSelectionMode) {
                Icon(
                    imageVector = if (isSelected) 
                        Icons.Default.CheckCircle 
                    else 
                        Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isSelected) Primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterVertically)
                )
            }
            
            // App icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                if (appIcon != null) {
                    Image(
                        bitmap = appIcon.toBitmap(48, 48).asImageBitmap(),
                        contentDescription = notification.appName,
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    Text(
                        text = (notification.appName?.firstOrNull() ?: "?").toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Primary
                    )
                }
            }
            
            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = (notification.appName ?: notification.packageName).uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = formatTime(notification.receivedTime),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = notification.title ?: "No title",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = notification.content ?: "No content",
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.VerifiedUser,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.no_notifications_yet),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.notifications_will_appear),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun BottomBar(
    onNavigateToSettings: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Primary.copy(alpha = 0.1f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(R.string.local_encryption_active),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                TextButton(
                    onClick = onNavigateToSettings
                ) {
                    Text(
                        text = stringResource(R.string.settings).uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val format = SimpleDateFormat("h:mm a", Locale.getDefault())
    return format.format(Date(timestamp))
}
