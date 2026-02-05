package id.onyet.app.notifylog.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.onyet.app.notifylog.NotifyLogApp
import id.onyet.app.notifylog.R
import id.onyet.app.notifylog.ui.components.LanguageBottomSheet
import id.onyet.app.notifylog.ui.theme.Primary
import id.onyet.app.notifylog.util.LocaleHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val app = context.applicationContext as NotifyLogApp
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(app.userPreferences, app.repository)
    )
    val scope = rememberCoroutineScope()

    val isLoggingEnabled by viewModel.isLoggingEnabled.collectAsState()
    val ignoreSystemApps by viewModel.ignoreSystemApps.collectAsState()
    val autoDeleteDays by viewModel.autoDeleteDays.collectAsState()
    val notificationCount by viewModel.notificationCount.collectAsState()
    val languageCode by app.userPreferences.languageCode.collectAsState(initial = "en")
    val isLoaded by viewModel.isLoaded.collectAsState(initial = false)

    var showClearDialog by remember { mutableStateOf(false) }
    var isLanguageSheetVisible by remember { mutableStateOf(false) }
    var showDeveloperDialog by remember { mutableStateOf(false) }
    var showPrivacyConfirmDialog by remember { mutableStateOf(false) }

    val languageSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
        // Show loading overlay until initial data is loaded
        if (!isLoaded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = stringResource(R.string.loading_settings), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Branding header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = stringResource(R.string.privacy_first_history),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = stringResource(R.string.notifications_stored, notificationCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = Primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            // General Section
            SectionHeader(stringResource(R.string.general))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                // Enable logging
                SettingsToggleItem(
                    title = stringResource(R.string.enable_notification_logging),
                    subtitle = stringResource(R.string.logs_stored_securely),
                    checked = isLoggingEnabled,
                    onCheckedChange = viewModel::setLoggingEnabled,
                    showDivider = true
                )
                
                // Ignore system apps
                SettingsToggleItem(
                    icon = Icons.Default.Block,
                    title = stringResource(R.string.ignore_system_apps),
                    checked = ignoreSystemApps,
                    onCheckedChange = viewModel::setIgnoreSystemApps,
                    showDivider = true
                )

                // Language selection
                SettingsClickableItem(
                    icon = Icons.Default.Language,
                    title = stringResource(R.string.language),
                    subtitle = LocaleHelper.Language.fromCode(languageCode).displayName,
                    onClick = { isLanguageSheetVisible = true }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Storage Section
            SectionHeader(stringResource(R.string.storage_and_data))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                // Auto-delete slider
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.auto_delete_logs),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = stringResource(R.string.after_days, autoDeleteDays),
                            color = Primary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Slider(
                        value = autoDeleteDays.toFloat(),
                        onValueChange = { viewModel.setAutoDeleteDays(it.toInt()) },
                        valueRange = 1f..90f,
                        colors = SliderDefaults.colors(
                            thumbColor = Primary,
                            activeTrackColor = Primary
                        )
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.one_day),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = stringResource(R.string.ninety_days),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
                
                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                )
                
                // Clear history
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showClearDialog = true }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.clear_notification_history),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Button(
                        onClick = { showClearDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(stringResource(R.string.clear), fontWeight = FontWeight.Bold)
                    }
                }

                // Export history
                SettingsClickableItem(
                    icon = Icons.Default.History,
                    title = stringResource(R.string.export_notifications),
                    onClick = {
                        scope.launch {
                            // Fetch all notifications from ViewModel
                            val list = try {
                                viewModel.getAllNotifications()
                            } catch (e: Exception) {
                                emptyList<id.onyet.app.notifylog.data.local.NotificationLog>()
                            }

                            if (list.isEmpty()) {
                                // Show simple toast
                                android.widget.Toast.makeText(context, context.getString(R.string.no_notifications_to_export), android.widget.Toast.LENGTH_SHORT).show()
                                return@launch
                            }

                            try {
                                val file = id.onyet.app.notifylog.util.ExportUtils.writeNotificationsToCsv(context, list)
                                val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

                                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(android.content.Intent.createChooser(intent, null))
                                android.widget.Toast.makeText(context, context.getString(R.string.export_successful), android.widget.Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, context.getString(R.string.export_failed), android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    showDivider = true
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // About Section
            SectionHeader(stringResource(R.string.about))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                // Privacy Policy & Terms of Service
                SettingsClickableItem(
                    icon = Icons.Default.Policy,
                    title = stringResource(R.string.privacy_policy_terms),
                    onClick = {
                        showPrivacyConfirmDialog = true
                    },
                    showDivider = true
                )
                
                // Developer Info (Clickable)
                SettingsClickableItem(
                    icon = Icons.Default.Person,
                    title = stringResource(R.string.developer),
                    onClick = {
                        showDeveloperDialog = true
                    },
                    showDivider = true
                )

                // App Version
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.app_version),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "v1.0.0 (Build 1)",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
            
            // Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = stringResource(R.string.open_source_project),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = stringResource(R.string.github_url),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Primary,
                    modifier = Modifier.clickable {
                        uriHandler.openUri("https://github.com/onyet/notifylog")
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    // Clear history confirmation dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.clear_all_history)) },
            text = {
                Text(stringResource(R.string.clear_all_history_message, notificationCount))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.clear_all))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
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

    // Developer Info Dialog
    if (showDeveloperDialog) {
        AlertDialog(
            onDismissRequest = { showDeveloperDialog = false },
            title = { Text(stringResource(R.string.developer)) },
            text = {
                        Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.developer_description),
                            fontSize = 14.sp
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "onyetcorp@gmail.com",
                                fontSize = 14.sp
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "+6282221874400",
                                fontSize = 14.sp
                            )
                        }
                    }
            },
            confirmButton = {
                TextButton(onClick = { showDeveloperDialog = false }) {
                    Text(stringResource(R.string.close), color = Primary)
                }
            }
        )
    }

    // Privacy Policy Confirmation Dialog
    if (showPrivacyConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyConfirmDialog = false },
            title = { Text(stringResource(R.string.privacy_policy_terms)) },
            text = { Text(stringResource(R.string.open_privacy_policy_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showPrivacyConfirmDialog = false
                    uriHandler.openUri("https://onyet.github.io/privacy-police.html")
                }) {
                    Text(stringResource(R.string.open), color = Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPrivacyConfirmDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
  }
}

