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

import com.fasttimes.data.DefaultFastingProfile
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

/**
 * Debug implementation of [FastingProfileProvider].
 * Returns the default profiles PLUS debug-only profiles (Test Timer, 1-Hour Timer).
 */
class DebugFastingProfileProvider @Inject constructor() : FastingProfileProvider {
    override fun getProfiles(): List<FastingProfile> {
        val defaultProfiles = DefaultFastingProfile.entries.map {
            FastingProfile(
                displayName = it.displayName,
                duration = it.duration?.inWholeMilliseconds,
                description = it.description
            )
        }

        val debugProfiles = listOf(
            FastingProfile(
                displayName = "Test Timer",
                duration = 10.seconds.inWholeMilliseconds,
                description = "A short-duration fast for testing purposes."
            ),
            FastingProfile(
                displayName = "Test 1 Hour",
                duration = 1.hours.inWholeMilliseconds,
                description = "A short-duration fast for testing purposes."
            )
        )

        return defaultProfiles + debugProfiles
    }
}
