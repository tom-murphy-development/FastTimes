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
package com.tmdev.fasttimes.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tmdev.fasttimes.data.fast.Fast
import com.tmdev.fasttimes.data.fast.FastDao
import com.tmdev.fasttimes.data.profile.FastingProfile
import com.tmdev.fasttimes.data.profile.FastingProfileDao

@Database(
    entities = [Fast::class, FastingProfile::class],
    version = 6,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fastDao(): FastDao
    abstract fun fastingProfileDao(): FastingProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context, callback: AppDatabaseCallback): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fasttimes-db"
                )
                    .addCallback(callback)
                    .addMigrations(MIGRATION_4_5, MIGRATION_5_6)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE fasting_profiles ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Rebrand "No Goal" to "Open Fast" in profiles
                db.execSQL("UPDATE fasting_profiles SET displayName = 'Open Fast' WHERE displayName = 'No Goal'")
                // Also update history records that used the old names
                db.execSQL("UPDATE fasts SET profileName = 'Open Fast' WHERE profileName = 'No Goal' OR profileName = 'Manual'")
            }
        }
    }
}
