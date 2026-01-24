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
package com.fasttimes.ui.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasttimes.data.fast.Fast
import com.fasttimes.data.fast.FastsRepository
import com.fasttimes.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val fastsRepository: FastsRepository,
    settingsRepository: SettingsRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _displayedMonth = MutableStateFlow(YearMonth.now())
    private val _selectedDay = MutableStateFlow<Int?>(null)
    private val _editingFastId = MutableStateFlow<Long?>(null)


    init {
        viewModelScope.launch {
            val fastId: Long? = savedStateHandle.get<Long>("fastId")
            if (fastId != null && fastId != -1L) {
                val fast = fastsRepository.getFasts().first().find { it.id == fastId }
                if (fast != null) {
                    val fastDate = fast.start.withZoneSameInstant(ZoneId.systemDefault())
                    _displayedMonth.value = YearMonth.from(fastDate)
                    _selectedDay.value = fastDate.dayOfMonth
                }
            }
        }
    }

    private data class HistoryMonth(
        val yearMonth: YearMonth,
        val fastsInMonth: List<Fast>,
    ) {
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

        val dayStatusByDayOfMonth: Map<Int, DayStatus> = fastsByDay.mapValues { (_, fastsOnDay) ->
            if (fastsOnDay.any { it.goalMet() }) DayStatus.GOAL_MET else DayStatus.GOAL_NOT_MET
        }

        val dailyTimelineSegments: Map<Int, List<TimelineSegment>> = run {
            val date = yearMonth.atDay(1)
            (1..date.lengthOfMonth()).associateWith { dayOfMonth ->
                val day = date.withDayOfMonth(dayOfMonth)
                generateTimelineSegments(day, fastsInMonth)
            }
        }
    }

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

    val uiState: StateFlow<HistoryUiState> = combine(
        historyMonth,
        _displayedMonth,
        _selectedDay,
        _editingFastId,
        settingsRepository.firstDayOfWeek
    ) { month, displayedMonth, selectedDay, editingFastId, firstDayOfWeek ->
        val fastsForSelectedDay = if (selectedDay != null) {
            month.fastsByDay[selectedDay] ?: emptyList()
        } else {
            emptyList()
        }

        val averageDuration = if (month.fastsInMonth.isNotEmpty()) {
            month.fastsInMonth.map { it.duration() }.average().toLong()
        } else {
            0L
        }

        HistoryUiState(
            displayedMonth = displayedMonth,
            selectedDate = displayedMonth.atDay(1).withDayOfMonth(selectedDay ?: 1),
            selectedDay = selectedDay,
            dayStatusByDayOfMonth = month.dayStatusByDayOfMonth.toImmutableMap(),
            dailyTimelineSegments = month.dailyTimelineSegments.toImmutableMap(),
            selectedDayFasts = fastsForSelectedDay,
            totalFastsInMonth = month.fastsInMonth.size,
            longestFastInMonth = month.fastsInMonth.maxByOrNull { it.duration() },
            averageFastDurationInMonth = averageDuration,
            editingFastId = editingFastId,
            firstDayOfWeek = firstDayOfWeek
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

    fun onEditFast(fastId: Long) {
        _editingFastId.value = fastId
    }

    fun onEditFastDismissed() {
        _editingFastId.value = null
    }
}
