
package com.fasttimes.data.fast

import kotlinx.coroutines.flow.Flow

interface FastsRepository {
    fun getFasts(): Flow<List<Fast>>
}
