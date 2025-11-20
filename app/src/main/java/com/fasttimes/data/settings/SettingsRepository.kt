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
package com.fasttimes.data.settings

import com.fasttimes.data.AppTheme
import kotlinx.coroutines.flow.Flow
import java.time.Duration

data class UserData(
    val fastingGoal: Duration,
    val theme: AppTheme,
    val seedColor: Long?,
    val accentColor: Long?,
    val useWavyIndicator: Boolean,
    val useExpressiveTheme: Boolean,
    val useSystemColors: Boolean
)

interface SettingsRepository {
    val userData: Flow<UserData>

    val showLiveProgress: Flow<Boolean>
    suspend fun setShowLiveProgress(show: Boolean)

    val showDashboardFab: Flow<Boolean>
    suspend fun setShowDashboardFab(show: Boolean)

    val showGoalReachedNotification: Flow<Boolean>
    suspend fun setShowGoalReachedNotification(show: Boolean)

    val theme: Flow<AppTheme>
    suspend fun setTheme(theme: AppTheme)

    suspend fun setSeedColor(color: Long)
    suspend fun clearSeedColor()

    suspend fun setAccentColor(color: Long)
    suspend fun clearAccentColor()

    val confettiShownForFastId: Flow<Long?>
    suspend fun setConfettiShownForFastId(fastId: Long)

    val firstDayOfWeek: Flow<String>
    suspend fun setFirstDayOfWeek(day: String)

    val showFab: Flow<Boolean>
    suspend fun setShowFab(show: Boolean)

    suspend fun setUseWavyIndicator(useWavy: Boolean)

    suspend fun setUseExpressiveTheme(useExpressive: Boolean)

    suspend fun setUseSystemColors(useSystemColors: Boolean)
}
