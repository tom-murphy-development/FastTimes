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

import com.tmdev.fasttimes.data.fast.Fast
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import java.time.LocalDate
import java.time.YearMonth

enum class DayStatus {
    GOAL_MET,
    GOAL_NOT_MET
}

data class HistoryUiState(
    val displayedMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedDay: Int? = null,
    val fastsByDay: ImmutableMap<LocalDate, List<Fast>> = persistentMapOf(),
    val dayStatusByDayOfMonth: ImmutableMap<Int, DayStatus> = persistentMapOf(),
    val dailyTimelineSegments: ImmutableMap<Int, List<TimelineSegment>> = persistentMapOf(),
    val selectedDayFasts: List<Fast> = emptyList(),
    val totalFastsInMonth: Int = 0,
    val longestFastInMonth: Fast? = null,
    val averageFastDurationInMonth: Long = 0L,
    val editingFastId: Long? = null,
    val firstDayOfWeek: String = "Sunday"
)
