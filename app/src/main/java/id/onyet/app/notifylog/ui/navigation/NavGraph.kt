package id.onyet.app.notifylog.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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

private const val ANIMATION_DURATION = 250
private const val FADE_DURATION = 200

@Composable
fun NotifyLogNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Splash Screen - Fade transition
        composable(
            route = Screen.Splash.route,
            enterTransition = {
                fadeIn(animationSpec = tween(FADE_DURATION, easing = FastOutSlowInEasing))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(FADE_DURATION, easing = FastOutSlowInEasing))
            }
        ) {
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
        
        // Onboarding Screen - Scale + Fade transition
        composable(
            route = Screen.Onboarding.route,
            enterTransition = {
                scaleIn(
                    initialScale = 0.95f,
                    animationSpec = tween(ANIMATION_DURATION, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(FADE_DURATION, easing = FastOutSlowInEasing))
            },
            exitTransition = {
                scaleOut(
                    targetScale = 0.95f,
                    animationSpec = tween(ANIMATION_DURATION, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(FADE_DURATION, easing = FastOutSlowInEasing))
            }
        ) {
            OnboardingScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Home Screen - Scale + Fade transition
        composable(
            route = Screen.Home.route,
            enterTransition = {
                scaleIn(
                    initialScale = 0.95f,
                    animationSpec = tween(ANIMATION_DURATION, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(FADE_DURATION, easing = FastOutSlowInEasing))
            },
            exitTransition = {
                scaleOut(
                    targetScale = 1.02f,
                    animationSpec = tween(ANIMATION_DURATION / 2, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(FADE_DURATION / 2, easing = FastOutSlowInEasing))
            },
            popEnterTransition = {
                scaleIn(
                    initialScale = 1.02f,
                    animationSpec = tween(ANIMATION_DURATION, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(FADE_DURATION, easing = FastOutSlowInEasing))
            },
            popExitTransition = {
                scaleOut(
                    targetScale = 0.95f,
                    animationSpec = tween(ANIMATION_DURATION / 2, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(FADE_DURATION / 2, easing = FastOutSlowInEasing))
            }
        ) {
            HomeScreen(
                onNavigateToDetail = { notificationId ->
                    navController.navigate(Screen.NotificationDetail.createRoute(notificationId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        
        // Notification Detail Screen - Slide from right with smooth easing
        composable(
            route = Screen.NotificationDetail.route,
            arguments = listOf(
                navArgument("notificationId") { type = NavType.LongType }
            ),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(ANIMATION_DURATION, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(FADE_DURATION, easing = FastOutSlowInEasing))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(ANIMATION_DURATION, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(FADE_DURATION, easing = FastOutSlowInEasing))
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(ANIMATION_DURATION, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(FADE_DURATION, easing = FastOutSlowInEasing))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(ANIMATION_DURATION, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(FADE_DURATION, easing = FastOutSlowInEasing))
            }
        ) { backStackEntry ->
            val notificationId = backStackEntry.arguments?.getLong("notificationId") ?: 0L
            NotificationDetailScreen(
                notificationId = notificationId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Settings Screen - Slide from bottom with smooth easing
        composable(
            route = Screen.Settings.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(ANIMATION_DURATION, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(FADE_DURATION, easing = FastOutSlowInEasing))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(ANIMATION_DURATION, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(FADE_DURATION, easing = FastOutSlowInEasing))
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(ANIMATION_DURATION, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(FADE_DURATION, easing = FastOutSlowInEasing))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(ANIMATION_DURATION, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(FADE_DURATION, easing = FastOutSlowInEasing))
            }
        ) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
