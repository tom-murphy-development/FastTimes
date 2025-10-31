package com.fasttimes.ui.dashboard

import android.app.AlarmManager
import android.app.Application
import app.cash.turbine.test
import com.fasttimes.alarms.AlarmScheduler
import com.fasttimes.data.DefaultFastingProfile
import com.fasttimes.data.fast.Fast
import com.fasttimes.data.fast.FastRepository
import com.fasttimes.data.settings.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class DashboardViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var repository: FastRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var viewModel: DashboardViewModel
    private lateinit var alarmScheduler: AlarmScheduler
    private lateinit var alarmManager: AlarmManager
    private lateinit var application: Application

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)
        alarmScheduler = mockk(relaxed = true)
        alarmManager = mockk(relaxed = true)
        application = mockk(relaxed = true)

        every { repository.getAllFasts() } returns flowOf(emptyList())
        coEvery { repository.insertFast(any()) } returns 1L
        every { settingsRepository.showLiveProgress } returns flowOf(true)

        viewModel = DashboardViewModel(repository, settingsRepository, alarmScheduler, alarmManager, application)
    }

    @Test
    fun `startManualFast inserts new fast`() = runTest {
        viewModel.startManualFast()
        advanceUntilIdle()
        coVerify { repository.insertFast(any()) }
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
        every { repository.getAllFasts() } returns flowOf(listOf(fast))

        viewModel = DashboardViewModel(repository, settingsRepository, alarmScheduler, alarmManager, application)

        runCurrent()

        viewModel.endCurrentFast()

        runCurrent()

        coVerify { repository.endFast(fast.id, any()) }
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

        every { repository.getAllFasts() } returns flowOf(fasts)
        viewModel = DashboardViewModel(repository, settingsRepository, alarmScheduler, alarmManager, application)

        advanceUntilIdle()

        viewModel.stats.test {
            val stats = awaitItem()
            assertEquals(2, stats.totalFasts)
            assertEquals(4, stats.longestFast)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
