package com.fasttimes.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fasttimes.data.fast.Fast
import com.fasttimes.ui.dashboard.DashboardStats
import com.fasttimes.ui.dashboard.DashboardViewModel
import com.fasttimes.ui.theme.FastTimesTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onSettingsClick: () -> Unit
) {
    val currentFast by viewModel.currentFast.collectAsState()
    val elapsedTime by viewModel.elapsedTime.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val history by viewModel.history.collectAsState()

    DashboardScreenContent(
        currentFast = currentFast,
        elapsedTime = elapsedTime,
        stats = stats,
        history = history,
        onStartFast = viewModel::startFast,
        onEndFast = viewModel::endCurrentFast,
        onSettingsClick = onSettingsClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreenContent(
    currentFast: Fast?,
    elapsedTime: Long,
    stats: DashboardStats,
    history: List<Fast>,
    onStartFast: () -> Unit,
    onEndFast: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("HH:mm, dd MMM yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fasting Dashboard") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = currentFast == null,
                enter = fadeIn(animationSpec = tween(500)),
                exit = fadeOut(animationSpec = tween(500))
            ) {
                FloatingActionButton(
                    onClick = onStartFast,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Start Fast")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Current Fast Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Current Fast", style = MaterialTheme.typography.titleMedium)
                    AnimatedVisibility(
                        visible = currentFast != null,
                        enter = fadeIn(animationSpec = tween(500)),
                        exit = fadeOut(animationSpec = tween(500))
                    ) {
                        currentFast?.let { fast ->
                            // Use a Column to arrange children vertically and prevent overlap.
                            Column {
                                Text("Started: ${sdf.format(Date(fast.startTime))}")
                                Text("Elapsed: ${formatElapsed(elapsedTime)}", style = MaterialTheme.typography.bodyLarge)
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = onEndFast,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                                ) {
                                    Icon(Icons.Filled.Stop, contentDescription = "End Fast")
                                    Spacer(Modifier.width(8.dp))
                                    Text("End Fast")
                                }
                            }
                        }
                    }
                    AnimatedVisibility(
                        visible = currentFast == null,
                        enter = fadeIn(animationSpec = tween(500)),
                        exit = fadeOut(animationSpec = tween(500))
                    ) {
                        Text("No fast in progress.")
                    }
                }
            }

            // Stats Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Stats")
                    Text("Total Fasts: ${stats.totalFasts}")
                    Text("Longest Fast: ${stats.longestFast}h")
                    // Add more stats as needed
                }
            }

            // History Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("History")
                    if (history.isEmpty()) {
                        Text("No fasts yet.")
                    } else {
                        history.take(3).forEach { fast ->
                            val endTimeText = fast.endTime?.let { endTime ->
                                sdf.format(Date(endTime))
                            } ?: "In Progress"
                            Text("${sdf.format(Date(fast.startTime))} - $endTimeText")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Formats a duration in milliseconds into a HH:mm:ss string.
 */
private fun formatElapsed(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    FastTimesTheme {
        DashboardScreenContent(
            currentFast = null,
            elapsedTime = 0,
            stats = DashboardStats(totalFasts = 10, longestFast = 20),
            history = listOf(
                Fast(
                    id = 1,
                    startTime = System.currentTimeMillis() - 1000 * 60 * 60 * 24,
                    endTime = System.currentTimeMillis() - 1000 * 60 * 60 * 8,
                    targetDuration = 16 * 60 * 60 * 1000
                )
            ),
            onStartFast = {},
            onEndFast = {},
            onSettingsClick = {}
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DashboardScreenDarkPreview() {
    FastTimesTheme(darkTheme = true) {
        DashboardScreenContent(
            currentFast = Fast(
                id = 1,
                startTime = System.currentTimeMillis() - 1000 * 60 * 60 * 4,
                endTime = null,
                targetDuration = 16 * 60 * 60 * 1000
            ),
            elapsedTime = 1000 * 60 * 60 * 4 + 1000 * 60 * 30 + 15,
            stats = DashboardStats(totalFasts = 10, longestFast = 20),
            history = listOf(),
            onStartFast = {},
            onEndFast = {},
            onSettingsClick = {}
        )
    }
}
