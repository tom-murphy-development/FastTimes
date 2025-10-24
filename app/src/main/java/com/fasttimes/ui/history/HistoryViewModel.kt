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
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    fastsRepository: FastsRepository,
) : ViewModel() {

    private val _displayedMonth = MutableStateFlow(YearMonth.now())
    private val _selectedDay = MutableStateFlow<Int?>(null)

    /**
     * A data class to hold all calculated data for a given month.
     * This avoids re-calculating data multiple times downstream.
     */
    private data class HistoryMonth(
        val yearMonth: YearMonth,
        val fastsInMonth: List<Fast>,
    ) {
        /**
         * A map of day-of-month to a list of fasts that occurred on that day.
         */
        val fastsByDay: Map<Int, List<Fast>> = run {
            val dailyFasts = mutableMapOf<Int, MutableList<Fast>>()
            val selectedDate = yearMonth.atDay(1)

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
            val date = yearMonth.atDay(1)
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
        _displayedMonth,
        fastsRepository.getFasts()
    ) { month, fasts ->
        val zone = ZoneId.systemDefault()
        val monthStart = month.atDay(1).atStartOfDay(zone)
        val monthEnd = monthStart.plusMonths(1)

        val fastsForMonth = fasts.filter {
            val fastEnd = it.end
            it.start.isBefore(monthEnd) && (fastEnd == null || fastEnd.isAfter(monthStart))
        }
        HistoryMonth(month, fastsForMonth)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryMonth(YearMonth.now(), emptyList())
    )

    /**
     * The UI state for the History screen, derived from combining all data sources.
     */
    val uiState: StateFlow<HistoryUiState> = combine(
        historyMonth,
        _displayedMonth,
        _selectedDay,
    ) { month, displayedMonth, selectedDay ->
        val fastsForSelectedDay = if (selectedDay != null) {
            month.fastsByDay[selectedDay] ?: emptyList()
        } else {
            emptyList()
        }

        HistoryUiState(
            displayedMonth = displayedMonth,
            selectedDate = displayedMonth.atDay(1).withDayOfMonth(selectedDay ?: 1),
            selectedDay = selectedDay,
            dayStatusByDayOfMonth = month.dayStatusByDayOfMonth.toImmutableMap(),
            dailyTimelineSegments = month.dailyTimelineSegments.toImmutableMap(),
            selectedDayFasts = fastsForSelectedDay
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState(),
    )

    fun onPreviousMonth() {
        _displayedMonth.value = _displayedMonth.value.minusMonths(1)
    }

    fun onNextMonth() {
        val nextMonth = _displayedMonth.value.plusMonths(1)
        if (nextMonth.isAfter(YearMonth.now())) return
        _displayedMonth.value = nextMonth
    }

    fun onPreviousDay() {
        val day = _selectedDay.value
        if (day != null) {
            val displayedMonth = _displayedMonth.value
            if (day > 1) {
                _selectedDay.value = day - 1
            } else {
                val previousMonth = displayedMonth.minusMonths(1)
                _displayedMonth.value = previousMonth
                _selectedDay.value = previousMonth.lengthOfMonth()
            }
        }
    }

    fun onNextDay() {
        val day = _selectedDay.value
        val displayedMonth = _displayedMonth.value
        if (day != null) {
            val nextDate = displayedMonth.atDay(day).plusDays(1)
            if (nextDate.isAfter(LocalDate.now())) {
                return // Don't go to the future
            }

            if (day < displayedMonth.lengthOfMonth()) {
                _selectedDay.value = day + 1
            } else {
                val nextMonth = displayedMonth.plusMonths(1)
                if (nextMonth.isAfter(YearMonth.now())) return
                _displayedMonth.value = nextMonth
                _selectedDay.value = 1
            }
        }
    }

    fun onDayClick(day: Int) {
        if (_selectedDay.value == day) {
            _selectedDay.value = null
        } else {
            _selectedDay.value = day
            _displayedMonth.value = YearMonth.from(uiState.value.displayedMonth.atDay(day))
        }
    }

    fun onDismissDetails() {
        _selectedDay.value = null
    }
}
