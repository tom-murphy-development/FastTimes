/*
 * Copyright (C) 2025 tom-murphy-development
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.fasttimes.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fasttimes.ui.history.HistoryScreen
import com.fasttimes.ui.history.Timeline
import com.fasttimes.ui.history.TimelineSegment
import com.fasttimes.ui.history.TimelineSegmentType
import com.fasttimes.ui.profile.navigateToProfileManagement
import com.fasttimes.ui.profile.profileManagementScreen
import com.fasttimes.ui.settings.AccentColorScreen
import com.fasttimes.ui.settings.SettingsScreen
import com.fasttimes.ui.statistics.StatisticsScreen
import com.fasttimes.ui.theme.FastTimesTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FastTimesNavHost() {
    val navController = rememberNavController()
    val draggableState = rememberDraggableScreenState()
    val scope = rememberCoroutineScope()
    var showHistoryInfo by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            val currentTitle = when (draggableState.state.currentValue) {
                DragAnchors.Dashboard -> "Fast Times"
                DragAnchors.History -> "History"
            }
            val currentStyle = when (draggableState.state.currentValue) {
                DragAnchors.Dashboard -> MaterialTheme.typography.headlineLarge
                DragAnchors.History -> MaterialTheme.typography.headlineLarge
            }
            val currentColor = when (draggableState.state.currentValue) {
                DragAnchors.Dashboard -> FastTimesTheme.accentColor
                DragAnchors.History -> MaterialTheme.colorScheme.onSurface
            }

            BackHandler(enabled = draggableState.state.currentValue == DragAnchors.History) {
                scope.launch {
                    draggableState.closeHistory()
                }
            }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(currentTitle, style = currentStyle, color = currentColor) },
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
                            if (draggableState.state.currentValue == DragAnchors.History) {
                                IconButton(onClick = { showHistoryInfo = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "History Info",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            IconButton(onClick = { navController.navigate("settings") }) {
                                Icon(Icons.Filled.Settings, contentDescription = "Settings")
                            }
                        }
                    )
                }
            ) { padding ->
                if (showHistoryInfo) {
                    ModalBottomSheet(
                        onDismissRequest = { showHistoryInfo = false },
                        sheetState = sheetState,
                        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
                    ) {
                        HistoryInfoContent(onDismiss = { showHistoryInfo = false })
                    }
                }

                DraggableScreen(
                    modifier = Modifier.padding(padding),
                    state = draggableState,
                    dashboardContent = {
                        DashboardScreen(
                            onHistoryClick = { scope.launch { draggableState.openHistory() } },
                            onStatisticsClick = { navController.navigate("statistics") },
                            onViewFastDetails = { fastId ->
                                navController.navigate("history/$fastId")
                            },
                            onManageProfilesClick = { navController.navigateToProfileManagement() }
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
        profileManagementScreen(onBackClick = { navController.navigateUp() })
        composable("settings") {
            SettingsScreen(
                onBackClick = { navController.navigateUp() },
                onAccentColorClick = { navController.navigate("settings/accent-color") }
            )
        }
        composable("settings/accent-color") {
            AccentColorScreen(onNavigateUp = { navController.navigateUp() })
        }
        composable("statistics") {
            StatisticsScreen(
                onBackClick = { navController.navigateUp() },
                onHistoryClick = {
                    navController.popBackStack("main", inclusive = false)
                    scope.launch {
                        draggableState.openHistory()
                    }
                }
            )
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

@Composable
fun HistoryInfoContent(onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        Text(
            text = "Understanding History",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Calendar/Timeline Explanation
            HistoryInfoDetailItem(
                title = "Calendar View",
                description = "Each dot represents a fasting session. Tap any day to see the exact start and end times, or swipe between days to navigate your history."
            )

            // Daily Timeline Explanation
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Daily Timeline",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Timeline(
                    segments = listOf(
                        TimelineSegment(TimelineSegmentType.Fasting, 0.3f),
                        TimelineSegment(TimelineSegmentType.NonFasting, 0.4f),
                        TimelineSegment(TimelineSegmentType.Fasting, 0.3f)
                    ),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(MaterialTheme.shapes.extraSmall)
                            .background(FastTimesTheme.accentColor)
                    )
                    Text(
                        text = "Fasting: Active fasting periods throughout the day.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(MaterialTheme.shapes.extraSmall)
                            .background(Color.Gray)
                    )
                    Text(
                        text = "Non-Fasting: Time spent between fasts (eating windows).",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Goal Indicators Explanation
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Goal Indicators",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = FastTimesTheme.accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Goal Reached: You successfully completed your target fasting duration.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Goal Logged: A target was set, but the fast ended before it was reached.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Text("Got it")
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun HistoryInfoDetailItem(title: String, description: String) {
    Column {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
