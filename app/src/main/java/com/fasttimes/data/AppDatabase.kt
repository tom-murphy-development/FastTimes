package com.fasttimes.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fasttimes.data.fast.Fast
import com.fasttimes.data.fast.FastDao

@Database(entities = [Fast::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fastDao(): FastDao
}
