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
package com.tmdev.fasttimes.ui.history

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.tmdev.fasttimes.ui.components.ExpressiveStatCard
import com.tmdev.fasttimes.ui.components.rememberRandomExpressiveShape
import com.tmdev.fasttimes.ui.editfast.EditFastRoute
import java.time.format.DateTimeFormatter
import java.util.Locale

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

            MonthlyStats(uiState = uiState)
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MonthlyStats(
    uiState: HistoryUiState
) {
    val monthFormatter = remember { DateTimeFormatter.ofPattern("MMMM", Locale.getDefault()) }
    
    // Stable random seeds for shapes based on the displayed month
    val monthSeed = remember(uiState.displayedMonth) { 
        uiState.displayedMonth.year * 12 + uiState.displayedMonth.monthValue 
    }
    
    val shape1 = rememberRandomExpressiveShape(seed = monthSeed)
    val shape2 = rememberRandomExpressiveShape(seed = monthSeed + 1)
    val shape3 = rememberRandomExpressiveShape(seed = monthSeed + 2)

    Column(modifier = Modifier.padding(top = 24.dp)) {
        Text(
            text = "${uiState.displayedMonth.format(monthFormatter)} Summary",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExpressiveStatCard(
                modifier = Modifier.weight(1f),
                label = "Total Fasts",
                value = uiState.totalFastsInMonth.toString(),
                unit = "completed",
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = shape1,
                height = 140.dp
            )
            
            ExpressiveStatCard(
                modifier = Modifier.weight(1f),
                label = "Longest Fast",
                value = uiState.longestFastInMonth?.let { formatHoursOnly(it.duration()) } ?: "-",
                unit = "this month",
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                shape = shape2,
                height = 140.dp
            )
            
            ExpressiveStatCard(
                modifier = Modifier.weight(1f),
                label = "Average",
                value = formatHoursOnly(uiState.averageFastDurationInMonth),
                unit = "duration",
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                shape = shape3,
                height = 140.dp
            )
        }
    }
}

private fun formatHoursOnly(durationMillis: Long): String {
    val totalHours = durationMillis / (1000f * 60 * 60)
    return String.format(Locale.getDefault(), "%.1fh", totalHours)
}
