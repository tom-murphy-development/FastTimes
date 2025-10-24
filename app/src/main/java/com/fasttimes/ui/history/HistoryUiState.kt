package com.fasttimes.ui.history

import com.fasttimes.data.fast.Fast
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
    val selectedDayFasts: List<Fast> = emptyList()
)
