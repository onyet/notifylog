package id.onyet.app.notifylog.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import id.onyet.app.notifylog.ui.screens.home.HomeScreen
import id.onyet.app.notifylog.ui.screens.detail.NotificationDetailScreen
import id.onyet.app.notifylog.ui.screens.onboarding.OnboardingScreen
import id.onyet.app.notifylog.ui.screens.settings.SettingsScreen
import id.onyet.app.notifylog.ui.screens.splash.SplashScreen

@Composable
fun NotifyLogNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = Screen.Splash.route) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(route = Screen.Onboarding.route) {
            OnboardingScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(route = Screen.Home.route) {
            HomeScreen(
                onNavigateToDetail = { notificationId ->
                    navController.navigate(Screen.NotificationDetail.createRoute(notificationId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        
        composable(
            route = Screen.NotificationDetail.route,
            arguments = listOf(
                navArgument("notificationId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val notificationId = backStackEntry.arguments?.getLong("notificationId") ?: 0L
            NotificationDetailScreen(
                notificationId = notificationId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(route = Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
