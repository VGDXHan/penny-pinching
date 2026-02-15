package com.example.voicebill.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Categories : Screen("categories")
    object Records : Screen("records")
    object Statistics : Screen("statistics")
    object Settings : Screen("settings")
}
