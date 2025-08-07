package com.fasttimes.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fasttimes.ui.dashboard.DashboardViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val currentFast = viewModel.currentFast.collectAsState().value
    val stats = viewModel.stats.collectAsState().value
    val history = viewModel.history.collectAsState().value
    val coroutineScope = rememberCoroutineScope()
    var elapsedTime by remember { mutableStateOf(0L) }

    // Timer effect for current fast
    if (currentFast != null) {
        LaunchedEffect(currentFast.id, currentFast.startTime, currentFast.endTime) {
            while (currentFast.endTime == null) {
                elapsedTime = System.currentTimeMillis() - currentFast.startTime
                delay(1000)
            }
            if (currentFast.endTime != null) {
                elapsedTime = currentFast.endTime - currentFast.startTime
            }
        }
    } else {
        elapsedTime = 0L
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Fasting Dashboard",
                style = MaterialTheme.typography.headlineMedium
            )
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
                        if (currentFast != null) {
                            val sdf = SimpleDateFormat("HH:mm, dd MMM yyyy", Locale.getDefault())
                            Text("Started: ${'$'}{sdf.format(Date(currentFast.startTime))}")
                            Text("Elapsed: ${'$'}{formatElapsed(elapsedTime)}", style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.endCurrentFast() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                            ) {
                                Icon(Icons.Filled.Stop, contentDescription = "End Fast")
                                Spacer(Modifier.width(8.dp))
                                Text("End Fast")
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
                    Text("Total Fasts: ${'$'}{stats.totalFasts}")
                    Text("Longest Fast: ${'$'}{stats.longestFast}h")
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
                        val sdf = SimpleDateFormat("HH:mm, dd MMM yyyy", Locale.getDefault())
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
        // FAB for starting a fast
        AnimatedVisibility(
            visible = currentFast == null,
            modifier = Modifier.align(Alignment.BottomEnd),
            enter = fadeIn(animationSpec = tween(500)),
            exit = fadeOut(animationSpec = tween(500))
        ) {
            FloatingActionButton(
                onClick = { viewModel.startFast() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Start Fast")
            }
        }
    }
}

// Helper function for formatting elapsed time
fun formatElapsed(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}
