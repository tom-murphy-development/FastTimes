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
}
