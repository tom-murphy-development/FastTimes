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
package com.tmdev.fasttimes.data.profile

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

class DefaultFastingProfileRepositoryImplTest {

    private lateinit var fastingProfileDao: FastingProfileDao
    private lateinit var repository: DefaultFastingProfileRepositoryImpl

    @Before
    fun setup() {
        fastingProfileDao = mockk()
        repository = DefaultFastingProfileRepositoryImpl(fastingProfileDao)
    }

    @Test
    fun `getProfiles calls dao`() = runTest {
        val profiles = listOf(
            FastingProfile(id = 1, displayName = "16:8", duration = 57600000L, description = "16 hour fast")
        )
        every { fastingProfileDao.getProfiles() } returns flowOf(profiles)

        repository.getProfiles().test {
            assertEquals(profiles, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `addProfile calls dao`() = runTest {
        val profile = FastingProfile(displayName = "18:6", duration = 64800000L, description = "")
        coEvery { fastingProfileDao.insert(profile) } returns 1L

        val id = repository.addProfile(profile)

        assertEquals(1L, id)
        coVerify { fastingProfileDao.insert(profile) }
    }

    @Test
    fun `updateProfile calls dao`() = runTest {
        val profile = FastingProfile(id = 1, displayName = "16:8", duration = 57600000L, description = "")
        coEvery { fastingProfileDao.update(profile) } returns Unit

        repository.updateProfile(profile)

        coVerify { fastingProfileDao.update(profile) }
    }

    @Test
    fun `deleteProfile calls dao`() = runTest {
        val profile = FastingProfile(id = 1, displayName = "16:8", duration = 57600000L, description = "")
        coEvery { fastingProfileDao.delete(profile) } returns Unit

        repository.deleteProfile(profile)

        coVerify { fastingProfileDao.delete(profile) }
    }

    @Test
    fun `setFavoriteProfile clears others and updates favorite`() = runTest {
        val profile = FastingProfile(id = 1, displayName = "16:8", duration = 57600000L, description = "", isFavorite = false)
        coEvery { fastingProfileDao.clearFavorites() } returns Unit
        coEvery { fastingProfileDao.update(any()) } returns Unit

        repository.setFavoriteProfile(profile)

        coVerify { fastingProfileDao.clearFavorites() }
        coVerify { fastingProfileDao.update(match { it.id == 1L && it.isFavorite }) }
    }
}
