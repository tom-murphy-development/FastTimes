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
package com.fasttimes.data

import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

/**
 * Represents the different types of fasting profiles available in the app.
 *
 * @param duration The target duration of the fast. Null for manual (count-up) mode.
 * @param displayName A user-friendly name for the profile.
 * @param description A detailed explanation of the fasting profile.
 */
@Serializable
enum class DefaultFastingProfile(
    val duration: Duration?,
    val displayName: String,
    val description: String
) {
    MANUAL(
        duration = null,
        displayName = "No Goal",
        description = "Start a fast and let it run until you manually stop it. Counts up from zero."
    ),
    SIXTEEN_EIGHT(
        duration = 16.hours,
        displayName = "16-Hour Fast",
        description = "Fast for 16 hours and eat within an 8-hour window each day. A popular and sustainable method."
    ),
    FOURTEEN_TEN(
        duration = 14.hours,
        displayName = "14-Hour Fast",
        description = "A less restrictive version of the 16/8 method, involving a 14-hour fast and a 10-hour eating window."
    ),
    TWELVE_TWELVE(
        duration = 12.hours,
        displayName = "12-Hour Fast",
        description = "A simple and basic schedule with a 12-hour fasting period and a 12-hour eating window."
    ),
    EIGHTEEN_SIX(
        duration = 18.hours,
        displayName = "18-Hour Fast",
        description = "Fast for 18 hours and eat within a 6-hour window. A more advanced method for experienced fasters."
    ),
    TWENTY_FOUR(
        duration = 24.hours,
        displayName = "24-Hour Fast",
        description = "A full day fast, typically done once or twice a week. Involves fasting for 24 hours straight."
    );

    companion object {
        fun getById(id: String): DefaultFastingProfile? {
            return entries.find { it.displayName == id }
        }
    }
}
