package com.fasttimes.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fasttimes.data.FastingProfile
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
    val remainingTime by viewModel.remainingTime.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val history by viewModel.history.collectAsState()
    val profiles by viewModel.profiles.collectAsState()
    val modalProfile by viewModel.modalProfile.collectAsState()

    DashboardScreenContent(
        currentFast = currentFast,
        elapsedTime = elapsedTime,
        remainingTime = remainingTime,
        stats = stats,
        history = history,
        profiles = profiles,
        modalProfile = modalProfile,
        onStartManualFast = viewModel::startManualFast,
        onStartProfileFast = viewModel::startProfileFast,
        onEndFast = viewModel::endCurrentFast,
        onSettingsClick = onSettingsClick,
        onShowProfile = viewModel::showProfileModal,
        onDismissProfile = viewModel::dismissProfileModal
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DashboardScreenContent(
    currentFast: Fast?,
    elapsedTime: Long,
    remainingTime: Long,
    stats: DashboardStats,
    history: List<Fast>,
    profiles: List<FastingProfile>,
    modalProfile: FastingProfile?,
    onStartManualFast: () -> Unit,
    onStartProfileFast: (FastingProfile) -> Unit,
    onEndFast: () -> Unit,
    onSettingsClick: () -> Unit,
    onShowProfile: (FastingProfile) -> Unit,
    onDismissProfile: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("HH:mm, dd MMM yyyy", Locale.getDefault()) }

    if (modalProfile != null) {
        ProfileDetailsModal(
            profile = modalProfile,
            onDismiss = onDismissProfile,
            onConfirm = {
                onStartProfileFast(modalProfile)
                onDismissProfile()
            }
        )
    }

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
                    onClick = onStartManualFast,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Start Manual Fast")
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
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Current Fast", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(16.dp))
                    AnimatedVisibility(
                        visible = currentFast != null,
                        enter = fadeIn(animationSpec = tween(500)),
                        exit = fadeOut(animationSpec = tween(500))
                    ) {
                        currentFast?.let { fast ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                if (fast.profile == FastingProfile.MANUAL) {
                                    Text("Elapsed: ${formatDuration(elapsedTime)}", style = MaterialTheme.typography.displaySmall)
                                } else {
                                    val progress = remember(fast.targetDuration, remainingTime) {
                                        if (fast.targetDuration != null && fast.targetDuration > 0) {
                                            1f - (remainingTime.toFloat() / fast.targetDuration.toFloat())
                                        } else {
                                            0f
                                        }
                                    }

                                    Box(contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(
                                            progress = progress,
                                            modifier = Modifier.size(200.dp),
                                            strokeWidth = 8.dp,
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                            strokeCap = StrokeCap.Round,
                                        )
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                fast.profile.displayName,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                formatDuration(remainingTime),
                                                style = MaterialTheme.typography.displaySmall
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                                Text("Started: ${sdf.format(Date(fast.startTime))}")
                                Spacer(Modifier.height(16.dp))
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
                        Text("No fast in progress.", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            // Choose Profile Section
            AnimatedVisibility(visible = currentFast == null) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Choose a Profile", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        Spacer(Modifier.height(16.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            profiles.forEach { profile ->
                                Button(onClick = { onShowProfile(profile) }) {
                                    Text(profile.displayName)
                                 }
                            }
                        }
                    }
                }
            }


            // Stats Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Statistics", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    Text("Total Fasts: ${stats.totalFasts}")
                    Text("Longest Fast: ${stats.longestFast}h")
                    Text("Total Fasting Time: ${stats.totalFastingTime}h")
                }
            }

            // History Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("History", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
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

@Composable
fun ProfileDetailsModal(
    profile: FastingProfile,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "${profile.displayName} Profile") },
        text = { Text(text = profile.description) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Start Fast")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


/**
 * Formats a duration in milliseconds into a HH:mm:ss string.
 */
private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview_NoFast() {
    FastTimesTheme {
        DashboardScreenContent(
            currentFast = null,
            elapsedTime = 0,
            remainingTime = 0,
            stats = DashboardStats(totalFasts = 10, longestFast = 20, totalFastingTime = 120),
            history = listOf(),
            profiles = listOf(FastingProfile.SIXTEEN_EIGHT, FastingProfile.FOURTEEN_TEN),
            modalProfile = null,
            onStartManualFast = {},
            onStartProfileFast = {},
            onEndFast = {},
            onSettingsClick = {},
            onShowProfile = {},
            onDismissProfile = {}
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DashboardScreenPreview_ManualFast() {
    FastTimesTheme {
        DashboardScreenContent(
            currentFast = Fast(startTime = System.currentTimeMillis() - 1000 * 60 * 60 * 3, profile = FastingProfile.MANUAL),
            elapsedTime = 1000 * 60 * 60 * 3 + 1000 * 60 * 20 + 5,
            remainingTime = 0,
            stats = DashboardStats(totalFasts = 10, longestFast = 20, totalFastingTime = 120),
            history = listOf(),
            profiles = listOf(FastingProfile.SIXTEEN_EIGHT, FastingProfile.FOURTEEN_TEN),
            modalProfile = null,
            onStartManualFast = {},
            onStartProfileFast = {},
            onEndFast = {},
            onSettingsClick = {},
            onShowProfile = {},
            onDismissProfile = {}
        )
    }
}
