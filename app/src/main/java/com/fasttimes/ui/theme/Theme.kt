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
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.fasttimes.data.AppTheme
import com.materialkolor.DynamicMaterialTheme
import com.materialkolor.PaletteStyle

private val LocalAccentColor = staticCompositionLocalOf<Color> {
    error("No AccentColor provided")
}

@Composable
fun FastTimesTheme(
    theme: AppTheme,
    seedColor: Color,
    accentColor: Color,
    useExpressiveTheme: Boolean,
    useSystemColors: Boolean,
    content: @Composable () -> Unit
) {
    val isDark = when (theme) {
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }

    val finalSeedColor = if (useSystemColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Use system dynamic color as the seed
        val context = LocalContext.current
        if (isDark) {
            dynamicDarkColorScheme(context).primary
        } else {
            dynamicLightColorScheme(context).primary
        }
    } else {
        // Use user-selected seed color or default
        seedColor
    }

    val style = if (useExpressiveTheme) PaletteStyle.Expressive else PaletteStyle.Vibrant

    CompositionLocalProvider(LocalAccentColor provides accentColor) {
        DynamicMaterialTheme(
            seedColor = finalSeedColor,
            isDark = isDark,
            style = style,
            animate = false,
            typography = Typography
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
    val accentColor: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAccentColor.current
}
