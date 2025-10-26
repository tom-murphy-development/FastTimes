package com.fasttimes.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.fasttimes.data.FastingProfile
import com.fasttimes.ui.dashboard.DashboardUiState
import com.fasttimes.ui.dashboard.DashboardViewModel
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val profiles by viewModel.profiles.collectAsState(initial = emptyList())
    val stats by viewModel.stats.collectAsState()
    val modalProfile by viewModel.modalProfile.collectAsState()
    val showAlarmPermissionRationale by viewModel.showAlarmPermissionRationale.collectAsState()

    val sdf = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val context = LocalContext.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) Unit
                // Handle the case where the user denies the permission
        }
    )

    LaunchedEffect(Unit) {
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    val onStartFast: (FastingProfile) -> Unit = { profile ->
        viewModel.startProfileFast(profile)
    }

    val onStartManualFast: () -> Unit = {
        viewModel.startManualFast()
    }

    val onEndFast: () -> Unit = { viewModel.endCurrentFast() }
    val onShowProfile: (FastingProfile) -> Unit = { profile -> viewModel.showProfileModal(profile) }
    val onDismissProfileDetails: () -> Unit = { viewModel.dismissProfileModal() }
    val onDismissAlarmPermissionRationale: () -> Unit = { viewModel.dismissAlarmPermissionRationale() }

    if (modalProfile != null) {
        ProfileDetailsModal(
            profile = modalProfile!!,
            onDismiss = onDismissProfileDetails,
            onConfirm = { onStartFast(modalProfile!!) }
        )
    }

    if (showAlarmPermissionRationale) {
        AlertDialog(
            onDismissRequest = onDismissAlarmPermissionRationale,
            title = { Text("Permission Required") },
            text = { Text("To ensure you're notified when your fast is complete, the app needs permission to schedule exact alarms. This is only used to show a notification when the timer ends.") },
            confirmButton = {
                Button(onClick = {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                    onDismissAlarmPermissionRationale()
                }) {
                    Text("Go to Settings")
                }
            },
            dismissButton = {
                Button(onClick = onDismissAlarmPermissionRationale) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fast Times") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = uiState is DashboardUiState.NoFast,
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
                when (val state = uiState) {
                    is DashboardUiState.NoFast -> {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Current Fast", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(16.dp))
                            Text("No fast in progress.", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        }
                    }
                    is DashboardUiState.FastingInProgress -> {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Current Fast", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(16.dp))
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { state.progress },
                                    modifier = Modifier.size(260.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 20.dp,
                                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                                    strokeCap = StrokeCap.Round
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        state.activeFast.profile.displayName,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(Modifier.height(8.dp))

                                    Text(
                                        text = "Remaining",
                                        style = MaterialTheme.typography.titleSmall
                                    )

                                    Text(
                                        text = formatDuration(state.remainingTime),
                                        style = MaterialTheme.typography.displaySmall
                                    )
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            Text("Started: ${sdf.format(Date(state.activeFast.startTime))}")
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = onEndFast,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            ) {
                                Icon(Icons.Filled.Stop, contentDescription = "End Fast")
                                Spacer(Modifier.width(8.dp))
                                Text("End Fast")
                            }
                        }
                    }
                    is DashboardUiState.ManualFasting -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Manual Fast", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(16.dp))
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { 1f }, // Static progress for manual fast
                                    modifier = Modifier.size(260.dp),
                                    color = MaterialTheme.colorScheme.secondary,
                                    strokeWidth = 20.dp,
                                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                                    strokeCap = StrokeCap.Round
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Elapsed Time",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(Modifier.height(8.dp))

                                    Text(
                                        text = formatDuration(state.elapsedTime),
                                        style = MaterialTheme.typography.displaySmall
                                    )
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            Text("Started: ${sdf.format(Date(state.activeFast.startTime))}")
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = onEndFast,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            ) {
                                Icon(Icons.Filled.Stop, contentDescription = "End Fast")
                                Spacer(Modifier.width(8.dp))
                                Text("End Fast")
                            }
                        }
                    }
                    is DashboardUiState.FastingGoalReached -> {
                        // Use a Box to allow stacking Composables. The confetti will be drawn on top of the Column.
                        Box(contentAlignment = Alignment.TopCenter) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Current Fast", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), modifier = Modifier.fillMaxWidth())
                                Spacer(Modifier.height(16.dp))
                                Box(contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        progress = { 1f },
                                        modifier = Modifier.size(260.dp),
                                        color = Color(0xFF3DDC84), // Vibrant success color
                                        strokeWidth = 20.dp,
                                        trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                                        strokeCap = StrokeCap.Round
                                    )
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "Goal Reached!",
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFF3DDC84)
                                        )
                                        Spacer(Modifier.height(8.dp))

                                        Text(
                                            text = "Total Time",
                                            style = MaterialTheme.typography.titleSmall
                                        )

                                        Text(
                                            text = formatDuration(state.totalElapsedTime),
                                            style = MaterialTheme.typography.displaySmall
                                        )
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                                Text("Started: ${sdf.format(Date(state.activeFast.startTime))}")
                                Spacer(Modifier.height(16.dp))
                                Button(
                                    onClick = onEndFast,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                ) {
                                    Icon(Icons.Filled.Stop, contentDescription = "End Fast")
                                    Spacer(Modifier.width(8.dp))
                                    Text("End Fast")
                                }
                            }

                            KonfettiView(
                                modifier = Modifier.matchParentSize(),
                                parties = remember {
                                    listOf(
                                        Party(
                                            speed = 0f,
                                            maxSpeed = 15f,
                                            damping = 0.9f,
                                            spread = 360,
                                            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                                            position = nl.dionsegijn.konfetti.core.Position.Relative(0.5, 0.0),
                                            emitter = Emitter(duration = 5, java.util.concurrent.TimeUnit.SECONDS).perSecond(100)
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(visible = uiState is DashboardUiState.NoFast) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Choose a Profile", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(16.dp))
                        if (profiles.isEmpty()) {
                            Text("No profiles created yet. Go to Settings to create one.")
                        } else {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
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
            }

            // Statistics Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Statistics", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(16.dp))
                    Text("Total Fasts: ${stats.totalFasts}")
                    Text("Total Fasting Time: ${formatDuration(stats.totalFastingTime.milliseconds)}")
                    Text("Longest Fast: ${formatDuration(stats.longestFast.milliseconds)}")
                }
            }

            // History Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onHistoryClick() }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("History", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(16.dp))
                    Text("View your complete fasting history.")
                }
            }
        }
    }
}
