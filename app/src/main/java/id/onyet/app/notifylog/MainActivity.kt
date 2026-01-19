package id.onyet.app.notifylog

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import id.onyet.app.notifylog.R
import id.onyet.app.notifylog.ui.navigation.NotifyLogNavGraph
import id.onyet.app.notifylog.ui.navigation.Screen
import id.onyet.app.notifylog.ui.theme.NotifyLogTheme
import id.onyet.app.notifylog.ui.theme.Primary
import id.onyet.app.notifylog.util.LocaleHelper
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen


val LocalAppLocale = staticCompositionLocalOf { "en" }

// Global state for exit dialog
var showExitDialog by mutableStateOf(false)
    private set


class MainActivity : ComponentActivity() {

    private lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install SplashScreen early to prevent OS blank/white screen
        val splashScreen = installSplashScreen()


        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Optionally keep splash until first frame or some condition
        // splashScreen.setKeepOnScreenCondition { /* return true while loading */ false }
        
        // Handle back button press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Check if we can navigate back or if we're at a root screen
                if (::navController.isInitialized) {
                    val currentRoute = navController.currentDestination?.route

                    // Show exit dialog on root screens
                    if (currentRoute == Screen.Home.route ||
                        currentRoute == Screen.Splash.route ||
                        currentRoute == Screen.Onboarding.route) {
                        showExitDialog = true
                    } else {
                        // Navigate back normally
                        navController.popBackStack()
                    }
                } else {
                    showExitDialog = true
                }
            }
        })

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
                            navController = rememberNavController()

                            NotifyLogNavGraph(navController = navController)

                            // Exit confirmation dialog
                            if (showExitDialog) {
                                ExitConfirmationDialog(
                                    onDismiss = { showExitDialog = false },
                                    onConfirm = {
                                        showExitDialog = false
                                        finishAffinity()
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
        if (newBase != null) {
            val app = newBase.applicationContext as? NotifyLogApp
            if (app != null) {
                // Get the saved language synchronously for attachBaseContext
                val prefs = newBase.getSharedPreferences("user_preferences", MODE_PRIVATE)
                val languageCode = prefs.getString("language_code", "en") ?: "en"
                val localizedContext = LocaleHelper.setLocale(newBase, languageCode)
                super.attachBaseContext(localizedContext)
                return
            }
        }
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
                Text(stringResource(R.string.cancel))
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
