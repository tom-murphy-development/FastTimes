/*
 * Copyright (C) 2025 tom-murphy-development
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.fasttimes.data

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fasttimes.data.profile.FastingProfileDao
import com.fasttimes.data.profile.FastingProfileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

class AppDatabaseCallback @Inject constructor(
    private val fastingProfileDao: Provider<FastingProfileDao>,
    private val applicationScope: CoroutineScope,
    private val fastingProfileProvider: FastingProfileProvider
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        applicationScope.launch {
            prepopulateFastingProfiles()
        }
    }

    private suspend fun prepopulateFastingProfiles() {
        fastingProfileProvider.getProfiles().forEach {
            fastingProfileDao.get().insert(it)
        }
    }
}
