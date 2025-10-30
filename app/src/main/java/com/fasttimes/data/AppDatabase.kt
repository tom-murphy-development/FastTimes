package com.fasttimes.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fasttimes.data.fast.Fast
import com.fasttimes.data.fast.FastDao
import com.fasttimes.data.profile.FastingProfile
import com.fasttimes.data.profile.FastingProfileDao

@Database(
    entities = [Fast::class, FastingProfile::class],
    version = 4, // TODO: Add migration from 3 to 4
    exportSchema = true,

)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fastDao(): FastDao
    abstract fun fastingProfileDao(): FastingProfileDao

    // companion object {
    //    val MIGRATION_1_2 = object : Migration(1, 2) {
    //        override fun migrate(db: SupportSQLiteDatabase) {
    //            db.execSQL("ALTER TABLE fasts ADD COLUMN profile TEXT NOT NULL DEFAULT 'MANUAL'")
    //        }
    //    }
    //}
}
