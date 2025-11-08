package com.fasttimes.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.fasttimes.data.AppTheme
import com.fasttimes.ui.settings.SettingsViewModel
import com.materialkolor.DynamicMaterialTheme
import com.materialkolor.PaletteStyle

private val LocalBrandColor = staticCompositionLocalOf<Color> {
    error("No BrandColor provided")
}

@Composable
fun FastTimesTheme(
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val uiState by settingsViewModel.uiState.collectAsState()
    val isDark = when (uiState.theme) {
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }

    val brandColor = uiState.brandColor?.let { Color(it) } ?: BrandColor

    val seedColor = if (uiState.useSystemColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Use system dynamic color as the seed
        val context = LocalContext.current
        if (isDark) {
            dynamicDarkColorScheme(context).primary
        } else {
            dynamicLightColorScheme(context).primary
        }
    } else {
        // Use user-selected seed color or default
        uiState.seedColor?.let { Color(it) } ?: BrandColor
    }

    val style = if (uiState.useExpressiveTheme) PaletteStyle.Expressive else PaletteStyle.Vibrant

    CompositionLocalProvider(LocalBrandColor provides brandColor) {
        DynamicMaterialTheme(
            seedColor = seedColor,
            isDark = isDark,
            style = style,
            animate = true,
        ) {
            SystemBarsTheme(isDark = isDark, colorScheme = MaterialTheme.colorScheme)
            content()
        }
    }
}

@Composable
private fun SystemBarsTheme(isDark: Boolean, colorScheme: ColorScheme) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !isDark
            insetsController.isAppearanceLightNavigationBars = !isDark
        }
    }
}

object FastTimesTheme {
    val brandColor: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalBrandColor.current
}
