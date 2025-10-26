package com.fasttimes.ui

import kotlin.time.Duration

/**
 * Formats a duration into a string format (HH:MM:SS).
 *
 * @param duration The duration object.
 * @return A formatted string representing the duration.
 */
fun formatDuration(duration: Duration): String {
    val seconds = duration.inWholeSeconds
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, secs)
}
