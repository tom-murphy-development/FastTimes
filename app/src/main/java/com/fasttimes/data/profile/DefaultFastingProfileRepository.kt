package com.fasttimes.data.profile

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DefaultFastingProfileRepository @Inject constructor(
    private val fastingProfileDao: FastingProfileDao
) : FastingProfileRepository {

    override fun getProfiles(): Flow<List<FastingProfile>> = fastingProfileDao.getProfiles()

    override suspend fun addProfile(profile: FastingProfile) {
        fastingProfileDao.insert(profile)
    }

    override suspend fun updateProfile(profile: FastingProfile) {
        fastingProfileDao.update(profile)
    }

    override suspend fun deleteProfile(profile: FastingProfile) {
        fastingProfileDao.delete(profile)
    }
}
