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

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Spacing(
    val default: Dp = 0.dp,
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp,
    val dialogHorizontal: Dp = 16.dp,
    val screenHorizontal: Dp = 16.dp,
    val sectionSpacing: Dp = 16.dp,
    val cardPadding: Dp = 16.dp,
    val timerSize: Dp = 260.dp,
    val dashboardStatCardHeight: Dp = 140.dp,
    val performanceStatCardHeight: Dp = 160.dp,
    val chartHeight: Dp = 200.dp,
    val statValueFontSize: TextUnit = 30.sp,
    val statUnitFontSize: TextUnit = 10.sp,
    val timerDurationFontSize: TextUnit = 36.sp,
    val heroStatFontSize: TextUnit = 56.sp,
    val goalCardValueFontSize: TextUnit = 32.sp,
    val goalCardLabelFontSize: TextUnit = 12.sp,
    val headlineMediumFontSize: TextUnit = 28.sp,
    val headlineSmallFontSize: TextUnit = 24.sp,
    val headlineLargeFontSize: TextUnit = 32.sp,
    val titleLargeFontSize: TextUnit = 22.sp,
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }

val MaterialTheme.spacing: Spacing
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current
