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
package com.tmdev.fasttimes.data.fast

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZonedDateTime

@Serializable
@Entity(tableName = "fasts")
data class Fast(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long?,
    val targetDuration: Long?,
    val profileName: String,
    val notes: String? = null,
    val rating: Int? = null
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
