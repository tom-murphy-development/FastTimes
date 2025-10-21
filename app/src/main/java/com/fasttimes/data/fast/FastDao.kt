package com.fasttimes.data.fast

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FastDao {
    @Query("SELECT * FROM fast_table ORDER BY startTime DESC")
    fun getAllFasts(): Flow<List<Fast>>

    @Query("SELECT * FROM fast_table WHERE id = :id")
    suspend fun getFast(id: Long): Fast?

    @Query("SELECT * FROM fast_table WHERE startTime < :dayEnd AND (endTime IS NULL OR endTime > :dayStart) ORDER BY startTime DESC")
    fun getFastsForDay(dayStart: Long, dayEnd: Long): Flow<List<Fast>>

    @Insert
    suspend fun insertFast(fast: Fast): Long

    @Query("UPDATE fast_table SET endTime = :endTime WHERE id = :id")
    suspend fun updateFastEndTime(id: Long, endTime: Long)
}
