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
package com.fasttimes.ui.dashboard

import android.app.AlarmManager
import android.app.Application
import app.cash.turbine.test
import com.fasttimes.alarms.AlarmScheduler
import com.fasttimes.data.AppTheme
import com.fasttimes.data.DefaultFastingProfile
import com.fasttimes.data.fast.Fast
import com.fasttimes.data.fast.FastsRepository
import com.fasttimes.data.profile.FastingProfileRepository
import com.fasttimes.data.settings.SettingsRepository
import com.fasttimes.data.settings.UserData
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Duration
import kotlin.time.Duration.Companion.hours

@ExperimentalCoroutinesApi
class DashboardViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

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
        every { settingsRepository.confettiShownForFastId } returns flowOf(0)
        every { settingsRepository.showFab } returns flowOf(true)
        every { settingsRepository.userData } returns flowOf(
            UserData(
                fastingGoal = Duration.ofHours(16),
                theme = AppTheme.SYSTEM,
                seedColor = null,
                accentColor = null,
                useWavyIndicator = false,
                useExpressiveTheme = false,
                useSystemColors = false
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
    fun `startManualFast inserts new fast`() = runTest {
        every { settingsRepository.showLiveProgress } returns flowOf(false) // Disable service start to avoid Intent mock issue

        viewModel.startManualFast()
        advanceUntilIdle()
        coVerify { fastsRepository.insertFast(any()) }
    }

    @Test
    fun `endCurrentFast calls repository`() = runTest {
        val fast = Fast(
            id = 1,
            startTime = System.currentTimeMillis() - 1000L,
            endTime = null,
            profileName = DefaultFastingProfile.MANUAL.displayName,
            targetDuration = 1000L,
            notes = null
        )
        every { fastsRepository.getFasts() } returns flowOf(listOf(fast))
        every { settingsRepository.showLiveProgress } returns flowOf(false) // Disable service start

        viewModel = DashboardViewModel(
            fastsRepository,
            settingsRepository,
            fastingProfileRepository,
            alarmScheduler,
            alarmManager,
            application
        )

        // Collect in backgroundScope to keep the subscription active but cancel it automatically at end of test.
        // Use UnconfinedTestDispatcher to eagerly emit the first value.
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        
        // Ensure the UI state has emitted its initial value derived from the flow
        // (runCurrent works because Unconfined executes eagerly, but this ensures safety)
        runCurrent()

        viewModel.endCurrentFast()

        // Execute the viewModelScope.launch block triggered by endCurrentFast
        runCurrent()

        coVerify { fastsRepository.endFast(fast.id, any()) }
        coVerify { alarmScheduler.cancel(fast) }
    }

    @Test
    fun `stats are calculated correctly`() = runTest {
        val now = System.currentTimeMillis()
        val hour = 3_600_000L
        val fasts = listOf(
            Fast(id = 1, startTime = now - (4 * hour), endTime = now - (2 * hour), profileName = DefaultFastingProfile.MANUAL.displayName, targetDuration = 1000L, notes = null),
            Fast(id = 2, startTime = now - (10 * hour), endTime = now - (6 * hour), profileName = DefaultFastingProfile.MANUAL.displayName, targetDuration = 2000L, notes = null)
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

        // Note: no need to advanceUntilIdle() here because turbine subscribes and triggers the flow

        viewModel.stats.test {
            // The StateFlow will emit its initial value (default/empty) immediately upon subscription.
            // We need to check if the first item is the one we want, or wait for the next one.
            val firstItem = awaitItem()
            val stats = if (firstItem.totalFasts == 0) awaitItem() else firstItem

            assertEquals(2, stats.totalFasts)
            assertEquals(6.hours, stats.totalFastingTime)
            assertEquals(fasts[1], stats.longestFast)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
