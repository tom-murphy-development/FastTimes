package com.fasttimes.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.fasttimes.data.AppTheme

@Composable
fun FastTimesPreviewTheme(content: @Composable () -> Unit) {
    FastTimesTheme(
        theme = AppTheme.SYSTEM,
        seedColor = Color.Blue,
        accentColor = Color.Blue,
        useExpressiveTheme = false,
        useSystemColors = false,
        content = content
    )
}
