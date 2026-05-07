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
package com.tmdev.fasttimes.data.fast

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FastsRepositoryImplTest {

    private lateinit var fastDao: FastDao
    private lateinit var repository: FastsRepositoryImpl

    @Before
    fun setup() {
        fastDao = mockk()
        repository = FastsRepositoryImpl(fastDao)
    }

    @Test
    fun `getFasts calls dao`() = runTest {
        val fasts = listOf(
            Fast(id = 1, startTime = 1000L, endTime = 2000L, targetDuration = 3600L, profileName = "16:8")
        )
        every { fastDao.getAllFasts() } returns flowOf(fasts)

        repository.getFasts().test {
            assertEquals(fasts, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getActiveFast calls dao`() = runTest {
        val activeFast = Fast(id = 1, startTime = 1000L, endTime = null, targetDuration = 3600L, profileName = "Open")
        every { fastDao.getActiveFast() } returns flowOf(activeFast)

        repository.getActiveFast().test {
            assertEquals(activeFast, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `insertFast calls dao`() = runTest {
        val fast = Fast(startTime = 1000L, endTime = null, targetDuration = null, profileName = "Open")
        coEvery { fastDao.insertFast(fast) } returns 1L

        val id = repository.insertFast(fast)

        assertEquals(1L, id)
        coVerify { fastDao.insertFast(fast) }
    }

    @Test
    fun `endFast updates dao`() = runTest {
        coEvery { fastDao.updateFastEndTime(1L, 3000L) } returns Unit

        repository.endFast(1L, 3000L)

        coVerify { fastDao.updateFastEndTime(1L, 3000L) }
    }

    @Test
    fun `deleteFast calls dao`() = runTest {
        coEvery { fastDao.deleteFast(1L) } returns Unit

        repository.deleteFast(1L)

        coVerify { fastDao.deleteFast(1L) }
    }
}
