package com.fasttimes.data.fast

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
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

    @Update
    suspend fun updateFast(fast: Fast)

    @Query("UPDATE fasts SET endTime = :endTime WHERE id = :id")
    suspend fun updateFastEndTime(id: Long, endTime: Long)

    @Query("UPDATE fasts SET rating = :rating WHERE id = :fastId")
    suspend fun updateRating(fastId: Long, rating: Int)
}
