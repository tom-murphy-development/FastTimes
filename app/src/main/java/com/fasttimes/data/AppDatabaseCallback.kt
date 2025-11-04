package com.fasttimes.data

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fasttimes.data.profile.FastingProfile
import com.fasttimes.data.profile.FastingProfileDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

class AppDatabaseCallback @Inject constructor(
    private val fastingProfileDao: Provider<FastingProfileDao>,
    private val applicationScope: CoroutineScope,
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        applicationScope.launch {
            prepopulateFastingProfiles()
        }
    }

    private suspend fun prepopulateFastingProfiles() {
        DefaultFastingProfile.entries.forEach {
            fastingProfileDao.get().insert(
                FastingProfile(
                    displayName = it.displayName,
                    duration = it.duration?.inWholeMilliseconds,
                    description = it.description
                )
            )
        }
    }
}
