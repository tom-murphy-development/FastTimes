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
package com.tmdev.fasttimes.ui.settings

import com.tmdev.fasttimes.data.AppTheme

data class SettingsUiState(
    val showLiveProgress: Boolean = false,
    val showGoalReachedNotification: Boolean = true,
    val theme: AppTheme = AppTheme.SYSTEM,
    val seedColor: Long? = null,
    val accentColor: Long? = null,
    val firstDayOfWeek: String = "Sunday",
    val showFab: Boolean = true,
    val useWavyIndicator: Boolean = true,
    val useExpressiveTheme: Boolean = false,
    val useSystemColors: Boolean = false
)
