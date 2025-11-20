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
import javax.inject.Inject

class FastsRepositoryImpl @Inject constructor(
    private val fastDao: FastDao
) : FastsRepository {
    override fun getFasts(): Flow<List<Fast>> = fastDao.getAllFasts()
    override fun getFast(id: Long): Flow<Fast?> = fastDao.getFast(id)

    override fun getActiveFast(): Flow<Fast?> = fastDao.getActiveFast()
    override suspend fun insertFast(fast: Fast): Long = fastDao.insertFast(fast)

    override suspend fun updateFast(fast: Fast) {
        fastDao.updateFast(fast)
    }

    override suspend fun endFast(id: Long, endTime: Long) {
        fastDao.updateFastEndTime(id, endTime)
    }

    override suspend fun updateRating(fastId: Long, rating: Int) {
        fastDao.updateRating(fastId, rating)
    }

    override suspend fun deleteFast(fastId: Long) {
        fastDao.deleteFast(fastId)
    }

    override suspend fun replaceAll(fasts: List<Fast>) {
        fastDao.replaceAll(fasts)
    }
}
