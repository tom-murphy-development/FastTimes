package com.fasttimes.ui.theme

import androidx.compose.runtime.Composable
import com.fasttimes.data.AppTheme

@Composable
fun FastTimesPreviewTheme(content: @Composable () -> Unit) {
    FastTimesTheme(
        theme = AppTheme.SYSTEM,
        seedColor = BrandColor,
        brandColor = BrandColor,
        useExpressiveTheme = false,
        useSystemColors = false,
        content = content
    )
}
