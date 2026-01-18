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

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.fasttimes.R
import com.fasttimes.data.fast.Fast
import com.fasttimes.data.profile.FastingProfile
import com.fasttimes.data.profile.durationMinutes
import com.fasttimes.ui.components.StatisticTile
import com.fasttimes.ui.dashboard.DashboardUiState
import com.fasttimes.ui.dashboard.DashboardViewModel
import com.fasttimes.ui.dashboard.FastingSummaryModal
import com.fasttimes.ui.editfast.EditFastRoute
import com.fasttimes.ui.theme.FastTimesTheme
import com.fasttimes.ui.theme.contentColorFor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds


private val confettiParty = listOf(
    Party(
        speed = 0f,
        maxSpeed = 15f,
        damping = 0.9f,
        spread = 360,
        colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
        position = Position.Relative(0.5, 0.0),
        emitter = Emitter(duration = 5, TimeUnit.SECONDS).perSecond(100),
        fadeOutEnabled = true,
    )
)

private data class FabButtonItem(
    val icon: ImageVector,
    val label: String,
    val action: () -> Unit
)


@OptIn(
    ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun DashboardScreen(
    onHistoryClick: () -> Unit,
    onViewFastDetails: (Long) -> Unit,
    onManageProfilesClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val profiles by viewModel.profiles.collectAsState()
    val favoriteProfile by viewModel.favoriteProfile.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val showAlarmPermissionRationale by viewModel.showAlarmPermissionRationale.collectAsState()
    val completedFast by viewModel.completedFast.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    val sdf = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val context = LocalContext.current

    var parties by remember { mutableStateOf(emptyList<Party>()) }

    val onStartFast: (FastingProfile) -> Unit = {
        viewModel.startProfileFast(it)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // For Tiramisu+, we also need to ask for exact alarm permissions
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
            }
        }
    )

    val onEndFast: () -> Unit = { viewModel.endCurrentFast() }
    val onDismissAlarmPermissionRationale: () -> Unit = { viewModel.dismissAlarmPermissionRationale() }

    val isEditing = when (uiState) {
        is DashboardUiState.FastingInProgress -> (uiState as DashboardUiState.FastingInProgress).isEditing
        is DashboardUiState.FastingGoalReached -> (uiState as DashboardUiState.FastingGoalReached).isEditing
        is DashboardUiState.ManualFasting -> (uiState as DashboardUiState.ManualFasting).isEditing
        else -> false
    }

    if (isEditing) {
        ModalBottomSheet(
            onDismissRequest = viewModel::onEditFastDismissed,
            sheetState = sheetState
        ) {
            EditFastRoute(onDismiss = viewModel::onEditFastDismissed, fastId = null)
        }
    }

    if (showAlarmPermissionRationale) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        AlertDialog(
            onDismissRequest = onDismissAlarmPermissionRationale,            title = { Text("Permission Required") },
            text = { Text("To ensure you're notified when your fast is complete, the app needs permission to post notifications and schedule exact alarms. This is only used to show a notification when the timer ends.") },
            confirmButton = {
                Button(onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        // On Android 13+, ask for notifications first.
                        // The launcher callback handles the Exact Alarm check.
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        // On Android 12 (S/S_V2), notifications are implicit, but we might need Exact Alarm.
                        // Only send to settings if we don't have it.
                        if (!alarmManager.canScheduleExactAlarms()) {
                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }
                    }
                    onDismissAlarmPermissionRationale()
                }) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                Button(onClick = onDismissAlarmPermissionRationale) {
                    Text("Cancel")
                }
            }
        )
    }

    completedFast?.let {
        FastingSummaryModal(
            fast = it,
            onDismiss = viewModel::onFastingSummaryDismissed,
            onSaveRating = { rating -> viewModel.saveFastRating(it.id, rating) }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val state = uiState
            if (state is DashboardUiState.Loading && state.showSkeleton) {
                // Skeleton Loading UI
                SkeletonLoader()
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
                ) {
                    when (state) {
                        is DashboardUiState.Loading -> {
                            // Intentionally empty, handled by the skeleton loader
                        }

                        is DashboardUiState.NoFast -> {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Set your goal",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                    IconButton(onClick = onManageProfilesClick) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Manage Profiles"
                                        )
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                                GoalSelectionSection(
                                    profiles = profiles,
                                    onStartFast = onStartFast,
                                    onManageProfilesClick = onManageProfilesClick
                                )
                            }
                        }

                        is DashboardUiState.FastingInProgress -> {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Current Fast",
                                        style = MaterialTheme.typography.headlineMedium,
                                    )
                                    Row {
                                        IconButton(onClick = viewModel::onEditFast) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Fast"
                                            )
                                        }
                                        IconButton(onClick = onManageProfilesClick) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Manage Profiles"
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                                Box(contentAlignment = Alignment.Center) {
                                    if (state.useWavyIndicator) {
                                        CircularWavyProgressIndicator(
                                            progress = { state.progress },
                                            modifier = Modifier.size(260.dp),
                                            color = FastTimesTheme.accentColor,
                                            trackColor = MaterialTheme.colorScheme.secondary.copy(
                                                alpha = 0.1f
                                            ),
                                            stroke = Stroke(
                                                width = with(LocalDensity.current) {
                                                    16.dp.toPx()
                                                },
                                                cap = StrokeCap.Round,
                                            ),
                                            trackStroke = Stroke(
                                                width = with(LocalDensity.current) {
                                                    16.dp.toPx()
                                                },
                                                cap = StrokeCap.Round,
                                            ),
                                            wavelength = 60.dp,
                                            waveSpeed = 15.dp,
                                            gapSize = 8.dp
                                        )
                                    } else {
                                        CircularProgressIndicator(
                                            progress = { state.progress },
                                            modifier = Modifier.size(260.dp),
                                            color = FastTimesTheme.accentColor,
                                            strokeWidth = 20.dp,
                                            trackColor = MaterialTheme.colorScheme.secondary.copy(
                                                alpha = 0.1f
                                            ),
                                            strokeCap = StrokeCap.Round
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            state.activeFast.profileName,
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
                            val infiniteTransition =
                                rememberInfiniteTransition(label = "pulsating_ring")
                            val scale by infiniteTransition.animateFloat(
                                initialValue = 1f,
                                targetValue = 1.08f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(3000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "pulsating_scale"
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Manual Fast",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Row {
                                        IconButton(onClick = viewModel::onEditFast) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Fast"
                                            )
                                        }
                                        IconButton(onClick = onManageProfilesClick) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Manage Profiles"
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                                Box(contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        progress = { 1f },
                                        modifier = Modifier
                                            .size(260.dp)
                                            .graphicsLayer {
                                                scaleX = scale
                                                scaleY = scale
                                            },
                                        color = MaterialTheme.colorScheme.secondary,
                                        strokeWidth = 20.dp,
                                        trackColor = MaterialTheme.colorScheme.secondary.copy(
                                            alpha = 0.1f
                                        ),
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
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Current Fast",
                                        style = MaterialTheme.typography.headlineMedium,
                                    )
                                    Row {
                                        IconButton(onClick = viewModel::onEditFast) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Fast"
                                            )
                                        }
                                        IconButton(onClick = onManageProfilesClick) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Manage Profiles"
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                                Box(contentAlignment = Alignment.Center) {
                                    CircularWavyProgressIndicator(
                                        progress = { 1f },
                                        modifier = Modifier.size(260.dp),
                                        color = FastTimesTheme.accentColor,
                                        trackColor = MaterialTheme.colorScheme.secondary.copy(
                                            alpha = 0.1f
                                        ),
                                        stroke = Stroke(
                                            width = with(LocalDensity.current) {
                                                16.dp.toPx()
                                            },
                                            cap = StrokeCap.Round,
                                        ),
                                        trackStroke = Stroke(
                                            width = with(LocalDensity.current) {
                                                16.dp.toPx()
                                            },
                                            cap = StrokeCap.Round,
                                        ),
                                        wavelength = 60.dp,
                                        waveSpeed = 15.dp,
                                        gapSize = 8.dp
                                    )
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "Goal Reached!",
                                            style = MaterialTheme.typography.titleLargeEmphasized,
                                            color = FastTimesTheme.accentColor
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
                        }
                    }
                }

                // Statistics Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Statistics",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                StatisticTile(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.BarChart,
                                    label = "Total Fasts",
                                    value = stats.totalFasts.toString()
                                )
                                StatisticTile(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.Timer,
                                    label = "Total Time",
                                    value = formatDuration(stats.totalFastingTime)
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                StatisticTile(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.Star,
                                    label = "Longest Fast",
                                    value = stats.longestFast?.let { fast ->
                                        fast.endTime?.let { endTime ->
                                            formatDuration((endTime - fast.startTime).milliseconds)
                                        }
                                    } ?: "-",
                                    onClick = { stats.longestFast?.id?.let(onViewFastDetails) }
                                )
                                StatisticTile(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.AvTimer,
                                    label = "Average Fast",
                                    value = formatDuration(stats.averageFast)
                                )
                            }
                        }
                    }
                }

                // History Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onHistoryClick() }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "History",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Go to History"
                            )
                        }

                        if (state is DashboardUiState.NoFast) {
                            if (state.thisWeekFasts.isEmpty() && state.lastWeekFasts.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No recent fasts to show.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .verticalScroll(rememberScrollState())
                                        .padding(horizontal = 16.dp)
                                        .padding(bottom = 16.dp)
                                ) {
                                    if (state.thisWeekFasts.isNotEmpty()) {
                                        Text(
                                            "This Week",
                                            style = MaterialTheme.typography.labelLarge,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            state.thisWeekFasts.forEachIndexed { index, fast ->
                                                LastFastItem(
                                                    fast = fast,
                                                    modifier = Modifier.clickable { onViewFastDetails(fast.id) })
                                                if (index < state.thisWeekFasts.lastIndex) {
                                                    HorizontalDivider(
                                                        modifier = Modifier.padding(
                                                            vertical = 8.dp
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    if (state.lastWeekFasts.isNotEmpty()) {
                                        if (state.thisWeekFasts.isNotEmpty()) {
                                            Spacer(Modifier.height(16.dp))
                                        }
                                        Text(
                                            "Last Week",
                                            style = MaterialTheme.typography.labelLarge,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            state.lastWeekFasts.forEachIndexed { index, fast ->
                                                LastFastItem(
                                                    fast = fast,
                                                    modifier = Modifier.clickable { onViewFastDetails(fast.id) })
                                                if (index < state.lastWeekFasts.lastIndex) {
                                                    HorizontalDivider(
                                                        modifier = Modifier.padding(
                                                            vertical = 8.dp
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        val state = uiState
        if (state is DashboardUiState.FastingGoalReached && state.showConfetti) {
            LaunchedEffect(state.activeFast.id) {
                scope.launch {
                    parties = confettiParty
                    delay(10000L)
                    parties = emptyList()
                }
                viewModel.onConfettiShown(state.activeFast.id)
            }
        }

        if (parties.isNotEmpty()) {
            KonfettiView(
                modifier = Modifier.fillMaxSize(),
                parties = parties
            )
        }

        if (uiState is DashboardUiState.NoFast && (uiState as DashboardUiState.NoFast).showFab) {
            val fabItems = mutableListOf<FabButtonItem>()
            favoriteProfile?.let {
                fabItems.add(
                    FabButtonItem(
                        icon = Icons.Default.Star,
                        label = "Start \"${it.displayName}\" Fast",
                        action = { onStartFast(it) }
                    )
                )
            }
            fabItems.add(
                FabButtonItem(
                    icon = Icons.Default.Timer,
                    label = "Start Manual Fast",
                    action = viewModel::startManualFast
                )
            )
            fabItems.add(
                FabButtonItem(
                    icon = Icons.Default.History,
                    label = "View History",
                    action = onHistoryClick
                )
            )

            MultiFab(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                items = fabItems
            )
        }
    }
}

@Composable
private fun SkeletonLoader() {
    val skeletonColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Current Fast Skeleton
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header placeholder
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .width(150.dp)
                            .background(skeletonColor)
                    )
                    Row {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(skeletonColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(skeletonColor)
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                // Timer placeholder
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .clip(CircleShape)
                        .background(skeletonColor.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .clip(CircleShape)
                            .background(skeletonColor.copy(alpha = 0.1f))
                    )
                }
                Spacer(Modifier.height(16.dp))
                // Start time placeholder
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .width(200.dp)
                        .background(skeletonColor)
                )
                Spacer(Modifier.height(16.dp))
                // Button placeholder
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .width(120.dp)
                        .background(skeletonColor)
                )
            }
        }

        // Statistics Skeleton
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .width(120.dp)
                        .padding(bottom = 8.dp)
                        .background(skeletonColor)
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                                .background(skeletonColor)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                                .background(skeletonColor)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                                .background(skeletonColor)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                                .background(skeletonColor)
                        )
                    }
                }
            }
        }

        // History Skeleton
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .width(100.dp)
                        .padding(bottom = 8.dp)
                        .background(skeletonColor)
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(skeletonColor)
                )
            }
        }
    }
}

