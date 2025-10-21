/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fasttimes.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fasttimes.ui.theme.FastTimesTheme
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun CalendarView(
    uiState: HistoryUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDayClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentMonth = uiState.selectedDate
    val daysOfWeek = listOf("M", "T", "W", "T", "F", "S", "S")

    val nextCalendarMonth = YearMonth.from(currentMonth).plusMonths(1)
    val systemCalendarMonth = YearMonth.now()
    val isNextMonthEnabled = !nextCalendarMonth.isAfter(systemCalendarMonth)


    Column(modifier = modifier.padding(16.dp)) {
        CalendarHeader(
            monthTitle = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            onPreviousClick = onPreviousMonth,
            onNextClick = onNextMonth,
            isNextEnabled = isNextMonthEnabled
        )
        Spacer(modifier = Modifier.height(16.dp))

        DayOfWeekHeader(daysOfWeek = daysOfWeek)
        Spacer(modifier = Modifier.height(8.dp))

        CalendarGrid(
            currentMonth = currentMonth,
            uiState = uiState,
            onDayClick = onDayClick
        )
    }
}

@Composable
private fun CalendarHeader(
    monthTitle: String,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    isNextEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous Month"
            )
        }
        Text(
            text = monthTitle,
            style = MaterialTheme.typography.titleLarge,
        )
        IconButton(onClick = onNextClick, enabled = isNextEnabled) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next Month"
            )
        }
    }
}

@Composable
private fun DayOfWeekHeader(daysOfWeek: List<String>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        daysOfWeek.forEach { day ->
            Text(
                text = day,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    currentMonth: LocalDate,
    uiState: HistoryUiState,
    onDayClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val firstDayOfMonth = currentMonth.withDayOfMonth(1)
    val firstDayOfWeekIndex = (firstDayOfMonth.dayOfWeek.value - 1)
    val daysInMonth = currentMonth.lengthOfMonth()

    val dayCells = (1..daysInMonth).toList()
    val emptyCells = List(firstDayOfWeekIndex) { null }
    val allCells = emptyCells + dayCells

    Column(modifier = modifier) {
        allCells.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                week.forEach { day ->
                    DayCell(
                        day = day,
                        currentMonth = currentMonth,
                        isSelected = uiState.selectedDay == day,
                        status = day?.let { uiState.dayStatusByDayOfMonth[it] },
                        segments = day?.let { uiState.dailyTimelineSegments[it] } ?: emptyList(),
                        onDayClick = { onDayClick(it) }
                    )
                }
                if (week.size < 7) {
                    for (i in 1..(7 - week.size)) {
                        Box(modifier = Modifier.weight(1f).height(60.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun RowScope.DayCell(
    day: Int?,
    currentMonth: LocalDate,
    isSelected: Boolean,
    status: DayStatus?,
    segments: List<TimelineSegment>,
    onDayClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isFutureDay = day != null && currentMonth.withDayOfMonth(day).isAfter(LocalDate.now())
    Column(
        modifier = modifier
            .weight(1f)
            .height(60.dp)
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
            .clickable(enabled = day != null && !isFutureDay) { day?.let { onDayClick(it) } },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (day != null) {
            val textColor = if (isFutureDay) {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            } else {
                MaterialTheme.colorScheme.onSurface
            }
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = textColor
            )

            if (segments.isNotEmpty() && !isFutureDay) {
                WeeklyTimeline(segments = segments, modifier = Modifier.height(4.dp).padding(horizontal = 2.dp))
            } else {
                Spacer(modifier = Modifier.height(4.dp))
            }

            val iconColor = when (status) {
                DayStatus.GOAL_MET -> Color.Green
                DayStatus.GOAL_NOT_MET -> Color.Gray
                null -> Color.Transparent
            }
            if (status != null && !isFutureDay) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(iconColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = if (status == DayStatus.GOAL_MET) "Goal Met" else "Goal Not Met",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CalendarViewPreview() {
    FastTimesTheme {
        CalendarView(
            uiState = HistoryUiState(
                dayStatusByDayOfMonth = mapOf(1 to DayStatus.GOAL_MET, 5 to DayStatus.GOAL_NOT_MET, 10 to DayStatus.GOAL_MET, 23 to DayStatus.GOAL_NOT_MET),
                dailyTimelineSegments = mapOf(
                    1 to listOf(TimelineSegment(Color.Green, 1f)),
                    5 to listOf(TimelineSegment(Color.Green, 0.5f), TimelineSegment(Color.Gray, 0.5f)),
                    10 to listOf(TimelineSegment(Color.Gray, 0.2f), TimelineSegment(Color.Green, 0.8f)),
                    23 to listOf(TimelineSegment(Color.Green, 1f)),
                )
            ),
            onPreviousMonth = {},
            onNextMonth = {},
            onDayClick = {}
        )
    }
}
