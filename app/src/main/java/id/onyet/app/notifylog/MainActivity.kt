package id.onyet.app.notifylog

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import id.onyet.app.notifylog.ui.navigation.NotifyLogNavGraph
import id.onyet.app.notifylog.ui.theme.NotifyLogTheme
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

            CompositionLocalProvider(LocalAppLocale provides languageCode) {
                NotifyLogTheme(darkTheme = isDarkMode) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        ProvideLocalizedContext(localizedContext) {
                            val navController = rememberNavController()
                            NotifyLogNavGraph(navController = navController)
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
fun ProvideLocalizedContext(
    context: Context,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalContext provides context,
        content = content
    )
}
