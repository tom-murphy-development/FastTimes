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
