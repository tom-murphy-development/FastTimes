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
package com.tmdev.fasttimes.ui.dashboard

import android.app.AlarmManager
import android.app.Application
import app.cash.turbine.test
import com.tmdev.fasttimes.alarms.AlarmScheduler
import com.tmdev.fasttimes.data.AppTheme
import com.tmdev.fasttimes.data.DefaultFastingProfile
import com.tmdev.fasttimes.data.fast.Fast
import com.tmdev.fasttimes.data.fast.FastingPhase
import com.tmdev.fasttimes.data.fast.FastsRepository
import com.tmdev.fasttimes.data.profile.FastingProfile
import com.tmdev.fasttimes.data.profile.FastingProfileRepository
import com.tmdev.fasttimes.data.settings.SettingsRepository
import com.tmdev.fasttimes.data.settings.UserData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Duration
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

@ExperimentalCoroutinesApi
class DashboardViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule(UnconfinedTestDispatcher())

    private lateinit var fastsRepository: FastsRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var fastingProfileRepository: FastingProfileRepository
    private lateinit var viewModel: DashboardViewModel
    private lateinit var alarmScheduler: AlarmScheduler
    private lateinit var alarmManager: AlarmManager
    private lateinit var application: Application

    @Before
    fun setup() {
        fastsRepository = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)
        fastingProfileRepository = mockk(relaxed = true)
        alarmScheduler = mockk(relaxed = true)
        alarmManager = mockk(relaxed = true)
        application = mockk(relaxed = true)

        every { fastsRepository.getFasts() } returns flowOf(emptyList())
        coEvery { fastsRepository.insertFast(any()) } returns 1L
        every { settingsRepository.showLiveProgress } returns flowOf(true)
        every { settingsRepository.confettiShownForFastId } returns flowOf(0L)
        every { settingsRepository.showFab } returns flowOf(true)
        every { settingsRepository.showFastingPhases } returns flowOf(true)
        every { settingsRepository.showGoalReachedNotification } returns flowOf(true)
        every { settingsRepository.userData } returns flowOf(
            UserData(
                fastingGoal = Duration.ofHours(16),
                theme = AppTheme.SYSTEM,
                seedColor = null,
                accentColor = null,
                useWavyIndicator = false,
                useExpressiveTheme = false,
                useSystemColors = false,
                showFastingPhases = true
            )
        )
        every { fastingProfileRepository.getProfiles() } returns flowOf(emptyList())

        viewModel = DashboardViewModel(
            fastsRepository,
            settingsRepository,
            fastingProfileRepository,
            alarmScheduler,
            alarmManager,
            application
        )
    }

    @Test
    fun `startOpenFast inserts new fast`() = runTest {
        every { settingsRepository.showLiveProgress } returns flowOf(false)

        viewModel.startOpenFast()
        advanceUntilIdle()
        coVerify { fastsRepository.insertFast(any()) }
    }

    @Test
    fun `startProfileFast schedules alarm and inserts fast`() = runTest {
        val profile = FastingProfile(id = 1, displayName = "16:8", duration = 16.hours.inWholeMilliseconds, description = "", isFavorite = true)
        every { settingsRepository.showLiveProgress } returns flowOf(false)
        every { settingsRepository.showGoalReachedNotification } returns flowOf(false)

        viewModel.startProfileFast(profile)
        advanceUntilIdle()

        coVerify { fastsRepository.insertFast(match { it.profileName == "16:8" }) }
        coVerify { alarmScheduler.schedule(any()) }
    }

    @Test
    fun `endCurrentFast calls repository`() = runTest {
        val fast = Fast(
            id = 1,
            startTime = System.currentTimeMillis() - 1000L,
            endTime = null,
            profileName = DefaultFastingProfile.OPEN.displayName,
            targetDuration = 1000L,
            notes = null
        )
        every { fastsRepository.getFasts() } returns flowOf(listOf(fast))
        every { settingsRepository.showLiveProgress } returns flowOf(false)

        viewModel = DashboardViewModel(
            fastsRepository,
            settingsRepository,
            fastingProfileRepository,
            alarmScheduler,
            alarmManager,
            application
        )

        // Collect the flow to make it active and wait for the initial value
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        
        // Ensure the state is updated from Loading
        runCurrent()
        
        assertTrue("State was ${viewModel.uiState.value}", viewModel.uiState.value !is DashboardUiState.Loading)

        viewModel.endCurrentFast()
        runCurrent()

        coVerify { fastsRepository.endFast(fast.id, any()) }
        coVerify { alarmScheduler.cancel(match { it.id == fast.id }) }
        
        job.cancel()
    }

    @Test
    fun `stats calculated correctly for multiple fasts`() = runTest {
        val now = System.currentTimeMillis()
        val hour = 3_600_000L
        val fasts = listOf(
            Fast(id = 1, startTime = now - (4 * hour), endTime = now - (2 * hour), profileName = DefaultFastingProfile.OPEN.displayName, targetDuration = 2 * hour, notes = null),
            Fast(id = 2, startTime = now - (10 * hour), endTime = now - (6 * hour), profileName = DefaultFastingProfile.OPEN.displayName, targetDuration = 4 * hour, notes = null)
        )

        every { fastsRepository.getFasts() } returns flowOf(fasts)
        viewModel = DashboardViewModel(
            fastsRepository,
            settingsRepository,
            fastingProfileRepository,
            alarmScheduler,
            alarmManager,
            application
        )

        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.stats.collect()
        }
        runCurrent()

        val stats = viewModel.stats.value
        assertEquals(2, stats.totalFasts)
        assertEquals(6.hours, stats.totalFastingTime)
        assertEquals(3.hours, stats.averageFast)
        assertEquals(fasts[1].id, stats.longestFast?.id)
        
        job.cancel()
    }

    @Test
    fun `trend calculation handles percentage correctly`() = runTest {
        val systemZone = ZoneId.systemDefault()
        val currentMonth = YearMonth.now()
        val previousMonth = currentMonth.minusMonths(1)
        
        val currentMonthTime = currentMonth.atDay(15).atStartOfDay(systemZone).toInstant().toEpochMilli()
        val previousMonthTime = previousMonth.atDay(15).atStartOfDay(systemZone).toInstant().toEpochMilli()

        // 2 fasts this month, 1 fast last month = 100% increase
        val fasts = listOf(
            createCompletedFast(1, currentMonthTime),
            createCompletedFast(2, currentMonthTime),
            createCompletedFast(3, previousMonthTime)
        )

        every { fastsRepository.getFasts() } returns flowOf(fasts)
        viewModel = DashboardViewModel(fastsRepository, settingsRepository, fastingProfileRepository, alarmScheduler, alarmManager, application)

        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.stats.collect()
        }
        runCurrent()

        val stats = viewModel.stats.value
        assertEquals(2, stats.trend.currentCount)
        assertEquals(1, stats.trend.previousCount)
        assertEquals(100f, stats.trend.percentageChange)
        assertTrue(stats.trend.isUpward)
        
        job.cancel()
    }

    @Test
    fun `streak calculation includes today`() = runTest {
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val twoDaysAgo = today.minusDays(2)

        val fasts = listOf(
            createCompletedFast(1, twoDaysAgo.atStartOfDay(zoneId).toInstant().toEpochMilli()),
            createCompletedFast(2, yesterday.atStartOfDay(zoneId).toInstant().toEpochMilli()),
            createCompletedFast(3, today.atStartOfDay(zoneId).toInstant().toEpochMilli())
        )

        every { fastsRepository.getFasts() } returns flowOf(fasts)
        viewModel = DashboardViewModel(fastsRepository, settingsRepository, fastingProfileRepository, alarmScheduler, alarmManager, application)

        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.stats.collect()
        }
        runCurrent()

        val stats = viewModel.stats.value
        assertEquals(3, stats.streak.daysInARow)
        
        job.cancel()
    }

    @Test
    fun `streak calculation counts back from yesterday if no fast today`() = runTest {
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val twoDaysAgo = today.minusDays(2)

        val fasts = listOf(
            createCompletedFast(1, twoDaysAgo.atStartOfDay(zoneId).toInstant().toEpochMilli()),
            createCompletedFast(2, yesterday.atStartOfDay(zoneId).toInstant().toEpochMilli())
        )

        every { fastsRepository.getFasts() } returns flowOf(fasts)
        viewModel = DashboardViewModel(fastsRepository, settingsRepository, fastingProfileRepository, alarmScheduler, alarmManager, application)

        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.stats.collect()
        }
        runCurrent()

        val stats = viewModel.stats.value
        assertEquals(2, stats.streak.daysInARow)
        
        job.cancel()
    }

    @Test
    fun `streak calculation returns most recent streak if there is a gap`() = runTest {
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now()
        val fourDaysAgo = today.minusDays(4)
        val fiveDaysAgo = today.minusDays(5)

        // Gap of 2 days between 4 days ago and today
        val fasts = listOf(
            createCompletedFast(1, fiveDaysAgo.atStartOfDay(zoneId).toInstant().toEpochMilli()),
            createCompletedFast(2, fourDaysAgo.atStartOfDay(zoneId).toInstant().toEpochMilli())
        )

        every { fastsRepository.getFasts() } returns flowOf(fasts)
        viewModel = DashboardViewModel(fastsRepository, settingsRepository, fastingProfileRepository, alarmScheduler, alarmManager, application)

        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.stats.collect()
        }
        runCurrent()

        val stats = viewModel.stats.value
        assertEquals(2, stats.streak.daysInARow)
        assertEquals(fourDaysAgo, stats.streak.lastFastDate)
        
        job.cancel()
    }

    @Test
    fun `uiState transitions to FastingGoalReached when time is up`() = runTest {
        val startTime = System.currentTimeMillis() - 17.hours.inWholeMilliseconds
        val activeFast = Fast(id = 1, startTime = startTime, endTime = null, profileName = "16:8", targetDuration = 16.hours.inWholeMilliseconds)
        
        every { fastsRepository.getFasts() } returns flowOf(listOf(activeFast))
        every { settingsRepository.confettiShownForFastId } returns flowOf(0L) 
        
        viewModel = DashboardViewModel(fastsRepository, settingsRepository, fastingProfileRepository, alarmScheduler, alarmManager, application)

        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        runCurrent()

        val state = viewModel.uiState.value
        assertTrue("State should be FastingGoalReached but was $state", state is DashboardUiState.FastingGoalReached)
        val reachedState = state as DashboardUiState.FastingGoalReached
        assertTrue(reachedState.showConfetti)
        // Verify phases
        assertTrue(reachedState.relevantPhases.isNotEmpty())
        assertEquals(FastingPhase.POST_ABSORPTIVE, reachedState.relevantPhases[0])
        assertEquals(FastingPhase.METABOLIC_SWITCH, reachedState.currentPhase)
        
        job.cancel()
    }

    @Test
    fun `uiState in FastingInProgress has correct phases`() = runTest {
        val startTime = System.currentTimeMillis() - 13.hours.inWholeMilliseconds
        val activeFast = Fast(id = 1, startTime = startTime, endTime = null, profileName = "16:8", targetDuration = 16.hours.inWholeMilliseconds)
        
        every { fastsRepository.getFasts() } returns flowOf(listOf(activeFast))
        
        viewModel = DashboardViewModel(fastsRepository, settingsRepository, fastingProfileRepository, alarmScheduler, alarmManager, application)

        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        runCurrent()

        val state = viewModel.uiState.value
        assertTrue("State should be FastingInProgress but was $state", state is DashboardUiState.FastingInProgress)
        val progressState = state as DashboardUiState.FastingInProgress
        
        // For 16h goal, phases should be POST_ABSORPTIVE (0h) and METABOLIC_SWITCH (12h)
        assertEquals(2, progressState.relevantPhases.size)
        assertEquals(FastingPhase.POST_ABSORPTIVE, progressState.relevantPhases[0])
        assertEquals(FastingPhase.METABOLIC_SWITCH, progressState.relevantPhases[1])
        
        // At 13h, current phase should be METABOLIC_SWITCH
        assertEquals(FastingPhase.METABOLIC_SWITCH, progressState.currentPhase)
        
        job.cancel()
    }

    @Test
    fun `uiState in OpenFasting has current phase`() = runTest {
        val startTime = System.currentTimeMillis() - 19.hours.inWholeMilliseconds
        val activeFast = Fast(id = 1, startTime = startTime, endTime = null, profileName = "Open Fast", targetDuration = null)
        
        every { fastsRepository.getFasts() } returns flowOf(listOf(activeFast))
        
        viewModel = DashboardViewModel(fastsRepository, settingsRepository, fastingProfileRepository, alarmScheduler, alarmManager, application)

        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        runCurrent()

        val state = viewModel.uiState.value
        assertTrue("State should be OpenFasting but was $state", state is DashboardUiState.OpenFasting)
        val openState = state as DashboardUiState.OpenFasting
        
        // At 19h, current phase should be EARLY_AUTOPHAGY
        assertEquals(FastingPhase.EARLY_AUTOPHAGY, openState.currentPhase)
        
        job.cancel()
    }

    @Test
    fun `showConfetti is false when already recorded in settings`() = runTest {
        val startTime = System.currentTimeMillis() - 17.hours.inWholeMilliseconds
        val activeFast = Fast(id = 99, startTime = startTime, endTime = null, profileName = "16:8", targetDuration = 16.hours.inWholeMilliseconds)
        
        every { fastsRepository.getFasts() } returns flowOf(listOf(activeFast))
        every { settingsRepository.confettiShownForFastId } returns flowOf(99L) 
        
        viewModel = DashboardViewModel(fastsRepository, settingsRepository, fastingProfileRepository, alarmScheduler, alarmManager, application)

        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.uiState.test(timeout = 1.seconds) {
            var state = awaitItem()
            while (state is DashboardUiState.Loading) {
                state = awaitItem()
            }

            val reachedState = state as DashboardUiState.FastingGoalReached
            assertFalse(reachedState.showConfetti)
        }
        job.cancel()
    }

    @Test
    fun `saveFastRating updates repository`() = runTest {
        viewModel.saveFastRating(123L, 5)
        advanceUntilIdle()
        coVerify { fastsRepository.updateRating(123L, 5) }
    }

    @Test
    fun `onConfettiShown updates settings`() = runTest {
        viewModel.onConfettiShown(456L)
        advanceUntilIdle()
        coVerify { settingsRepository.setConfettiShownForFastId(456L) }
    }

    @Test
    fun `streak calculation includes all days covered by a multi-day fast`() = runTest {
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now()
        val twoDaysAgo = today.minusDays(2)

        // A single fast starting 2 days ago and ending today (e.g., a 48-hour fast)
        val fasts = listOf(
            Fast(
                id = 1,
                startTime = twoDaysAgo.atTime(20, 0).atZone(zoneId).toInstant().toEpochMilli(),
                endTime = today.atTime(8, 0).atZone(zoneId).toInstant().toEpochMilli(),
                profileName = "Long Fast",
                targetDuration = 36.hours.inWholeMilliseconds,
                notes = null
            )
        )

        every { fastsRepository.getFasts() } returns flowOf(fasts)
        viewModel = DashboardViewModel(fastsRepository, settingsRepository, fastingProfileRepository, alarmScheduler, alarmManager, application)

        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.stats.collect()
        }
        runCurrent()

        val stats = viewModel.stats.value
        assertEquals("Streak should be 3 days for a fast spanning twoDaysAgo, yesterday, and today", 3, stats.streak.daysInARow)
        
        job.cancel()
    }

    @Test
    fun `weekly progress reflects all days covered by a multi-day fast`() = runTest {
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val twoDaysAgo = today.minusDays(2)

        val fasts = listOf(
            Fast(
                id = 1,
                startTime = twoDaysAgo.atTime(20, 0).atZone(zoneId).toInstant().toEpochMilli(),
                endTime = today.atTime(8, 0).atZone(zoneId).toInstant().toEpochMilli(),
                profileName = "Long Fast",
                targetDuration = 36.hours.inWholeMilliseconds,
                notes = null
            )
        )

        every { fastsRepository.getFasts() } returns flowOf(fasts)
        viewModel = DashboardViewModel(fastsRepository, settingsRepository, fastingProfileRepository, alarmScheduler, alarmManager, application)

        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.stats.collect()
        }
        runCurrent()

        val weeklyProgress = viewModel.stats.value.weeklyProgress
        
        val todayProgress = weeklyProgress.find { it.date == today }
        val yesterdayProgress = weeklyProgress.find { it.date == yesterday }
        val twoDaysAgoProgress = weeklyProgress.find { it.date == twoDaysAgo }

        assertTrue("Today should be marked as completed", todayProgress?.isCompleted == true)
        assertTrue("Yesterday should be marked as completed", yesterdayProgress?.isCompleted == true)
        assertTrue("Two days ago should be marked as completed", twoDaysAgoProgress?.isCompleted == true)
        
        job.cancel()
    }

    @Test
    fun `streak calculation includes days covered by an ongoing multi-day fast`() = runTest {
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now()
        val twoDaysAgo = today.minusDays(2)

        // An active fast starting 2 days ago and not yet ended
        val activeFast = Fast(
            id = 1,
            startTime = twoDaysAgo.atTime(20, 0).atZone(zoneId).toInstant().toEpochMilli(),
            endTime = null,
            profileName = "Long Fast",
            targetDuration = 36.hours.inWholeMilliseconds,
            notes = null
        )

        every { fastsRepository.getFasts() } returns flowOf(listOf(activeFast))
        viewModel = DashboardViewModel(fastsRepository, settingsRepository, fastingProfileRepository, alarmScheduler, alarmManager, application)

        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.stats.collect()
        }
        runCurrent()

        val stats = viewModel.stats.value
        assertEquals("Streak should be 3 days for an ongoing fast spanning twoDaysAgo, yesterday, and today", 3, stats.streak.daysInARow)
        
        job.cancel()
    }

    private fun createCompletedFast(id: Long, dateMillis: Long): Fast {
        val zdt = ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(dateMillis), ZoneId.systemDefault())
        // Ensure the fast is entirely within the day represented by dateMillis
        // For example, 4 AM to 8 PM on that same day
        val startTime = zdt.plusHours(4).toInstant().toEpochMilli()
        val endTime = zdt.plusHours(20).toInstant().toEpochMilli()
        return Fast(
            id = id,
            startTime = startTime,
            endTime = endTime,
            profileName = "16:8",
            targetDuration = 16.hours.inWholeMilliseconds,
            notes = null
        )
    }
}
