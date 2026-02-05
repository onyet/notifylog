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
import android.content.Intent


val LocalAppLocale = staticCompositionLocalOf { "en" }

// Global state for exit dialog
var showExitDialog by mutableStateOf(false)
    private set


class MainActivity : ComponentActivity() {

    private lateinit var navController: NavHostController
    
    // Cache language preference synchronously for faster startup
    private val initialLanguage: String by lazy {
        getSharedPreferences("user_preferences", MODE_PRIVATE)
            .getString("language_code", "en") ?: "en"
    }
    
    private val initialDarkMode: Boolean by lazy {
        getSharedPreferences("user_preferences", MODE_PRIVATE)
            .getBoolean("dark_mode", true)
    }

    private lateinit var appUpdateManager: com.google.android.play.core.appupdate.AppUpdateManager
    private val installStateListener = com.google.android.play.core.install.InstallStateUpdatedListener { state ->
        if (state.installStatus() == com.google.android.play.core.install.model.InstallStatus.DOWNLOADED) {
            // For flexible updates, prompt user to complete; here we complete immediately
            appUpdateManager.completeUpdate()
        }
    }

    companion object {
        private const val REQUEST_CODE_UPDATE = 2001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize AppUpdateManager
        appUpdateManager = com.google.android.play.core.appupdate.AppUpdateManagerFactory.create(this)

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
            // Use cached initial values for faster first render, then observe changes
            val isDarkMode by app.userPreferences.isDarkMode.collectAsState(initial = initialDarkMode)
            val languageCode by app.userPreferences.languageCode.collectAsState(initial = initialLanguage)

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

        // Handle update intent if activity launched from notification
        checkAndStartUpdateFromIntent(intent)
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

    override fun onResume() {
        super.onResume()
        // Register install listener for flexible updates
        // Guard against SecurityException on some platforms / older Play Core libs
        try {
            appUpdateManager.registerListener(installStateListener)
        } catch (e: SecurityException) {
            // ignore - avoid crashing app if system disallows registering the listener
        }
        // If activity was started with update intent, try to start update
        checkAndStartUpdateFromIntent(intent)
    }

    override fun onPause() {
        super.onPause()
        try {
            appUpdateManager.unregisterListener(installStateListener)
        } catch (_: Exception) {
            // ignore
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        checkAndStartUpdateFromIntent(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_UPDATE) {
            if (resultCode != RESULT_OK) {
                // User canceled or update failed; no-op or show message
            }
        }
    }

    private fun checkAndStartUpdateFromIntent(intent: Intent?) {
        val shouldStart = intent?.getBooleanExtra(id.onyet.app.notifylog.update.UpdateNotificationHelper.EXTRA_START_UPDATE, false) ?: false
        if (shouldStart) {
            startFlexibleUpdate()
        }
    }

    private fun startFlexibleUpdate() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == com.google.android.play.core.install.model.UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(com.google.android.play.core.install.model.AppUpdateType.FLEXIBLE)
            ) {
                try {
                    appUpdateManager.startUpdateFlowForResult(appUpdateInfo, com.google.android.play.core.install.model.AppUpdateType.FLEXIBLE, this, REQUEST_CODE_UPDATE)
                } catch (e: Exception) {
                    openPlayStore()
                }
            } else {
                openPlayStore()
            }
        }.addOnFailureListener {
            openPlayStore()
        }
    }

    private fun openPlayStore() {
        val uri = android.net.Uri.parse("https://play.google.com/store/apps/details?id=id.onyet.app.notifylog")
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri).apply {
            setPackage("com.android.vending")
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback to browser
            startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, uri))
        }
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
