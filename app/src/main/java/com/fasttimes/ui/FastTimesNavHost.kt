package com.fasttimes.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fasttimes.ui.history.HistoryScreen
import com.fasttimes.ui.settings.SettingsScreen


@Composable
fun FastTimesNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "dashboard") {
        composable("dashboard") {
            DashboardScreen(
                onSettingsClick = { navController.navigate("settings") },
                onHistoryClick = { navController.navigate("history") },
                onViewFastDetails = { fastId ->
                    navController.navigate("history/$fastId")
                }
            )
        }
        composable("settings") {
            SettingsScreen(
                onBackClick = { navController.navigateUp() }
            )
        }
        composable(
            route = "history/{fastId}",
            arguments = listOf(navArgument("fastId") { type = NavType.LongType })
        ) {
            HistoryScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        composable("history") {
            HistoryScreen(
                onBackClick = { navController.navigateUp() }
            )
        }
    }
}