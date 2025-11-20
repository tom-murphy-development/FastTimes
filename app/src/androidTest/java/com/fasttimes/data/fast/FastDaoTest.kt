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
package com.fasttimes.data.fast

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FastDaoTest {
    private lateinit var db: com.fasttimes.data.AppDatabase
    private lateinit var dao: FastDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, com.fasttimes.data.AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.fastDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insert_and_getAllFasts() = runBlocking {
        val fast = Fast(startTime = 1000, endTime = null, targetDuration = 10000, notes = "Test", profileName = "Test Profile")
        dao.insertFast(fast)
        val fasts = dao.getAllFasts().first()
        assertEquals(1, fasts.size)
        assertEquals(1000, fasts[0].startTime)
    }

    @Test
    fun updateFastEndTime() = runBlocking {
        val fast = Fast(startTime = 1000, endTime = null, targetDuration = 10000, notes = null, profileName = "Test Profile")
        val id = dao.insertFast(fast)
        dao.updateFastEndTime(id, 2000)
        val fasts = dao.getAllFasts().first()
        assertEquals(2000, fasts[0].endTime)
    }
}
