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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasttimes.data.fast.Fast
import com.fasttimes.data.fast.FastsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    fastsRepository: FastsRepository,
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _selectedDay = MutableStateFlow<Int?>(null)

    /**
     * A data class to hold all calculated data for a given month.
     * This avoids re-calculating data multiple times downstream.
     */
    private data class HistoryMonth(
        val year: Int,
        val month: Int,
        val fastsInMonth: List<Fast>,
    ) {
        /**
         * A map of day-of-month to a list of fasts that occurred on that day.
         */
        val fastsByDay: Map<Int, List<Fast>> = run {
            val dailyFasts = mutableMapOf<Int, MutableList<Fast>>()
            val selectedDate = LocalDate.of(year, month, 1)

            for (fast in fastsInMonth) {
                var currentFastDate = fast.start.toLocalDate()
                val endFastDate = (fast.end ?: ZonedDateTime.now()).toLocalDate()

                while (!currentFastDate.isAfter(endFastDate)) {
                    if (currentFastDate.year == selectedDate.year && currentFastDate.month == selectedDate.month) {
                        dailyFasts.computeIfAbsent(currentFastDate.dayOfMonth) { mutableListOf() }
                            .add(fast)
                    }
                    if (currentFastDate.year > selectedDate.year ||
                        (currentFastDate.year == selectedDate.year && currentFastDate.month > selectedDate.month)
                    ) {
                        break
                    }
                    currentFastDate = currentFastDate.plusDays(1)
                }
            }
            dailyFasts
        }

        /**
         * A map of day-of-month to its [DayStatus] (goal met or not).
         */
        val dayStatusByDayOfMonth: Map<Int, DayStatus> = fastsByDay.mapValues { (_, fastsOnDay) ->
            if (fastsOnDay.any { it.goalMet() }) DayStatus.GOAL_MET else DayStatus.GOAL_NOT_MET
        }

        /**
         * A map of day-of-month to its [TimelineSegment] list for the calendar view.
         */
        val dailyTimelineSegments: Map<Int, List<TimelineSegment>> = run {
            val date = LocalDate.of(year, month, 1)
            (1..date.lengthOfMonth()).associateWith { dayOfMonth ->
                val day = date.withDayOfMonth(dayOfMonth)
                generateTimelineSegments(day, fastsInMonth)
            }
        }
    }

    /**
     * A flow that emits a [HistoryMonth] object whenever the selected month or the underlying
     * fasts data changes. This is the single source of truth for all monthly calendar data.
     */
    private val historyMonth: StateFlow<HistoryMonth> = combine(
        _selectedDate,
        fastsRepository.getFasts()
    ) { date, fasts ->
        val zone = ZoneId.systemDefault()
        val monthStart = date.withDayOfMonth(1).atStartOfDay(zone)
        val monthEnd = monthStart.plusMonths(1)

        val fastsForMonth = fasts.filter {
            val fastEnd = it.end
            it.start.isBefore(monthEnd) && (fastEnd == null || fastEnd.isAfter(monthStart))
        }
        HistoryMonth(date.year, date.monthValue, fastsForMonth)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryMonth(LocalDate.now().year, LocalDate.now().monthValue, emptyList())
    )

    /**
     * The UI state for the History screen, derived from combining all data sources.
     */
    val uiState: StateFlow<HistoryUiState> = combine(
        historyMonth,
        _selectedDay,
    ) { month, selectedDay ->
        val fastsForSelectedDay = if (selectedDay != null) {
            month.fastsByDay[selectedDay] ?: emptyList()
        } else {
            emptyList()
        }

        HistoryUiState(
            selectedDate = LocalDate.of(month.year, month.month, 1),
            selectedDay = selectedDay,
            dayStatusByDayOfMonth = month.dayStatusByDayOfMonth,
            dailyTimelineSegments = month.dailyTimelineSegments,
            selectedDayFasts = fastsForSelectedDay
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState(),
    )

    fun onPreviousMonth() {
        _selectedDate.value = _selectedDate.value.minusMonths(1)
    }

    fun onNextMonth() {
        _selectedDate.value = _selectedDate.value.plusMonths(1)
    }

    fun onDayClick(day: Int) {
        if (_selectedDay.value == day) {
            _selectedDay.value = null
        } else {
            _selectedDay.value = day
        }
    }

    fun onDismissDetails() {
        _selectedDay.value = null
    }
}
