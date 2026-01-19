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
package com.fasttimes.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasttimes.data.fast.Fast
import com.fasttimes.data.fast.FastsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Data class representing the current fasting streak.
 */
data class FastingStreak(
    val daysInARow: Int = 0,
    val startDate: LocalDate? = null,
    val lastFastDate: LocalDate? = null
)

/**
 * Data class representing the trend in fasting activity.
 */
data class FastingTrend(
    val currentMonthFasts: Int = 0,
    val previousMonthFasts: Int = 0,
    val percentageChange: Float = 0f,
    val isUpward: Boolean = false
)

/**
 * Data class representing daily fasting activity for the chart.
 */
data class DailyActivity(
    val dayOfWeek: DayOfWeek,
    val durationHours: Float,
    val isGoalMet: Boolean
)

/**
 * UI state data class containing all statistics for the Statistics screen.
 */
data class StatisticsUiState(
    val streak: FastingStreak = FastingStreak(),
    val averageFast: Duration = Duration.ZERO,
    val trend: FastingTrend = FastingTrend(),
    val weeklyActivity: List<DailyActivity> = emptyList(),
    val weeklyGoals: Set<Float> = emptySet(),
    val totalFasts: Int = 0,
    val totalFastingTime: Duration = Duration.ZERO,
    val longestFast: Fast? = null,
    val fastsPerWeek: Float = 0f,
    val firstFastDate: LocalDate? = null,
    val averageStartTime: LocalTime? = null,
    val mostFrequentDay: DayOfWeek? = null,
    val isLoading: Boolean = true
)

