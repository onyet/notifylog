package id.onyet.app.notifylog.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object NotificationDetail : Screen("notification_detail/{notificationId}") {
        fun createRoute(notificationId: Long) = "notification_detail/$notificationId"
    }
    object Settings : Screen("settings")
}
