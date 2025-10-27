package com.fasttimes.ui.history

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.fasttimes.ui.components.StatisticTile
import com.fasttimes.ui.editfast.EditFastRoute
import com.fasttimes.ui.formatDuration
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onViewFastDetails: (Long) -> Unit,
    onBackClick: (() -> Unit)? = null,
    onSwipeBack: (() -> Unit)? = null,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState()

    if (uiState.editingFastId != null) {
        Dialog(onDismissRequest = viewModel::onEditFastDismissed) {
            EditFastRoute(
                onDismiss = viewModel::onEditFastDismissed, 
                fastId = uiState.editingFastId
            )
        }
    }

    val content = @Composable { p: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(p)
                .padding(16.dp)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { _, dragAmount ->
                        if (dragAmount > 50) { // Threshold for swipe right
                            onSwipeBack?.invoke()
                        }
                    }
                }
        ) {
            CalendarView(
                uiState = uiState,
                onPreviousMonth = viewModel::onPreviousMonth,
                onNextMonth = viewModel::onNextMonth,
                onDayClick = viewModel::onDayClick,
            )

            MonthlyStats(uiState = uiState, onViewFastDetails = onViewFastDetails)
        }

        if (uiState.selectedDay != null) {
            val selectedDate = uiState.selectedDate.withDayOfMonth(uiState.selectedDay!!)
            val timelineSegments = uiState.dailyTimelineSegments[uiState.selectedDay!!] ?: emptyList()

            ModalBottomSheet(
                onDismissRequest = viewModel::onDismissDetails,
                sheetState = sheetState
            ) {
                DailyFastDetailsSheet(
                    date = selectedDate,
                    fasts = uiState.selectedDayFasts,
                    timeline = timelineSegments,
                    onSwipeLeft = viewModel::onNextDay,
                    onSwipeRight = viewModel::onPreviousDay,
                    onEditClick = viewModel::onEditFast
                )
            }
        }
    }

    if (onBackClick != null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "History") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            content(paddingValues)
        }
    } else {
        content(PaddingValues(0.dp))
    }
}

@Composable
private fun MonthlyStats(
    uiState: HistoryUiState,
    onViewFastDetails: (Long) -> Unit
) {
    val monthFormatter = remember { DateTimeFormatter.ofPattern("MMMM", Locale.getDefault()) }

    Card(modifier = Modifier.padding(top = 16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${uiState.displayedMonth.format(monthFormatter)} Statistics",
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatisticTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.BarChart,
                    label = "Total Fasts",
                    value = uiState.totalFastsInMonth.toString(),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
                StatisticTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Star,
                    label = "Longest Fast",
                    value = uiState.longestFastInMonth?.let { formatDuration(it.duration().milliseconds) } ?: "-",
                    onClick = { uiState.longestFastInMonth?.id?.let(onViewFastDetails) },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}
