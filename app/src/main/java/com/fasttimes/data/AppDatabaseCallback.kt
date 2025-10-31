package com.fasttimes.data

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fasttimes.data.profile.FastingProfile
import com.fasttimes.data.profile.FastingProfileDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

class AppDatabaseCallback @Inject constructor(
    private val fastingProfileDao: Provider<FastingProfileDao>
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        CoroutineScope(Dispatchers.IO).launch {
            prepopulateFastingProfiles()
        }
    }

    private suspend fun prepopulateFastingProfiles() {
        DefaultFastingProfile.entries.forEach {
            fastingProfileDao.get().insert(
                FastingProfile(
                    name = it.displayName,
                    duration = it.duration?.inWholeMilliseconds,
                    description = it.description
                )
            )
        }
    }
}
