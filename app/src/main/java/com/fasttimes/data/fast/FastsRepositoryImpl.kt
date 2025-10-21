package com.fasttimes.data.fast

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FastsRepositoryImpl @Inject constructor(
    private val fastDao: FastDao
) : FastsRepository {
    override fun getFasts(): Flow<List<Fast>> = fastDao.getAllFasts()
}
