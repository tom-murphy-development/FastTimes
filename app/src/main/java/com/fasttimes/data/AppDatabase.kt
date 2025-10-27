package com.fasttimes.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fasttimes.data.fast.Fast
import com.fasttimes.data.fast.FastDao

@Database(
    entities = [Fast::class],
    version = 3,
    exportSchema = true,

)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fastDao(): FastDao

    // companion object {
    //    val MIGRATION_1_2 = object : Migration(1, 2) {
    //        override fun migrate(db: SupportSQLiteDatabase) {
    //            db.execSQL("ALTER TABLE fasts ADD COLUMN profile TEXT NOT NULL DEFAULT 'MANUAL'")
    //        }
    //    }
    //}
}