/**
 * ViewModel for the Statistics screen.
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val fastsRepository: FastsRepository
) : ViewModel() {

    val statisticsState: StateFlow<StatisticsUiState> = fastsRepository.getFasts()
        .map { fasts ->
            val completedFasts = fasts.filter { it.endTime != null }
            val streak = calculateStreak(completedFasts)
            val averageFast = calculateAverageFast(completedFasts)
            val trend = calculateTrend(completedFasts)
            val weeklyActivity = calculateWeeklyActivity(completedFasts)
            
            // Extract unique goals from fasts in the current week
            val today = LocalDate.now()
            val startOfWeek = today.minusDays(6)
            val weeklyGoals = completedFasts
                .filter { it.start.toLocalDate() >= startOfWeek }
                .mapNotNull { it.targetDuration?.let { target -> target.toFloat() / 3600000f } }
                .toSet()

            val totalFastingTime = completedFasts.sumOf { it.duration() }.milliseconds
            val longestFast = fasts.maxByOrNull { it.duration() }
            
            val firstFast = completedFasts.minByOrNull { it.startTime }
            val firstFastDate = firstFast?.start?.toLocalDate()
            
            val fastsPerWeek = if (firstFastDate != null) {
                val daysSinceFirstFast = ChronoUnit.DAYS.between(firstFastDate, LocalDate.now()).coerceAtLeast(1)
                (completedFasts.size.toFloat() / daysSinceFirstFast) * 7
            } else 0f

            val averageStartTime = calculateAverageStartTime(completedFasts)
            val mostFrequentDay = calculateMostFrequentDay(completedFasts)

            StatisticsUiState(
                streak = streak,
                averageFast = averageFast,
                trend = trend,
                weeklyActivity = weeklyActivity,
                weeklyGoals = weeklyGoals,
                totalFasts = fasts.size,
                totalFastingTime = totalFastingTime,
                longestFast = longestFast,
                fastsPerWeek = fastsPerWeek,
                firstFastDate = firstFastDate,
                averageStartTime = averageStartTime,
                mostFrequentDay = mostFrequentDay,
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StatisticsUiState(isLoading = true)
        )

    private fun calculateStreak(completedFasts: List<Fast>): FastingStreak {
        if (completedFasts.isEmpty()) return FastingStreak()

        val fastDates = completedFasts
            .mapNotNull { it.end?.toLocalDate() }
            .distinct()
            .sorted()

        if (fastDates.isEmpty()) return FastingStreak()

        val today = LocalDate.now()
        var currentDate = today
        var streakDays = 0

        while (true) {
            when {
                currentDate in fastDates -> {
                    streakDays++
                    currentDate = currentDate.minusDays(1)
                }
                currentDate == today && fastDates.isNotEmpty() -> {
                    currentDate = currentDate.minusDays(1)
                }
                else -> break
            }
        }

        if (streakDays == 0 && fastDates.isNotEmpty()) {
            for (date in fastDates.reversed()) {
                currentDate = date
                streakDays = 1
                var checkDate = date.minusDays(1)
                while (checkDate in fastDates) {
                    streakDays++
                    checkDate = checkDate.minusDays(1)
                }
                break
            }
        }

        val lastFastDate = fastDates.lastOrNull()
        val streakStartDate = if (streakDays > 0) {
            lastFastDate?.minusDays((streakDays - 1).toLong())
        } else null

        return FastingStreak(
            daysInARow = streakDays,
            startDate = streakStartDate,
            lastFastDate = lastFastDate
        )
    }

    private fun calculateAverageFast(completedFasts: List<Fast>): Duration {
        if (completedFasts.isEmpty()) return Duration.ZERO
        val totalDuration = completedFasts.sumOf { it.duration() }
        return (totalDuration / completedFasts.size).milliseconds
    }

    private fun calculateTrend(completedFasts: List<Fast>): FastingTrend {
        val currentMonth = YearMonth.now()
        val previousMonth = currentMonth.minusMonths(1)

        val systemZone = ZoneId.systemDefault()
        val currentMonthStart = currentMonth.atDay(1).atStartOfDay(systemZone)
        val currentMonthEnd = currentMonth.atEndOfMonth().plusDays(1).atStartOfDay(systemZone)
        previousMonth.atDay(1).atStartOfDay(systemZone)
        val previousMonthEnd = previousMonth.atEndOfMonth().plusDays(1).atStartOfDay(systemZone)

        val currentMonthFasts = completedFasts.count { fast ->
            fast.end?.isBefore(currentMonthEnd) == true && fast.end?.isAfter(currentMonthStart) == true
        }
        val previousMonthFasts = completedFasts.count { fast ->
            fast.end?.isBefore(previousMonthEnd) == true && fast.end?.isAfter(currentMonthStart.minusMonths(1)) == true
        }

        val percentageChange = when {
            previousMonthFasts > 0 -> ((currentMonthFasts - previousMonthFasts).toFloat() / previousMonthFasts) * 100
            currentMonthFasts > 0 -> 100f
            else -> 0f
        }

        return FastingTrend(
            currentMonthFasts = currentMonthFasts,
            previousMonthFasts = previousMonthFasts,
            percentageChange = percentageChange,
            isUpward = currentMonthFasts >= previousMonthFasts
        )
    }

    private fun calculateWeeklyActivity(completedFasts: List<Fast>): List<DailyActivity> {
        val today = LocalDate.now()
        val startOfWeek = today.minusDays(6)
        
        return (0..6).map { i ->
            val date = startOfWeek.plusDays(i.toLong())
            val fastsOnDate = completedFasts.filter { it.start.toLocalDate() == date }
            val totalDurationHours = fastsOnDate.sumOf { it.duration() }.toFloat() / 3600000f
            
            // Check if any fast on this date met its target goal
            val goalMet = fastsOnDate.any { it.goalMet() }
            
            DailyActivity(
                dayOfWeek = date.dayOfWeek,
                durationHours = totalDurationHours,
                isGoalMet = goalMet
            )
        }
    }

    private fun calculateAverageStartTime(completedFasts: List<Fast>): LocalTime? {
        if (completedFasts.isEmpty()) return null
        
        val totalSeconds = completedFasts.sumOf { fast ->
            val startTime = fast.start.toLocalTime()
            startTime.toSecondOfDay().toLong()
        }
        
        val averageSeconds = totalSeconds / completedFasts.size
        return LocalTime.ofSecondOfDay(averageSeconds)
    }

    private fun calculateMostFrequentDay(completedFasts: List<Fast>): DayOfWeek? {
        if (completedFasts.isEmpty()) return null
        
        return completedFasts
            .map { it.start.dayOfWeek }
            .groupBy { it }
            .maxByOrNull { it.value.size }
            ?.key
    }
}
