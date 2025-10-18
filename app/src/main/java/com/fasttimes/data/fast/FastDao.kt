package com.fasttimes.data.fast

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FastDao {
    @Query("SELECT * FROM fast_table ORDER BY startTime DESC")
    fun getAllFasts(): Flow<List<Fast>>

    @Insert
    suspend fun insertFast(fast: Fast)

    @Query("UPDATE fast_table SET endTime = :endTime WHERE id = :id")
    suspend fun updateFastEndTime(id: Long, endTime: Long)
}
