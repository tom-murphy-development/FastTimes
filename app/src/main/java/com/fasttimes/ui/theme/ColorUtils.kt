package com.fasttimes.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Calculates the best content color (black or white) for a given background color.
 *
 * @param backgroundColor The color to calculate the content color for.
 * @return `Color.Black` if the background color is light, `Color.White` otherwise.
 */
@Composable
@ReadOnlyComposable
fun contentColorFor(backgroundColor: Color): Color {
    return if (backgroundColor.luminance() > 0.5f) Color.Black else Color.White
}
