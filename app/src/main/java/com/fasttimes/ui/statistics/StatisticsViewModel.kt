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
import com.fasttimes.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
 * Periods for filtering statistics and trends.
 */
enum class StatisticsPeriod {
    WEEKLY, MONTHLY, ALL_TIME
}

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
    val date: LocalDate,
    val durationHours: Float,
    val isGoalMet: Boolean
)

/**
 * UI state data class containing all statistics for the Statistics screen.
 */
data class StatisticsUiState(
    val selectedPeriod: StatisticsPeriod = StatisticsPeriod.WEEKLY,
    val chartPeriod: StatisticsPeriod = StatisticsPeriod.WEEKLY,
    val streak: FastingStreak = FastingStreak(),
    val periodStreakValue: Int = 0,
    val periodAverageFast: Duration = Duration.ZERO,
    val periodTrend: FastingTrend = FastingTrend(),
    val periodTotalFasts: Int = 0,
    val periodFastingTime: Duration = Duration.ZERO,
    val periodConsistency: Float = 0f,
    val averageFast: Duration = Duration.ZERO,
    val weeklyAverageFast: Duration = Duration.ZERO,
    val trend: FastingTrend = FastingTrend(),
    val chartActivity: List<DailyActivity> = emptyList(),
    val chartGoals: Set<Float> = emptySet(),
    val chartAverageHours: Float = 0f,
    val totalFasts: Int = 0,
    val totalFastingTime: Duration = Duration.ZERO,
    val weeklyFastingTime: Duration = Duration.ZERO,
    val longestFast: Fast? = null,
    val fastsPerWeek: Float = 0f,
    val firstFastDate: LocalDate? = null,
    val averageStartTime: LocalTime? = null,
    val mostFrequentDay: DayOfWeek? = null,
    val firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
    val isLoading: Boolean = true
)

