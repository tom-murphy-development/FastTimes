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

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FastDao {
    @Query("SELECT * FROM fasts ORDER BY startTime DESC")
    fun getAllFasts(): Flow<List<Fast>>

    @Query("SELECT * FROM fasts WHERE id = :id")
    fun getFast(id: Long): Flow<Fast?>


    @Query("SELECT * FROM fasts WHERE endTime IS NULL")
    fun getActiveFast(): Flow<Fast?>

    @Query("SELECT * FROM fasts WHERE startTime < :dayEnd AND (endTime IS NULL OR endTime > :dayStart) ORDER BY startTime DESC")
    fun getFastsForDay(dayStart: Long, dayEnd: Long): Flow<List<Fast>>

    @Insert
    suspend fun insertFast(fast: Fast): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(fasts: List<Fast>)

    @Update
    suspend fun updateFast(fast: Fast)

    @Query("UPDATE fasts SET endTime = :endTime WHERE id = :id")
    suspend fun updateFastEndTime(id: Long, endTime: Long)

    @Query("UPDATE fasts SET rating = :rating WHERE id = :fastId")
    suspend fun updateRating(fastId: Long, rating: Int)

    @Query("DELETE FROM fasts WHERE id = :fastId")
    suspend fun deleteFast(fastId: Long)

    @Query("DELETE FROM fasts")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(fasts: List<Fast>) {
        deleteAll()
        insertAll(fasts)
    }
}
