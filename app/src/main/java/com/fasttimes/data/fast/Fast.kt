package com.fasttimes.data.fast

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasttimes.data.FastingProfile
import java.time.Instant
import java.time.ZonedDateTime

@Entity(tableName = "fasts")
data class Fast(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long?,
    val targetDuration: Long?,
    val profile: FastingProfile,
    val notes: String? = null
) {
    val start: ZonedDateTime
        get() = ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(startTime),
            java.time.ZoneId.systemDefault()
        )

    val end: ZonedDateTime?
        get() = endTime?.let {
            ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(it),
                java.time.ZoneId.systemDefault()
            )
        }

    fun goalMet(): Boolean {
        if (targetDuration == null || targetDuration == 0L || endTime == null) return false
        val actualDuration = endTime - startTime
        return actualDuration >= targetDuration
    }

    fun duration(): Long {
        return (endTime ?: System.currentTimeMillis()) - startTime
    }
}