/**
 * ViewModel for the Statistics screen.
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val fastsRepository: FastsRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(StatisticsPeriod.WEEKLY)
    private val _chartPeriod = MutableStateFlow(StatisticsPeriod.WEEKLY)

    val statisticsState: StateFlow<StatisticsUiState> = combine(
        fastsRepository.getFasts(),
        _selectedPeriod,
        _chartPeriod,
        settingsRepository.firstDayOfWeek.map { 
            try { DayOfWeek.valueOf(it.uppercase()) } catch (e: Exception) { DayOfWeek.MONDAY }
        }
    ) { fasts, period, chartPeriod, firstDayOfWeek ->
        val completedFasts = fasts.filter { it.endTime != null }
        
        // Base stats (always calculated for the top section/all-time)
        val currentStreak = calculateCurrentStreak(completedFasts)
        val averageFast = calculateAverageFast(completedFasts)
        val velocity = calculateVelocity(completedFasts, StatisticsPeriod.WEEKLY)
        
        // Calculate chart data based on selected chart period
        val chartActivity = calculateActivity(completedFasts, chartPeriod)
        val chartDays = when (chartPeriod) {
            StatisticsPeriod.WEEKLY -> 7
            StatisticsPeriod.MONTHLY -> 30
            else -> 7
        }
        val chartStartDate = LocalDate.now().minusDays((chartDays - 1).toLong())
        val chartFasts = completedFasts.filter { it.start.toLocalDate() >= chartStartDate }
        
        val chartGoals = chartFasts
            .mapNotNull { it.targetDuration?.let { target -> target.toFloat() / 3600000f } }
            .toSet()
        
        val chartAverageDuration = calculateAverageFast(chartFasts)
        val chartAverageHours = chartAverageDuration.inWholeMinutes / 60f

        val today = LocalDate.now()
        val startOfSevenDays = today.minusDays(6)
        
        val totalFastingTime = completedFasts.sumOf { it.duration() }.milliseconds
        val weeklyFasts = completedFasts.filter { it.start.toLocalDate() >= startOfSevenDays }
        val weeklyFastingTime = weeklyFasts.sumOf { it.duration() }.milliseconds
        val weeklyAverageFast = calculateAverageFast(weeklyFasts)

        // Period-specific stats for the Trends section
        val (periodFasts, periodPrevFasts) = filterFastsForPeriod(completedFasts, period)
        
        val periodStreakValue = if (period == StatisticsPeriod.WEEKLY) {
            currentStreak.daysInARow
        } else {
            calculateLongestStreak(periodFasts)
        }
        
        val periodAverageFast = calculateAverageFast(periodFasts)
        val periodTotalFasts = periodFasts.size
        val periodFastingTime = periodFasts.sumOf { it.duration() }.milliseconds
        val periodTrend = calculatePeriodVelocity(periodFasts, periodPrevFasts)
        val periodConsistency = calculateConsistency(periodFasts)

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
            selectedPeriod = period,
            chartPeriod = chartPeriod,
            streak = currentStreak,
            periodStreakValue = periodStreakValue,
            periodAverageFast = periodAverageFast,
            periodTrend = periodTrend,
            periodTotalFasts = periodTotalFasts,
            periodFastingTime = periodFastingTime,
            periodConsistency = periodConsistency,
            averageFast = averageFast,
            weeklyAverageFast = weeklyAverageFast,
            trend = velocity,
            chartActivity = chartActivity,
            chartGoals = chartGoals,
            chartAverageHours = chartAverageHours,
            totalFasts = fasts.size,
            totalFastingTime = totalFastingTime,
            weeklyFastingTime = weeklyFastingTime,
            longestFast = longestFast,
            fastsPerWeek = fastsPerWeek,
            firstFastDate = firstFastDate,
            averageStartTime = averageStartTime,
            mostFrequentDay = mostFrequentDay,
            firstDayOfWeek = firstDayOfWeek,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatisticsUiState(isLoading = true)
    )

    fun onPeriodSelected(period: StatisticsPeriod) {
        _selectedPeriod.value = period
    }

    fun onChartPeriodSelected(period: StatisticsPeriod) {
        _chartPeriod.value = period
    }

    private fun filterFastsForPeriod(fasts: List<Fast>, period: StatisticsPeriod): Pair<List<Fast>, List<Fast>> {
        val now = LocalDate.now()
        return when (period) {
            StatisticsPeriod.WEEKLY -> {
                val start = now.minusDays(6)
                val prevStart = start.minusDays(7)
                val current = fasts.filter { it.start.toLocalDate() >= start }
                val previous = fasts.filter { 
                    val date = it.start.toLocalDate()
                    date >= prevStart && date < start
                }
                current to previous
            }
            StatisticsPeriod.MONTHLY -> {
                val start = now.minusDays(29)
                val prevStart = start.minusDays(30)
                val current = fasts.filter { it.start.toLocalDate() >= start }
                val previous = fasts.filter { 
                    val date = it.start.toLocalDate()
                    date >= prevStart && date < start
                }
                current to previous
            }
            StatisticsPeriod.ALL_TIME -> {
                fasts to emptyList()
            }
        }
    }

    private fun calculateCurrentStreak(completedFasts: List<Fast>): FastingStreak {
        if (completedFasts.isEmpty()) return FastingStreak()

        val fastDates = completedFasts
            .mapNotNull { it.end?.toLocalDate() }
            .distinct()
            .sorted()

        if (fastDates.isEmpty()) return FastingStreak()

        val today = LocalDate.now()
        var currentDate = today
        var streakDays = 0

        // Check if there's a fast today or yesterday to continue the current streak
        while (true) {
            if (currentDate in fastDates) {
                streakDays++
                currentDate = currentDate.minusDays(1)
            } else if (currentDate == today) {
                // If no fast today, streak might still be active from yesterday
                currentDate = currentDate.minusDays(1)
            } else {
                break
            }
        }

        // If no active streak ending today/yesterday, find the last streak
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

    private fun calculateLongestStreak(completedFasts: List<Fast>): Int {
        if (completedFasts.isEmpty()) return 0
        val fastDates = completedFasts
            .mapNotNull { it.end?.toLocalDate() }
            .distinct()
            .sorted()
        
        if (fastDates.isEmpty()) return 0

        var maxStreak = 0
        var currentStreak = 0
        var prevDate: LocalDate? = null

        for (date in fastDates) {
            if (prevDate == null || date == prevDate.plusDays(1)) {
                currentStreak++
            } else {
                maxStreak = maxOf(maxStreak, currentStreak)
                currentStreak = 1
            }
            prevDate = date
        }
        return maxOf(maxStreak, currentStreak)
    }

    private fun calculateAverageFast(completedFasts: List<Fast>): Duration {
        if (completedFasts.isEmpty()) return Duration.ZERO
        val totalDuration = completedFasts.sumOf { it.duration() }
        return (totalDuration / completedFasts.size).milliseconds
    }

    private fun calculateVelocity(completedFasts: List<Fast>, period: StatisticsPeriod): FastingTrend {
        val now = ZonedDateTime.now()
        val startOfThisPeriod = when (period) {
            StatisticsPeriod.WEEKLY -> now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toLocalDate().atStartOfDay(now.zone)
            StatisticsPeriod.MONTHLY -> now.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay(now.zone)
            StatisticsPeriod.ALL_TIME -> return FastingTrend()
        }
        
        val startOfPrevPeriod = when (period) {
            StatisticsPeriod.WEEKLY -> startOfThisPeriod.minusWeeks(1)
            StatisticsPeriod.MONTHLY -> startOfThisPeriod.minusMonths(1)
            else -> startOfThisPeriod
        }

        val thisPeriodCount = completedFasts.count { it.start.isAfter(startOfThisPeriod) }
        val prevPeriodCount = completedFasts.count { it.start.isAfter(startOfPrevPeriod) && it.start.isBefore(startOfThisPeriod) }

        val percentageChange = when {
            prevPeriodCount > 0 -> ((thisPeriodCount - prevPeriodCount).toFloat() / prevPeriodCount) * 100
            thisPeriodCount > 0 -> 100f
            else -> 0f
        }

        return FastingTrend(
            currentCount = thisPeriodCount,
            previousCount = prevPeriodCount,
            percentageChange = percentageChange,
            isUpward = thisPeriodCount >= prevPeriodCount
        )
    }

    private fun calculatePeriodVelocity(currentFasts: List<Fast>, previousFasts: List<Fast>): FastingTrend {
        val currentCount = currentFasts.size
        val previousCount = previousFasts.size

        val percentageChange = when {
            previousCount > 0 -> ((currentCount - previousCount).toFloat() / previousCount) * 100
            currentCount > 0 -> 100f
            else -> 0f
        }

        return FastingTrend(
            currentCount = currentCount,
            previousCount = previousCount,
            percentageChange = percentageChange,
            isUpward = currentCount >= previousCount
        )
    }

    private fun calculateConsistency(fasts: List<Fast>): Float {
        if (fasts.isEmpty()) return 0f
        val goalsMet = fasts.count { it.goalMet() }
        return (goalsMet.toFloat() / fasts.size) * 100f
    }

    private fun calculateActivity(completedFasts: List<Fast>, period: StatisticsPeriod): List<DailyActivity> {
        val days = when (period) {
            StatisticsPeriod.WEEKLY -> 7
            StatisticsPeriod.MONTHLY -> 30
            else -> 7
        }
        val today = LocalDate.now()
        val start = today.minusDays((days - 1).toLong())
        
        return (0 until days).map { i ->
            val date = start.plusDays(i.toLong())
            val fastsOnDate = completedFasts.filter { it.start.toLocalDate() == date }
            val totalDurationHours = fastsOnDate.sumOf { it.duration() }.toFloat() / 3600000f
            
            val goalMet = fastsOnDate.any { it.goalMet() }
            
            DailyActivity(
                date = date,
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
