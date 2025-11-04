package com.fasttimes.data.profile

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DefaultFastingProfileRepositoryImpl @Inject constructor(
    private val fastingProfileDao: FastingProfileDao
) : FastingProfileRepository {

    override fun getProfiles(): Flow<List<FastingProfile>> = fastingProfileDao.getProfiles()

    override suspend fun addProfile(profile: FastingProfile): Long {
        return fastingProfileDao.insert(profile)
    }

    override suspend fun updateProfile(profile: FastingProfile) {
        fastingProfileDao.update(profile)
    }

    override suspend fun deleteProfile(profile: FastingProfile) {
        fastingProfileDao.delete(profile)
    }

    override suspend fun setFavoriteProfile(profile: FastingProfile) {
        fastingProfileDao.clearFavorites()
        fastingProfileDao.update(profile.copy(isFavorite = true))
    }
}
