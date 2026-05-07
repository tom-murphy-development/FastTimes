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
package com.tmdev.fasttimes.ui.history

import androidx.lifecycle.SavedStateHandle
import com.tmdev.fasttimes.data.fast.Fast
import com.tmdev.fasttimes.data.fast.FastsRepository
import com.tmdev.fasttimes.data.settings.SettingsRepository
import com.tmdev.fasttimes.ui.dashboard.MainCoroutineRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.YearMonth
import java.time.ZoneId

@ExperimentalCoroutinesApi
class HistoryViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule(kotlinx.coroutines.test.UnconfinedTestDispatcher())

    private lateinit var fastsRepository: FastsRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var viewModel: HistoryViewModel
    private val savedStateHandle = SavedStateHandle()

    @Before
    fun setup() {
        fastsRepository = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)
        
        every { fastsRepository.getFasts() } returns flowOf(emptyList())
        every { settingsRepository.firstDayOfWeek } returns flowOf("MONDAY")
        every { settingsRepository.userData } returns flowOf(mockk(relaxed = true))
    }

    @Test
    fun `navigation updates displayed month`() = runTest {
        viewModel = HistoryViewModel(fastsRepository, settingsRepository, savedStateHandle)
        val initialMonth = YearMonth.now()
        
        val job = backgroundScope.launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        runCurrent()

        var state = viewModel.uiState.value
        assertEquals(initialMonth, state.displayedMonth)
        
        viewModel.onPreviousMonth()
        runCurrent()
        state = viewModel.uiState.value
        assertEquals(initialMonth.minusMonths(1), state.displayedMonth)
        
        viewModel.onNextMonth()
        runCurrent()
        state = viewModel.uiState.value
        assertEquals(initialMonth, state.displayedMonth)

        job.cancel()
    }

    @Test
    fun `onNextMonth does not navigate to the future`() = runTest {
        viewModel = HistoryViewModel(fastsRepository, settingsRepository, savedStateHandle)
        val currentMonth = YearMonth.now()
        
        viewModel.onNextMonth()
        assertEquals(currentMonth, viewModel.uiState.value.displayedMonth)
    }

    @Test
    fun `fasts are correctly grouped by month`() = runTest {
        val zone = ZoneId.systemDefault()
        val thisMonth = YearMonth.now()
        val lastMonth = thisMonth.minusMonths(1)
        
        val startOfThisMonth = thisMonth.atDay(1).atStartOfDay(zone)
        val startOfLastMonth = lastMonth.atDay(1).atStartOfDay(zone)

        // Create fasts 12 hours into each month to avoid boundary issues
        val fastInThisMonth = Fast(
            id = 1, 
            startTime = startOfThisMonth.plusHours(12).toInstant().toEpochMilli(), 
            endTime = startOfThisMonth.plusHours(28).toInstant().toEpochMilli(), 
            profileName = "16:8", 
            targetDuration = 16 * 3600000L, 
            notes = null
        )
        val fastInLastMonth = Fast(
            id = 2, 
            startTime = startOfLastMonth.plusHours(12).toInstant().toEpochMilli(), 
            endTime = startOfLastMonth.plusHours(28).toInstant().toEpochMilli(), 
            profileName = "16:8", 
            targetDuration = 16 * 3600000L, 
            notes = null
        )
        
        every { fastsRepository.getFasts() } returns flowOf(listOf(fastInThisMonth, fastInLastMonth))
        
        viewModel = HistoryViewModel(fastsRepository, settingsRepository, savedStateHandle)
        
        val job = backgroundScope.launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        runCurrent()

        var state = viewModel.uiState.value
        assertEquals(1, state.totalFastsInMonth)
        
        viewModel.onPreviousMonth()
        runCurrent()

        state = viewModel.uiState.value
        assertEquals(lastMonth, state.displayedMonth)
        assertEquals(1, state.totalFastsInMonth)
        assertEquals(fastInLastMonth.id, state.longestFastInMonth?.id)

        job.cancel()
    }

    @Test
    fun `onDayClick toggles selection`() = runTest {
        viewModel = HistoryViewModel(fastsRepository, settingsRepository, savedStateHandle)
        
        val job = backgroundScope.launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        runCurrent()

        viewModel.onDayClick(15)
        runCurrent()
        var state = viewModel.uiState.value
        assertEquals(15, state.selectedDay)
        
        viewModel.onDayClick(15)
        runCurrent()
        state = viewModel.uiState.value
        assertNull(state.selectedDay)

        job.cancel()
    }
}
