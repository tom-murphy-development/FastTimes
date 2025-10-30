package com.fasttimes.data.profile

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FastingProfileDao {

    @Query("SELECT * FROM fasting_profiles ORDER BY name ASC")
    fun getProfiles(): Flow<List<FastingProfile>>

    @Insert
    suspend fun insert(profile: FastingProfile)

    @Update
    suspend fun update(profile: FastingProfile)

    @Delete
    suspend fun delete(profile: FastingProfile)

    @Transaction
    @Query("UPDATE fasting_profiles SET isDefault = 0")
    suspend fun clearDefault()

    @Transaction
    suspend fun setDefault(profile: FastingProfile) {
        clearDefault()
        update(profile.copy(isDefault = true))
    }
}
