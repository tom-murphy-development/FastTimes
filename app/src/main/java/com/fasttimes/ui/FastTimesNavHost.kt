package com.fasttimes.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fasttimes.ui.history.HistoryScreen
import com.fasttimes.ui.settings.AccentColorScreen
import com.fasttimes.ui.settings.SettingsScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FastTimesNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            val draggableState = rememberDraggableScreenState()
            val scope = rememberCoroutineScope()

            val currentTitle = when (draggableState.state.currentValue) {
                DragAnchors.Dashboard -> "Fast Times"
                DragAnchors.History -> "History"
            }

            BackHandler(enabled = draggableState.state.currentValue == DragAnchors.History) {
                scope.launch {
                    draggableState.closeHistory()
                }
            }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(currentTitle) },
                        navigationIcon = {
                            if (draggableState.state.currentValue == DragAnchors.History) {
                                IconButton(onClick = { scope.launch { draggableState.closeHistory() } }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            }
                        },
                        actions = {
                            IconButton(onClick = { navController.navigate("settings") }) {
                                Icon(Icons.Filled.Settings, contentDescription = "Settings")
                            }
                        }
                    )
                }
            ) { padding ->
                DraggableScreen(
                    modifier = Modifier.padding(padding),
                    state = draggableState,
                    dashboardContent = {
                        DashboardScreen(
                            onHistoryClick = { scope.launch { draggableState.openHistory() } },
                            onViewFastDetails = { fastId ->
                                navController.navigate("history/$fastId")
                            }
                        )
                    },
                    historyContent = {
                        HistoryScreen(
                            onViewFastDetails = { fastId ->
                                navController.navigate("history/$fastId")
                            },
                            onSwipeBack = { scope.launch { draggableState.closeHistory() } }
                        )
                    }
                )
            }
        }
        composable("settings") {
            SettingsScreen(
                onBackClick = { navController.navigateUp() },
                onAccentColorClick = { navController.navigate("settings/accent-color") }
            )
        }
        composable("settings/accent-color") {
            AccentColorScreen(onNavigateUp = { navController.navigateUp() })
        }
        composable(
            route = "history/{fastId}",
            arguments = listOf(navArgument("fastId") { type = NavType.LongType })
        ) {
            HistoryScreen(
                onBackClick = { navController.navigateUp() },
                onViewFastDetails = { fastId ->
                    // While on the detail screen, we can just navigate to another one
                    navController.navigate("history/$fastId")
                }
            )
        }
    }
}