package com.example.voicebill.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.voicebill.ui.screens.categories.CategoriesScreen
import com.example.voicebill.ui.screens.home.HomeScreen
import com.example.voicebill.ui.screens.records.RecordsScreen
import com.example.voicebill.ui.screens.settings.SettingsScreen
import com.example.voicebill.ui.screens.statistics.StatisticsScreen

@Composable
fun VoiceBillNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen()
        }
        composable(Screen.Categories.route) {
            CategoriesScreen()
        }
        composable(Screen.Records.route) {
            RecordsScreen()
        }
        composable(Screen.Statistics.route) {
            StatisticsScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
