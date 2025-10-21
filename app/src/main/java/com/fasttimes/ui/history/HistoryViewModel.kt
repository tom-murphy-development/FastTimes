/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may
 * obtain a copy of the License at
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
import com.fasttimes.data.UserData
import com.fasttimes.data.UserPreferencesRepository
import com.fasttimes.data.fast.Fast
import com.fasttimes.data.fast.FastDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val fastDao: FastDao,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _isDetailsSheetShown = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HistoryUiState> =
        _selectedDate.flatMapLatest { date ->
            val monthStart = date.withDayOfMonth(1).atStartOfDay().toInstant(ZoneOffset.UTC)
            val monthEnd =
                date.plusMonths(1).withDayOfMonth(1).atStartOfDay().toInstant(ZoneOffset.UTC)

            val fastsForSelectedMonth = fastDao.getFastsForDay(
                dayStart = monthStart.toEpochMilli(),
                dayEnd = monthEnd.toEpochMilli()
            )

            combine(
                fastsForSelectedMonth,
                _isDetailsSheetShown,
                userPreferencesRepository.userData
            ) { fasts: List<Fast>, isSheetShown: Boolean, userData: UserData ->
                toHistoryUiState(date, fasts, userData, isSheetShown)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryUiState()
        )

    fun onPreviousMonth() {
        _selectedDate.value = _selectedDate.value.minusMonths(1)
        _isDetailsSheetShown.value = false
    }

    fun onNextMonth() {
        _selectedDate.value = _selectedDate.value.plusMonths(1)
        _isDetailsSheetShown.value = false
    }

    fun onDayClick(day: Int) {
        _selectedDate.value = _selectedDate.value.withDayOfMonth(day)
        _isDetailsSheetShown.value = true
    }

    fun onDismissDetails() {
        _isDetailsSheetShown.value = false
    }

    private fun toHistoryUiState(
        date: LocalDate,
        fasts: List<Fast>,
        userData: UserData,
        isSheetShown: Boolean
    ): HistoryUiState {
        val fastingGoal = userData.fastingGoal.seconds

        // 1. Pre-process fasts into a map for efficient O(1) lookup by day.
        val fastsByDay = mutableMapOf<Int, MutableList<Fast>>()
        for (fast in fasts) {
            val fastStartInstant = Instant.ofEpochMilli(fast.startTime)
            val fastEndInstant = fast.endTime?.let { Instant.ofEpochMilli(it) } ?: Instant.now()

            val fastStartDate = LocalDate.ofInstant(fastStartInstant, ZoneOffset.UTC)
            val fastEndDate = LocalDate.ofInstant(fastEndInstant, ZoneOffset.UTC)

            // Clamp the date range to the currently visible month.
            val startDay = if (fastStartDate.monthValue == date.monthValue && fastStartDate.year == date.year) {
                fastStartDate.dayOfMonth
            } else {
                1 // Fast starts before the beginning of this month.
            }

            val endDay = if (fastEndDate.monthValue == date.monthValue && fastEndDate.year == date.year) {
                fastEndDate.dayOfMonth
            } else {
                date.month.length(date.isLeapYear) // Fast ends after this month.
            }

            // Add the fast to the map for each day that it overlaps with.
            for (day in startDay..endDay) {
                fastsByDay.getOrPut(day) { mutableListOf() }.add(fast)
            }
        }


        val dayStatusByDayOfMonth = mutableMapOf<Int, DayStatus>()
        val dailyTimelineSegments = mutableMapOf<Int, List<TimelineSegment>>()

        // 2. Efficiently calculate status and timelines using the pre-processed map.
        fastsByDay.forEach { (dayOfMonth, fastsForDay) ->
            val currentDay = date.withDayOfMonth(dayOfMonth)
            val dayStart = currentDay.atStartOfDay().toInstant(ZoneOffset.UTC)
            val dayEnd = currentDay.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)

            val totalFastDurationForDay = fastsForDay.sumOf { fast ->
                calculateDurationForDay(fast, dayStart, dayEnd)
            }

            if (totalFastDurationForDay > 0) {
                dayStatusByDayOfMonth[dayOfMonth] =
                    if (totalFastDurationForDay >= fastingGoal) DayStatus.GOAL_MET else DayStatus.GOAL_NOT_MET
            }

            dailyTimelineSegments[dayOfMonth] =
                createTimelineSegments(fastsForDay, dayStart, dayEnd, fastingGoal)
        }

        return HistoryUiState(
            selectedDate = date,
            selectedDay = if (isSheetShown) date.dayOfMonth else null,
            dayStatusByDayOfMonth = dayStatusByDayOfMonth,
            dailyTimelineSegments = dailyTimelineSegments,
            // 3. Get the selected day's fasts with an efficient map lookup.
            selectedDayFasts = if (isSheetShown) fastsByDay[date.dayOfMonth] ?: emptyList() else emptyList()
        )
    }

    private fun calculateDurationForDay(
        fast: Fast,
        dayStart: Instant,
        dayEnd: Instant
    ): Long {
        val fastStart = Instant.ofEpochMilli(fast.startTime)
        val fastEnd = fast.endTime?.let { Instant.ofEpochMilli(it) } ?: Instant.now()

        val start = if (fastStart.isBefore(dayStart)) dayStart else fastStart
        val end = if (fastEnd.isAfter(dayEnd)) dayEnd else fastEnd

        return if (start.isBefore(end)) {
            Duration.between(start, end).seconds
        } else {
            0
        }
    }

    private fun createTimelineSegments(
        fasts: List<Fast>,
        dayStart: Instant,
        dayEnd: Instant,
        fastingGoal: Long
    ): List<TimelineSegment> {
        val dayDuration = Duration.between(dayStart, dayEnd).toMillis()
        if (dayDuration == 0L) return emptyList()

        return fasts.map { fast ->
            val fastStart = Instant.ofEpochMilli(fast.startTime)
            val fastEnd = fast.endTime?.let { Instant.ofEpochMilli(it) } ?: Instant.now()

            val segmentStart = if (fastStart.isBefore(dayStart)) dayStart else fastStart
            val segmentEnd = if (fastEnd.isAfter(dayEnd)) dayEnd else fastEnd

            val segmentDuration = Duration.between(segmentStart, segmentEnd).toMillis()
            val weight = (segmentDuration.toFloat() / dayDuration.toFloat()).coerceIn(0f, 1f)

            val duration = (fast.endTime ?: System.currentTimeMillis()) - fast.startTime
            val goalReached = Duration.ofMillis(duration).seconds >= fastingGoal

            TimelineSegment(
                color = if (goalReached) Color.Green else Color.Blue,
                weight = weight
            )
        }
    }
}
