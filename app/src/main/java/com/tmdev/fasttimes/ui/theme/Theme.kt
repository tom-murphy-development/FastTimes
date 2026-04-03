/*
 * Copyright (C) 2025 tom-murphy-development
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.tmdev.fasttimes.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.materialkolor.DynamicMaterialTheme
import com.materialkolor.PaletteStyle
import com.tmdev.fasttimes.data.AppTheme

private val LocalAccentColor = staticCompositionLocalOf<Color> {
    error("No AccentColor provided")
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun FastTimesTheme(
    theme: AppTheme,
    seedColor: Color,
    accentColor: Color,
    useExpressiveTheme: Boolean,
    useSystemColors: Boolean,
    windowWidthSizeClass: WindowWidthSizeClass? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val calculatedWindowSizeClass = (context as? Activity)?.let { calculateWindowSizeClass(it) }
    val widthSizeClass = windowWidthSizeClass ?: calculatedWindowSizeClass?.widthSizeClass
    
    val spacing = when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> Spacing(
            dialogHorizontal = 16.dp,
            screenHorizontal = 12.dp,
            sectionSpacing = 10.dp,
            cardPadding = 12.dp,
            timerSize = 220.dp,
            dashboardStatCardHeight = 120.dp,
            performanceStatCardHeight = 140.dp,
            chartHeight = 170.dp,
            statValueFontSize = 22.sp,
            statUnitFontSize = 8.sp,
            timerDurationFontSize = 30.sp,
            heroStatFontSize = 44.sp,
            goalCardValueFontSize = 26.sp,
            goalCardLabelFontSize = 10.sp,
            headlineLargeFontSize = 26.sp,
            headlineMediumFontSize = 22.sp,
            headlineSmallFontSize = 18.sp,
            titleLargeFontSize = 16.sp
        )
        WindowWidthSizeClass.Medium -> Spacing(
            dialogHorizontal = 32.dp,
            screenHorizontal = 24.dp,
            sectionSpacing = 16.dp,
            cardPadding = 16.dp,
            timerSize = 280.dp,
            dashboardStatCardHeight = 150.dp,
            performanceStatCardHeight = 170.dp,
            chartHeight = 220.dp,
            statValueFontSize = 30.sp,
            statUnitFontSize = 11.sp,
            timerDurationFontSize = 40.sp,
            heroStatFontSize = 64.sp,
            goalCardValueFontSize = 32.sp,
            goalCardLabelFontSize = 12.sp,
            headlineLargeFontSize = 32.sp,
            headlineMediumFontSize = 28.sp,
            headlineSmallFontSize = 24.sp,
            titleLargeFontSize = 22.sp
        )
        WindowWidthSizeClass.Expanded -> Spacing(
            dialogHorizontal = 48.dp,
            screenHorizontal = 32.dp,
            sectionSpacing = 24.dp,
            cardPadding = 20.dp,
            timerSize = 320.dp,
            dashboardStatCardHeight = 170.dp,
            performanceStatCardHeight = 190.dp,
            chartHeight = 260.dp,
            statValueFontSize = 34.sp,
            statUnitFontSize = 12.sp,
            timerDurationFontSize = 48.sp,
            heroStatFontSize = 72.sp,
            goalCardValueFontSize = 36.sp,
            goalCardLabelFontSize = 13.sp,
            headlineLargeFontSize = 36.sp,
            headlineMediumFontSize = 32.sp,
            headlineSmallFontSize = 28.sp,
            titleLargeFontSize = 24.sp
        )
        else -> Spacing()
    }

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

    CompositionLocalProvider(
        LocalAccentColor provides accentColor,
        LocalSpacing provides spacing
    ) {
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