@Composable
private fun MultiFab(
    items: List<FabButtonItem>,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (isExpanded) {
            items.forEach { item ->
                ExtendedFloatingActionButton(
                    onClick = {
                        item.action()
                        isExpanded = false
                    },
                    icon = { Icon(item.icon, contentDescription = item.label) },
                    text = { Text(item.label) },
                    containerColor = FastTimesTheme.accentColor,
                    contentColor = contentColorFor(backgroundColor = FastTimesTheme.accentColor),
                )
            }
        }

        FloatingActionButton(
            onClick = { isExpanded = !isExpanded },
            modifier = Modifier.align(Alignment.End),
            containerColor = FastTimesTheme.accentColor
        ) {
            Icon(
                //imageVector = Icons.Default.Add,
                painter = painterResource(id = R.drawable.ic_timer),
                contentDescription = "Add Fast",
                modifier = Modifier.size(24.dp),
                tint = contentColorFor(backgroundColor = FastTimesTheme.accentColor)
            )
        }
    }
}


@Composable
private fun RatingBar(rating: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        repeat(5) { index ->
            val icon = if (index < rating) Icons.Filled.Star else Icons.Outlined.StarBorder
            Icon(
                imageVector = icon,
                contentDescription = null, // decorative
                tint = if (index < rating) FastTimesTheme.accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun LastFastItem(
    fast: Fast,
    modifier: Modifier = Modifier
) {
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")
    val today = ZonedDateTime.now().toLocalDate()

    val durationString = if (fast.end != null) {
        val duration = Duration.between(fast.start, fast.end)
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        "${hours}h ${minutes}m"
    } else {
        "In progress"
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = durationString,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            if (fast.targetDuration != null && fast.targetDuration > 0) {
                Icon(
                    imageVector = if (fast.goalMet()) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                    contentDescription = if (fast.goalMet()) "Goal Reached" else "Goal Not Reached",
                    tint = if (fast.goalMet()) FastTimesTheme.accentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = 0.5f
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row {
                Text(
                    text = "Start:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(50.dp)
                )
                Text(
                    text = if (fast.start.toLocalDate() == today) {
                        fast.start.format(timeFormatter)
                    } else {
                        "${fast.start.format(timeFormatter)} - ${fast.start.format(dateFormatter)}"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    Text(
                        text = "End:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(50.dp)
                    )
                    Text(
                        text = fast.end?.let {
                            if (it.toLocalDate() == today) {
                                it.format(timeFormatter)
                            } else {
                                "${it.format(timeFormatter)} on ${it.format(dateFormatter)}"
                            }
                        } ?: "In progress",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (fast.rating != null) {
                    RatingBar(rating = fast.rating!!)
                }
            }
        }
    }
}

/**
 * Displays fasting profile options with a segmented button row for presets and
 * elevated cards for detailed profiles. Separates custom profile creation as a distinct action.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalSelectionSection(
    profiles: List<FastingProfile>,
    onStartFast: (FastingProfile) -> Unit,
    onManageProfilesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Separate preset profiles from custom ones
    val presetProfiles = profiles.filter { it.displayName in listOf("16:8", "18:6", "20:4") }
    val customProfiles = profiles.filterNot { it.displayName in listOf("16:8", "18:6", "20:4") }

    var selectedPresetIndex by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Segmented Buttons for Preset Fasting Windows
        if (presetProfiles.isNotEmpty()) {
            Text(
                text = "Quick Select",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth(),
                space = 8.dp
            ) {
                presetProfiles.forEachIndexed { index, profile ->
                    SegmentedButton(
                        selected = selectedPresetIndex == index,
                        onClick = {
                            selectedPresetIndex = index
                            onStartFast(profile)
                        },
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = profile.displayName,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        // Elevated Cards for Detailed Fasting Profiles
        if (customProfiles.isNotEmpty() || profiles.isEmpty()) {
            if (presetProfiles.isNotEmpty()) {
                Text(
                    text = "More Options",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 2
            ) {
                customProfiles.forEach { profile ->
                    ElevatedCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onStartFast(profile) },
                        shape = MaterialTheme.shapes.large
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                if (profile.durationMinutes == 0L) {
                                    // Display infinity symbol for 'No Goal' profile
                                    Text(
                                        text = "∞",
                                        style = MaterialTheme.typography.displaySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    // Convert from milliseconds to hours
                                    val hours = profile.durationMinutes / 3_600_000
                                    Text(
                                        text = "${hours}h",
                                        style = MaterialTheme.typography.displaySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = profile.displayName,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }

                            // Favorite star indicator
                            if (profile.isFavorite) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = "Favorite",
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(20.dp),
                                    tint = FastTimesTheme.accentColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
