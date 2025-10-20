package com.fasttimes.ui.dashboard

import app.cash.turbine.test
import com.fasttimes.alarms.AlarmScheduler
import com.fasttimes.data.FastingProfile
import com.fasttimes.data.fast.Fast
import com.fasttimes.data.fast.FastRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {
    private lateinit var repository: FastRepository
    private lateinit var viewModel: DashboardViewModel
    private lateinit var alarmScheduler: AlarmScheduler
    private lateinit var alarmManager: android.app.AlarmManager
    private lateinit var testDispatcher: TestDispatcher

    @Before
    fun setup() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)

        repository = mockk(relaxed = true)
        alarmScheduler = mockk(relaxed = true)
        alarmManager = mockk(relaxed = true)

        // Default repository history empty
        every { repository.getAllFasts() } returns flowOf(emptyList())
        // Mock insert to return an id
        coEvery { repository.insertFast(any()) } returns 1L

        // Construct ViewModel with required dependencies
        viewModel = DashboardViewModel(repository, alarmScheduler, alarmManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `startManualFast inserts new fast`() = runTest {
        // Call the manual start method
        viewModel.startManualFast()

        // Advance dispatcher so launched coroutine runs
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.insertFast(any()) }
    }

    @Test
    fun `endCurrentFast calls repository`() = runTest {
        // Create an active fast (endTime = null)
        val fast = Fast(
            id = 1,
            startTime = System.currentTimeMillis() - 1000L,
            endTime = null,
            profile = FastingProfile.MANUAL,
            targetDuration = 1000L,
            notes = null
        )

        every { repository.getAllFasts() } returns flowOf(listOf(fast))

        // Recreate ViewModel to pick up new history flow
        viewModel = DashboardViewModel(repository, alarmScheduler, alarmManager)

        viewModel.endCurrentFast()

        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.endFast(fast.id, any()) }
        coVerify { alarmScheduler.cancel(fast) }
    }

    @Test
    fun `stats are calculated correctly`() = runTest {
        val now = System.currentTimeMillis()
        val hour = 3_600_000L
        val fasts = listOf(
            // A 2-hour fast
            Fast(id = 1, startTime = now - (4 * hour), endTime = now - (2 * hour), profile = FastingProfile.MANUAL, targetDuration = 1000L, notes = null),
            // A 4-hour fast
            Fast(id = 2, startTime = now - (10 * hour), endTime = now - (6 * hour), profile = FastingProfile.MANUAL, targetDuration = 2000L, notes = null)
        )

        every { repository.getAllFasts() } returns flowOf(fasts)
        viewModel = DashboardViewModel(repository, alarmScheduler, alarmManager)

        // Ensure state flows have a chance to emit
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.stats.test {
            val stats = awaitItem()
            assertEquals(2, stats.totalFasts)
            // Longest fast should be 4 hours
            assertEquals(4, stats.longestFast)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
