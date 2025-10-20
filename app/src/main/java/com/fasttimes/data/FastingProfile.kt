package com.fasttimes.data

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

/**
 * Represents the different types of fasting profiles available in the app.
 *
 * @param duration The target duration of the fast. Null for manual (count-up) mode.
 * @param displayName A user-friendly name for the profile.
 * @param description A detailed explanation of the fasting profile.
 */
enum class FastingProfile(
    val duration: Duration?,
    val displayName: String,
    val description: String
) {
    MANUAL(
        duration = null,
        displayName = "Manual",
        description = "Start a fast and let it run until you manually stop it. Counts up from zero."
    ),
    SIXTEEN_EIGHT(
        duration = 16.hours,
        displayName = "16/8",
        description = "Fast for 16 hours and eat within an 8-hour window each day. A popular and sustainable method."
    ),
    FOURTEEN_TEN(
        duration = 14.hours,
        displayName = "14/10",
        description = "A less restrictive version of the 16/8 method, involving a 14-hour fast and a 10-hour eating window."
    ),
    TWELVE_TWELVE(
        duration = 12.hours,
        displayName = "12/12",
        description = "A simple and basic schedule with a 12-hour fasting period and a 12-hour eating window."
    ),
    EIGHTEEN_SIX(
        duration = 18.hours,
        displayName = "18/6",
        description = "Fast for 18 hours and eat within a 6-hour window. A more advanced method for experienced fasters."
    ),
    TWENTY_FOUR (
        duration = 24.hours,
        displayName = "24-Hour Fast",
        description = "A full day fast, typically done once or twice a week. Involves fasting for 24 hours straight."
    ),
    TEST_TIMER (
        duration = 10.seconds,
        displayName = "Test Timer",
        description = "A short-duration fast for testing purposes."
    )
}
