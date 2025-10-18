package com.fasttimes.data

/**
 * Represents the different types of fasting profiles available in the app.
 *
 * @param durationHours The target duration of the fast in hours. Null for manual (count-up) mode.
 * @param displayName A user-friendly name for the profile.
 * @param description A detailed explanation of the fasting profile.
 */
enum class FastingProfile(
    val durationHours: Int?,
    val displayName: String,
    val description: String
) {
    MANUAL(
        durationHours = null,
        displayName = "Manual",
        description = "Start a fast and let it run until you manually stop it. Counts up from zero."
    ),
    SIXTEEN_EIGHT(
        durationHours = 16,
        displayName = "16/8",
        description = "Fast for 16 hours and eat within an 8-hour window each day. A popular and sustainable method."
    ),
    FOURTEEN_TEN(
        durationHours = 14,
        displayName = "14/10",
        description = "A less restrictive version of the 16/8 method, involving a 14-hour fast and a 10-hour eating window."
    ),
    TWELVE_TWELVE(
        durationHours = 12,
        displayName = "12/12",
        description = "A simple and basic schedule with a 12-hour fasting period and a 12-hour eating window."
    )
}
