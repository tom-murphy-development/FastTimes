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
package com.fasttimes.ui.history

import androidx.lifecycle.SavedStateHandle
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime

@ExperimentalCoroutinesApi
class HistoryViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

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
    }

    @Test
    fun `navigation updates displayed month`() = runTest {
        viewModel = HistoryViewModel(fastsRepository, settingsRepository, savedStateHandle)
        val initialMonth = YearMonth.now()
        
        viewModel.onPreviousMonth()
        assertEquals(initialMonth.minusMonths(1), viewModel.uiState.value.displayedMonth)
        
        viewModel.onNextMonth()
        assertEquals(initialMonth, viewModel.uiState.value.displayedMonth)
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
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        val fastInThisMonth = Fast(id = 1, startTime = now.toInstant().toEpochMilli(), endTime = now.plusHours(16).toInstant().toEpochMilli(), profileName = "16:8", targetDuration = 16 * 3600000L, notes = null)
        val fastInLastMonth = Fast(id = 2, startTime = now.minusMonths(1).toInstant().toEpochMilli(), endTime = now.minusMonths(1).plusHours(16).toInstant().toEpochMilli(), profileName = "16:8", targetDuration = 16 * 3600000L, notes = null)
        
        every { fastsRepository.getFasts() } returns flowOf(listOf(fastInThisMonth, fastInLastMonth))
        
        viewModel = HistoryViewModel(fastsRepository, settingsRepository, savedStateHandle)
        
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.totalFastsInMonth)
            
            viewModel.onPreviousMonth()
            val prevState = awaitItem()
            assertEquals(1, prevState.totalFastsInMonth)
            assertEquals(fastInLastMonth.id, prevState.longestFastInMonth?.id)
        }
    }

    @Test
    fun `onDayClick toggles selection`() = runTest {
        viewModel = HistoryViewModel(fastsRepository, settingsRepository, savedStateHandle)
        
        viewModel.onDayClick(15)
        assertEquals(15, viewModel.uiState.value.selectedDay)
        
        viewModel.onDayClick(15)
        assertNull(viewModel.uiState.value.selectedDay)
    }
}
