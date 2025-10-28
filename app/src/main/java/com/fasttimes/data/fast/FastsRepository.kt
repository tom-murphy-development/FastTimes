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
}
