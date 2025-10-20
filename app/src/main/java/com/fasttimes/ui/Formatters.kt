package com.fasttimes.ui

import kotlin.time.Duration

/**
 * Formats a duration into a string format (HH:mm:ss).
 *
 * @param duration The duration object.
 * @return A formatted string representing the duration.
 */
fun formatDuration(duration: Duration): String {
    return duration.toComponents { hours, minutes, seconds, _ ->
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
