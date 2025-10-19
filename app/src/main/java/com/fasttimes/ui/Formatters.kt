package com.fasttimes.ui

import java.util.concurrent.TimeUnit

/**
 * Formats a duration in milliseconds into a string format (HH:mm:ss).
 *
 * @param millis The duration in milliseconds.
 * @return A formatted string representing the duration.
 */
fun formatDuration(millis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60

    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}
