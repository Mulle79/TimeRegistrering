package com.example.timeregistrering.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.timeregistrering.ui.screens.*

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.TimeRegistration.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.TimeRegistration.route) {
            TimeRegistrationScreen(
                onNavigateToMap = {
                    navController.navigate(Screen.Map.route)
                }
            )
        }
        
        composable(Screen.Map.route) {
            MapScreen()
        }
        
        composable(Screen.WeekSchedule.route) {
            WeekScheduleScreen()
        }
    }
}

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object TimeRegistration : Screen("time_registration")
    object Map : Screen("map")
    object WeekSchedule : Screen("week_schedule")
    
    companion object {
        fun fromRoute(route: String?): Screen {
            return when (route) {
                Login.route -> Login
                TimeRegistration.route -> TimeRegistration
                Map.route -> Map
                WeekSchedule.route -> WeekSchedule
                else -> Login
            }
        }
    }
}
