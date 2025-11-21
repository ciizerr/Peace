package com.nami.peace.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    object Home : Screen("home_screen")
    object Settings : Screen("settings")
    object Profile : Screen("profile_screen")
}
