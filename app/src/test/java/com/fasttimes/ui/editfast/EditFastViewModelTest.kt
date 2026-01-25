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
package com.fasttimes.ui.editfast

import com.fasttimes.data.fast.Fast
import com.fasttimes.data.fast.FastsRepository
import com.fasttimes.ui.dashboard.MainCoroutineRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.hours

@ExperimentalCoroutinesApi
class EditFastViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var fastsRepository: FastsRepository
    private lateinit var viewModel: EditFastViewModel

    @Before
    fun setup() {
        fastsRepository = mockk(relaxed = true)
    }

    @Test
    fun `saveChanges fails if end time is before start time`() = runTest {
        val startTime = System.currentTimeMillis() - 2.hours.inWholeMilliseconds
        val endTime = startTime - 1.hours.inWholeMilliseconds // Invalid: end before start
        val fast = Fast(id = 1, startTime = startTime, endTime = endTime, targetDuration = 16.hours.inWholeMilliseconds, profileName = "16:8")
        
        every { fastsRepository.getFast(1) } returns flowOf(fast)
        viewModel = EditFastViewModel(fastsRepository, 1)
        advanceUntilIdle()

        viewModel.saveChanges { /* On Success */ }
        advanceUntilIdle()

        assertEquals("End time must be after start time.", viewModel.uiState.value.error)
        coVerify(exactly = 0) { fastsRepository.updateFast(any()) }
    }

    @Test
    fun `saveChanges fails if start time is in future`() = runTest {
        val futureStart = System.currentTimeMillis() + 1.hours.inWholeMilliseconds
        val fast = Fast(id = 1, startTime = futureStart, endTime = null, targetDuration = 16.hours.inWholeMilliseconds, profileName = "16:8")
        
        every { fastsRepository.getFast(1) } returns flowOf(fast)
        viewModel = EditFastViewModel(fastsRepository, 1)
        advanceUntilIdle()

        viewModel.saveChanges { }
        advanceUntilIdle()

        assertEquals("Start time cannot be in the future.", viewModel.uiState.value.error)
    }

    @Test
    fun `saveChanges calls repository on valid data`() = runTest {
        val startTime = System.currentTimeMillis() - 17.hours.inWholeMilliseconds
        val endTime = startTime + 16.hours.inWholeMilliseconds
        val fast = Fast(id = 1, startTime = startTime, endTime = endTime, targetDuration = 16.hours.inWholeMilliseconds, profileName = "16:8")
        
        every { fastsRepository.getFast(1) } returns flowOf(fast)
        viewModel = EditFastViewModel(fastsRepository, 1)
        advanceUntilIdle()

        var successCalled = false
        viewModel.saveChanges { successCalled = true }
        advanceUntilIdle()

        assertTrue(successCalled)
        coVerify { fastsRepository.updateFast(match { it.id == 1L }) }
    }
}
