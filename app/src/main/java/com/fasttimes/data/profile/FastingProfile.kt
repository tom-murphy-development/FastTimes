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
package com.fasttimes.data.profile

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fasting_profiles")
data class FastingProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val displayName: String,
    val duration: Long?, // Duration in minutes
    val description: String,
    val isFavorite: Boolean = false,
)

/**
 * Extension property to get duration in minutes.
 * Safely handles null values.
 */
val FastingProfile.durationMinutes: Long
    get() = duration ?: 0L
