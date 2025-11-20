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

interface FastsRepository {
    fun getFasts(): Flow<List<Fast>>

    fun getFast(id: Long): Flow<Fast?>

    fun getActiveFast(): Flow<Fast?>

    suspend fun insertFast(fast: Fast): Long

    suspend fun updateFast(fast: Fast)

    suspend fun endFast(id: Long, endTime: Long)

    suspend fun updateRating(fastId: Long, rating: Int)

    suspend fun deleteFast(fastId: Long)

    suspend fun replaceAll(fasts: List<Fast>)
}
