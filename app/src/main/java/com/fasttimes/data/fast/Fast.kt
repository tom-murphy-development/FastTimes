package com.fasttimes.data.fast

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fast_table")
data class Fast(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long? = null,
    val targetDuration: Long,
    val notes: String? = null
)
