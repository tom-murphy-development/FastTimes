package com.fasttimes.ui

import java.util.Locale
import kotlin.time.Duration

/**
 * Formats a duration into a string format (HH:MM:SS).
 *
 * @param duration The duration object.
 * @return A formatted string representing the duration.
 */
fun formatDuration(duration: Duration): String {
    val totalSeconds = duration.inWholeSeconds
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.forLanguageTag("en-AU"), "%02d:%02d:%02d", hours, minutes, seconds)
}
