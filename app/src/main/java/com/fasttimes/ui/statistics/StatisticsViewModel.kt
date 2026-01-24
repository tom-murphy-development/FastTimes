/*
 * Copyright (C) 2025 tom-murphy-development
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
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
    val currentCount: Int = 0,
    val previousCount: Int = 0,
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
    val weeklyAverageFast: Duration = Duration.ZERO,
    val trend: FastingTrend = FastingTrend(),
    val weeklyActivity: List<DailyActivity> = emptyList(),
    val weeklyGoals: Set<Float> = emptySet(),
    val totalFasts: Int = 0,
    val totalFastingTime: Duration = Duration.ZERO,
    val weeklyFastingTime: Duration = Duration.ZERO,
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
            val trend = calculateVelocity(completedFasts)
            val weeklyActivity = calculateWeeklyActivity(completedFasts)
            
            val today = LocalDate.now()
            val startOfWeek = today.minusDays(6)
            
            // Extract unique goals from fasts in the current week
            val weeklyGoals = completedFasts
                .filter { it.start.toLocalDate() >= startOfWeek }
                .mapNotNull { it.targetDuration?.let { target -> target.toFloat() / 3600000f } }
                .toSet()

            val totalFastingTime = completedFasts.sumOf { it.duration() }.milliseconds
            
            // Calculate total fasting time for the current week
            val weeklyFasts = completedFasts.filter { it.start.toLocalDate() >= startOfWeek }
            val weeklyFastingTime = weeklyFasts.sumOf { it.duration() }.milliseconds
            val weeklyAverageFast = calculateAverageFast(weeklyFasts)

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
                weeklyAverageFast = weeklyAverageFast,
                trend = trend,
                weeklyActivity = weeklyActivity,
                weeklyGoals = weeklyGoals,
                totalFasts = fasts.size,
                totalFastingTime = totalFastingTime,
                weeklyFastingTime = weeklyFastingTime,
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

    private fun calculateVelocity(completedFasts: List<Fast>): FastingTrend {
        val now = ZonedDateTime.now()
        val startOfThisWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toLocalDate().atStartOfDay(now.zone)
        val startOfLastWeek = startOfThisWeek.minusWeeks(1)

        val thisWeekCount = completedFasts.count { it.start.isAfter(startOfThisWeek) }
        val lastWeekCount = completedFasts.count { it.start.isAfter(startOfLastWeek) && it.start.isBefore(startOfThisWeek) }

        val percentageChange = when {
            lastWeekCount > 0 -> ((thisWeekCount - lastWeekCount).toFloat() / lastWeekCount) * 100
            thisWeekCount > 0 -> 100f
            else -> 0f
        }

        return FastingTrend(
            currentCount = thisWeekCount,
            previousCount = lastWeekCount,
            percentageChange = percentageChange,
            isUpward = thisWeekCount >= lastWeekCount
        )
    }

    private fun calculateWeeklyActivity(completedFasts: List<Fast>): List<DailyActivity> {
        val today = LocalDate.now()
        val startOfWeek = today.minusDays(6)
        
        return (0..6).map { i ->
            val date = startOfWeek.plusDays(i.toLong())
            val fastsOnDate = completedFasts.filter { it.start.toLocalDate() == date }
            val totalDurationHours = fastsOnDate.sumOf { it.duration() }.toFloat() / 3600000f
            
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
            fast.start.toLocalTime().toSecondOfDay().toLong()
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
