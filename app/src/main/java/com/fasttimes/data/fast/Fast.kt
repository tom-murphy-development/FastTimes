package com.fasttimes.data.fast

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasttimes.data.FastingProfile

@Entity(tableName = "fast_table")
data class Fast(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long? = null,
    val profile: FastingProfile = FastingProfile.MANUAL,
    val targetDuration: Long? = null,
    val notes: String? = null
)
