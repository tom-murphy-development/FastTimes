package com.fasttimes.data.profile

import kotlinx.coroutines.flow.Flow

interface FastingProfileRepository {
    fun getProfiles(): Flow<List<FastingProfile>>
    suspend fun addProfile(profile: FastingProfile)
    suspend fun updateProfile(profile: FastingProfile)
    suspend fun deleteProfile(profile: FastingProfile)
}
