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

import app.cash.turbine.test
import com.fasttimes.data.fast.Fast
import com.fasttimes.data.fast.FastsRepository
import com.fasttimes.data.settings.SettingsRepository
import com.fasttimes.ui.dashboard.MainCoroutineRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import kotlin.time.Duration.Companion.hours

@ExperimentalCoroutinesApi
class StatisticsViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var fastsRepository: FastsRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var viewModel: StatisticsViewModel

    @Before
    fun setup() {
        fastsRepository = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)
        
        every { fastsRepository.getFasts() } returns flowOf(emptyList())
        every { settingsRepository.firstDayOfWeek } returns flowOf("MONDAY")
    }

    @Test
    fun `initial state is loading`() = runTest {
        viewModel = StatisticsViewModel(fastsRepository, settingsRepository)
        assertTrue(viewModel.statisticsState.value.isLoading)
    }

    @Test
    fun `calculateCurrentStreak handles active streak`() = runTest {
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        // Create fasts that clearly end on yesterday and today
        val fasts = listOf(
            createFast(1, yesterday.atTime(8, 0).atZone(zoneId).toInstant().toEpochMilli(), 16.hours.inWholeMilliseconds, 8.hours.inWholeMilliseconds),
            createFast(2, today.atTime(8, 0).atZone(zoneId).toInstant().toEpochMilli(), 16.hours.inWholeMilliseconds, 8.hours.inWholeMilliseconds)
        )

        every { fastsRepository.getFasts() } returns flowOf(fasts)
        viewModel = StatisticsViewModel(fastsRepository, settingsRepository)

        viewModel.statisticsState.test {
            // Skip initial loading state
            var state = awaitItem()
            if (state.isLoading) state = awaitItem()
            
            assertEquals(2, state.streak.daysInARow)
            assertEquals(yesterday, state.streak.startDate)
            assertEquals(today, state.streak.lastFastDate)
        }
    }

    @Test
    fun `period trend calculation reflects activity changes`() = runTest {
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now()
        
        // 2 fasts in current week, 1 in previous week
        val fasts = listOf(
            createFast(1, today.minusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli(), 16.hours.inWholeMilliseconds, 8.hours.inWholeMilliseconds),
            createFast(2, today.minusDays(2).atStartOfDay(zoneId).toInstant().toEpochMilli(), 16.hours.inWholeMilliseconds, 8.hours.inWholeMilliseconds),
            createFast(3, today.minusDays(8).atStartOfDay(zoneId).toInstant().toEpochMilli(), 16.hours.inWholeMilliseconds, 8.hours.inWholeMilliseconds)
        )

        every { fastsRepository.getFasts() } returns flowOf(fasts)
        viewModel = StatisticsViewModel(fastsRepository, settingsRepository)
        viewModel.onPeriodSelected(StatisticsPeriod.WEEKLY)

        viewModel.statisticsState.test {
            var state = awaitItem()
            if (state.isLoading || state.periodTotalFasts == 0) state = awaitItem()
            
            assertEquals(2, state.periodTrend.currentCount)
            assertEquals(1, state.periodTrend.previousCount)
            assertEquals(100f, state.periodTrend.percentageChange)
            assertTrue(state.periodTrend.isUpward)
        }
    }

    @Test
    fun `consistency is calculated as percentage of goals met`() = runTest {
        val fasts = listOf(
            // Goal: 16h, Actual: 17h (Met)
            createFast(1, System.currentTimeMillis() - 24.hours.inWholeMilliseconds, 16.hours.inWholeMilliseconds, 17.hours.inWholeMilliseconds),
            // Goal: 16h, Actual: 10h (Not Met)
            createFast(2, System.currentTimeMillis() - 48.hours.inWholeMilliseconds, 16.hours.inWholeMilliseconds, 10.hours.inWholeMilliseconds)
        )

        every { fastsRepository.getFasts() } returns flowOf(fasts)
        viewModel = StatisticsViewModel(fastsRepository, settingsRepository)

        viewModel.statisticsState.test {
            var state = awaitItem()
            if (state.isLoading || state.periodTotalFasts == 0) state = awaitItem()
            
            assertEquals(50f, state.periodConsistency)
        }
    }

    private fun createFast(id: Long, startTime: Long, target: Long, actual: Long): Fast {
        return Fast(
            id = id,
            startTime = startTime,
            endTime = startTime + actual,
            targetDuration = target,
            profileName = "16:8",
            notes = null
        )
    }
}
