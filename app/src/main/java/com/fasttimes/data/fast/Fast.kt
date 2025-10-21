package com.fasttimes.data.fast

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasttimes.data.FastingProfile
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

@Entity(tableName = "fast_table")
data class Fast(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long? = null,
    val profile: FastingProfile = FastingProfile.MANUAL,
    val targetDuration: Long? = null,
    val notes: String? = null
) {
    val start: ZonedDateTime
        get() = ZonedDateTime.ofInstant(Instant.ofEpochMilli(startTime), ZoneId.systemDefault())

    val end: ZonedDateTime?
        get() = endTime?.let { ZonedDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault()) }

    fun goalMet(): Boolean {
        if (endTime == null || targetDuration == null) {
            return false
        }
        val duration = endTime - startTime
        return duration >= targetDuration
    }
}
