package com.fasttimes.ui

import kotlin.time.Duration

/**
 * Formats a duration into a string format (HHh MMm).
 *
 * @param duration The duration object.
 * @return A formatted string representing the duration.
 */
fun formatDuration(duration: Duration): String {
    val hours = duration.inWholeHours
    val minutes = duration.inWholeMinutes % 60
    return String.format("%dh %dm", hours, minutes)
}
