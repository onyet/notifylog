package id.onyet.app.notifylog.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import id.onyet.app.notifylog.NotifyLogApp
import id.onyet.app.notifylog.R
import id.onyet.app.notifylog.ui.components.LanguageBottomSheet
import id.onyet.app.notifylog.ui.theme.Primary
import id.onyet.app.notifylog.util.LocaleHelper
import id.onyet.app.notifylog.util.NotificationPermissionHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onNavigateToHome: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    
    // Get app preferences for language
    val app = context.applicationContext as NotifyLogApp
    val languageCode by app.userPreferences.languageCode.collectAsState(initial = "en")
    val currentLanguage = LocaleHelper.Language.fromCode(languageCode)
    
    var hasPermission by remember {
        mutableStateOf(NotificationPermissionHelper.hasNotificationListenerPermission(context))
    }
    
    var showWhyNeededDialog by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }
    val languageSheetState = rememberModalBottomSheetState()
    
    // Check permission when returning to the app
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPermission = NotificationPermissionHelper.hasNotificationListenerPermission(context)
                if (hasPermission) {
                    onNavigateToHome()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar with language switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Header
            Text(
                text = stringResource(R.string.setup),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            // Language switcher button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .clickable { showLanguageSheet = true }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = currentLanguage.flag,
                        fontSize = 16.sp
                    )
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = stringResource(R.string.language),
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(0.5f))
        
        // Illustration
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background blur effect
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(
                        color = Primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
                    .blur(60.dp)
            )
            
            // Notification cards illustration
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Top notification (faded)
                NotificationCard(
                    modifier = Modifier
                        .width(180.dp)
                        .align(Alignment.Start)
                        .padding(start = 16.dp),
                    alpha = 0.3f
                )
                
                // Center notification (main)
                Box(
                    modifier = Modifier
                        .width(220.dp)
                        .height(64.dp)
                        .background(
                            color = Primary,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(8.dp)
                                    .background(
                                        color = Color.White.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(8.dp)
                                    .background(
                                        color = Color.White.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                    }
                }
                
                // Bottom notification (faded)
                NotificationCard(
                    modifier = Modifier
                        .width(180.dp)
                        .align(Alignment.End)
                        .padding(end = 16.dp),
                    alpha = 0.15f
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Status indicator
        Box(
            modifier = Modifier
                .background(
                    color = Color.Red.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(50)
                )
                .border(
                    width = 1.dp,
                    color = Color.Red.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(50)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color.Red, CircleShape)
                )
                Text(
                    text = stringResource(R.string.permission_not_granted),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Red,
                    letterSpacing = 0.5.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Title
        Text(
            text = stringResource(R.string.enable_notification_access),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Description
        Text(
            text = stringResource(R.string.onboarding_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Action buttons
        Button(
            onClick = {
                NotificationPermissionHelper.openNotificationListenerSettings(context)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.enable_notification_access),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = { showWhyNeededDialog = true }
        ) {
            Text(
                text = stringResource(R.string.why_needed),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
    
    // Why is this needed dialog
    if (showWhyNeededDialog) {
        AlertDialog(
            onDismissRequest = { showWhyNeededDialog = false },
            title = { 
                Text(
                    text = stringResource(R.string.why_is_this_needed),
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.why_needed_explanation),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Privacy points
                    WhyNeededPoint(
                        title = stringResource(R.string.why_needed_point1_title),
                        description = stringResource(R.string.why_needed_point1_desc)
                    )
                    WhyNeededPoint(
                        title = stringResource(R.string.why_needed_point2_title),
                        description = stringResource(R.string.why_needed_point2_desc)
                    )
                    WhyNeededPoint(
                        title = stringResource(R.string.why_needed_point3_title),
                        description = stringResource(R.string.why_needed_point3_desc)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showWhyNeededDialog = false }) {
                    Text(stringResource(R.string.close), color = Primary)
                }
            }
        )
    }
    
    // Language Bottom Sheet
    if (showLanguageSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLanguageSheet = false },
            sheetState = languageSheetState
        ) {
            LanguageBottomSheet(
                currentLanguage = currentLanguage,
                onLanguageSelected = { language ->
                    scope.launch {
                        app.userPreferences.setLanguageCode(language.code)
                        languageSheetState.hide()
                        showLanguageSheet = false
                    }
                }
            )
        }
    }
}

@Composable
private fun WhyNeededPoint(
    title: String,
    description: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .padding(top = 6.dp)
                .background(Primary, CircleShape)
        )
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun NotificationCard(
    modifier: Modifier = Modifier,
    alpha: Float = 1f
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .background(
                color = Primary.copy(alpha = alpha * 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = Primary.copy(alpha = alpha * 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = Primary.copy(alpha = alpha * 0.4f),
                        shape = CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .background(
                        color = Primary.copy(alpha = alpha * 0.3f),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}
