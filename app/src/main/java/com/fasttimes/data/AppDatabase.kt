package com.fasttimes.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fasttimes.data.fast.Fast
import com.fasttimes.data.fast.FastDao
import com.fasttimes.data.profile.FastingProfile
import com.fasttimes.data.profile.FastingProfileDao

@Database(
    entities = [Fast::class, FastingProfile::class],
    version = 5,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fastDao(): FastDao
    abstract fun fastingProfileDao(): FastingProfileDao

    companion object {
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE fasting_profiles ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
