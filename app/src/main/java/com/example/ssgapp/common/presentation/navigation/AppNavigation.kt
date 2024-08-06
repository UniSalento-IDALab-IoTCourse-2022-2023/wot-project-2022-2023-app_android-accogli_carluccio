package com.example.ssgapp.common.presentation.navigation

enum class Screen {
    LOGIN,
    SAFETY_SCANNER_SCREEN,
    MACHINERY_SELECTION_SCREEN,
    MACHINERY_DASHBOARD_SCREEN
}
sealed class NavigationItem(val route: String) {
    object Login : NavigationItem(Screen.LOGIN.name)
    object SafetyScannerScreen : NavigationItem(Screen.SAFETY_SCANNER_SCREEN.name)
    object MachinerySelectionScreen : NavigationItem(Screen.MACHINERY_SELECTION_SCREEN.name)
    object MachineryDashboardScreen : NavigationItem(Screen.MACHINERY_DASHBOARD_SCREEN.name)

}