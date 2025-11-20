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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class FastRepository(private val fastDao: FastDao) {
    fun getAllFasts(): Flow<List<Fast>> = fastDao.getAllFasts()

    fun getCurrentFast(): Flow<Fast?> = fastDao.getAllFasts().map { fasts ->
        fasts.firstOrNull { it.endTime == null }
    }

    suspend fun getFast(id: Long): Fast? = fastDao.getFast(id).firstOrNull()

    suspend fun insertFast(fast: Fast): Long = fastDao.insertFast(fast)

    suspend fun updateFast(fast: Fast) = fastDao.updateFast(fast)

    suspend fun endFast(id: Long, endTime: Long) = fastDao.updateFastEndTime(id, endTime)
}
