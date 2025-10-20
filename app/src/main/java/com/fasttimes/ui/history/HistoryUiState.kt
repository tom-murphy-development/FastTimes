package com.fasttimes.ui.history

import com.fasttimes.data.fast.Fast
import java.time.LocalDate

enum class DayStatus {
    GOAL_MET,
    GOAL_NOT_MET
}

data class HistoryUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedDay: Int? = null,
    val fastsByDay: Map<LocalDate, List<Fast>> = emptyMap(),
    val dayStatusByDayOfMonth: Map<Int, DayStatus> = emptyMap(),
    val dailyTimelineSegments: Map<Int, List<TimelineSegment>> = emptyMap(),
    val selectedDayFasts: List<Fast> = emptyList()
)
