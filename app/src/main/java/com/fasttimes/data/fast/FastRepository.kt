package com.fasttimes.data.fast

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FastRepository(private val fastDao: FastDao) {
    fun getAllFasts(): Flow<List<Fast>> = fastDao.getAllFasts()

    fun getCurrentFast(): Flow<Fast?> = fastDao.getAllFasts().map { fasts ->
        fasts.firstOrNull { it.endTime == null }
    }

    suspend fun getFast(id: Long): Fast? = fastDao.getFast(id)

    suspend fun insertFast(fast: Fast): Long = fastDao.insertFast(fast)

    suspend fun endFast(id: Long, endTime: Long) = fastDao.updateFastEndTime(id, endTime)
}
