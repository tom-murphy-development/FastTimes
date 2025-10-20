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

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasttimes.data.fast.Fast
import com.fasttimes.data.fast.FastDao
import com.fasttimes.data.toLocalDateTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.Duration
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    fastDao: FastDao
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _selectedDay = MutableStateFlow<Int?>(null)

    val uiState: StateFlow<HistoryUiState> =
        combine(
            _selectedDate,
            _selectedDay,
            fastDao.getAllFasts()
        ) { selectedDate, selectedDay, fasts ->
            val fastsByDay = fasts
                .filter { it.endTime != null }
                .groupBy { it.endTime!!.toLocalDateTime().toLocalDate() }

            val dayStatusByDayOfMonth = fasts
                .filter { it.endTime != null && it.endTime.toLocalDateTime().month == selectedDate.month }
                .groupBy { it.endTime!!.toLocalDateTime().toLocalDate() }
                .mapValues { (_, fastsOnDay) ->
                    if (fastsOnDay.any { it.goalReached() }) {
                        DayStatus.GOAL_MET
                    } else {
                        DayStatus.GOAL_NOT_MET
                    }
                }
                .mapKeys { it.key.dayOfMonth }


            val dailyTimelineSegments = processFastsForDailyTimeline(fasts, selectedDate)

            val selectedDayFasts = if (selectedDay != null) {
                fastsByDay[selectedDate.withDayOfMonth(selectedDay)] ?: emptyList()
            } else {
                emptyList()
            }

            HistoryUiState(
                selectedDate = selectedDate,
                selectedDay = selectedDay,
                fastsByDay = fastsByDay,
                dayStatusByDayOfMonth = dayStatusByDayOfMonth,
                dailyTimelineSegments = dailyTimelineSegments,
                selectedDayFasts = selectedDayFasts
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryUiState()
        )

    fun onPreviousMonth() {
        _selectedDate.value = _selectedDate.value.minusMonths(1)
    }

    fun onNextMonth() {
        _selectedDate.value = _selectedDate.value.plusMonths(1)
    }

    fun onDaySelected(day: Int) {
        _selectedDay.value = day
    }

    fun onDismissDetails() {
        _selectedDay.value = null
    }

    private fun processFastsForDailyTimeline(
        fasts: List<Fast>,
        currentMonth: LocalDate
    ): Map<Int, List<TimelineSegment>> {
        val segmentsByDay = mutableMapOf<Int, List<TimelineSegment>>()
        val daysInMonth = currentMonth.lengthOfMonth()

        val relevantFasts = fasts.filter { it.endTime != null }

        for (dayOfMonth in 1..daysInMonth) {
            val dayStart = currentMonth.withDayOfMonth(dayOfMonth).atStartOfDay()

            val minuteTimeline = BooleanArray(1440) {
                val minuteTime = dayStart.plusMinutes(it.toLong())
                relevantFasts.any { fast ->
                    val fastStart = fast.startTime.toLocalDateTime()
                    val fastEnd = fast.endTime!!.toLocalDateTime()
                    !minuteTime.isBefore(fastStart) && minuteTime.isBefore(fastEnd)
                }
            }

            val segments = mutableListOf<TimelineSegment>()
            if (minuteTimeline.any { it }) { // If there's any fasting
                var currentSegmentStart = 0
                var currentSegmentState = minuteTimeline[0]
                for (i in 1 until 1440) {
                    if (minuteTimeline[i] != currentSegmentState) {
                        val duration = (i - currentSegmentStart).toFloat()
                        segments.add(
                            TimelineSegment(
                                color = if (currentSegmentState) Color.Green else Color.Gray,
                                weight = duration
                            )
                        )
                        currentSegmentStart = i
                        currentSegmentState = minuteTimeline[i]
                    }
                }
                // Add the last segment
                segments.add(
                    TimelineSegment(
                        color = if (currentSegmentState) Color.Green else Color.Gray,
                        weight = (1440 - currentSegmentStart).toFloat()
                    )
                )

                // Normalize
                val totalWeight = segments.sumOf { it.weight.toDouble() }.toFloat()
                if (totalWeight > 0) {
                    segmentsByDay[dayOfMonth] = segments.map { it.copy(weight = it.weight / totalWeight) }
                }
            }
        }
        return segmentsByDay
    }

    private fun Fast.goalReached(): Boolean {
        if (endTime == null || targetDuration == null) return false
        val duration = Duration.between(
            startTime.toLocalDateTime(),
            endTime.toLocalDateTime()
        ).seconds
        return duration >= targetDuration
    }
}
