package com.example.timeregistrering.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.timeregistrering.ui.screens.*

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "week_schedule"
    ) {
        composable("week_schedule") {
            WeekScheduleScreen()
        }
        composable("moeder") {
            MoederScreen(navController)
        }
        composable("maanedsoversigt") {
            MaanedsoversigtsScreen(navController)
        }
        composable("indstillinger") {
            SettingsScreen(navController)
        }
    }
}
