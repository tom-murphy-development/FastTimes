package com.fasttimes.data.profile

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FastingProfileDao {

    @Query("SELECT * FROM fasting_profiles ORDER BY isFavorite DESC, displayName ASC")
    fun getProfiles(): Flow<List<FastingProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: FastingProfile): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(profiles: List<FastingProfile>)

    @Update
    suspend fun update(profile: FastingProfile)

    @Delete
    suspend fun delete(profile: FastingProfile)

    @Query("UPDATE fasting_profiles SET isFavorite = 0")
    suspend fun clearFavorites()
}
