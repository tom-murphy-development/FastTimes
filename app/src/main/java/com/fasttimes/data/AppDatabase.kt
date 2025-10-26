package com.fasttimes.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fasttimes.data.fast.Fast
import com.fasttimes.data.fast.FastDao

@Database(entities = [Fast::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fastDao(): FastDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE fasts ADD COLUMN profile TEXT NOT NULL DEFAULT 'MANUAL'")
            }
        }
    }
}
