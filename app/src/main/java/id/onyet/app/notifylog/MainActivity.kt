package id.onyet.app.notifylog

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.rememberNavController
import id.onyet.app.notifylog.ui.navigation.NotifyLogNavGraph
import id.onyet.app.notifylog.ui.navigation.Screen
import id.onyet.app.notifylog.ui.theme.NotifyLogTheme
import id.onyet.app.notifylog.ui.theme.Primary
import id.onyet.app.notifylog.util.LocaleHelper

val LocalAppLocale = staticCompositionLocalOf { "en" }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            val app = application as NotifyLogApp
            val isDarkMode by app.userPreferences.isDarkMode.collectAsState(initial = true)
            val languageCode by app.userPreferences.languageCode.collectAsState(initial = "en")

            // Apply locale
            val localizedContext = LocaleHelper.setLocale(LocalContext.current, languageCode)

            // Determine layout direction based on language
            val layoutDirection = if (LocaleHelper.isRtl(languageCode)) {
                LayoutDirection.Rtl
            } else {
                LayoutDirection.Ltr
            }

            CompositionLocalProvider(
                LocalAppLocale provides languageCode,
                LocalLayoutDirection provides layoutDirection
            ) {
                NotifyLogTheme(darkTheme = isDarkMode) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        ProvideLocalizedContext(localizedContext) {
                            val navController = rememberNavController()
                            var showExitDialog by remember { mutableStateOf(false) }

                            // Handle back press on home screen
                            BackHandler(
                                enabled = navController.currentBackStackEntry?.destination?.route == Screen.Home.route
                            ) {
                                showExitDialog = true
                            }

                            NotifyLogNavGraph(navController = navController)

                            // Exit confirmation dialog
                            if (showExitDialog) {
                                ExitConfirmationDialog(
                                    onDismiss = { showExitDialog = false },
                                    onConfirm = {
                                        showExitDialog = false
                                        moveTaskToBack(true)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
    }
}

@Composable
fun ExitConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.exit_app)) },
        text = { Text(stringResource(R.string.exit_app_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.exit), color = Primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.stay))
            }
        }
    )
}

@Composable
fun ProvideLocalizedContext(
    context: Context,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalContext provides context,
        content = content
    )
}
