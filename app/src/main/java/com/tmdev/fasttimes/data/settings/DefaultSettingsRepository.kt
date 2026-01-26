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
package com.tmdev.fasttimes.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tmdev.fasttimes.data.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.time.Duration
import javax.inject.Inject

class DefaultSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private object PreferencesKeys {
        val THEME_KEY = stringPreferencesKey("theme")
        val FASTING_GOAL_KEY = longPreferencesKey("fasting_goal")
        val SEED_COLOR_KEY = longPreferencesKey("seed_color")
        val ACCENT_COLOR_KEY = longPreferencesKey("accent_color")
        val CONFETTI_SHOWN_FOR_FAST_ID = longPreferencesKey("confetti_shown_for_fast_id")
        val SHOW_FAB = booleanPreferencesKey("show_fab")
        val SHOW_LIVE_PROGRESS = booleanPreferencesKey("show_live_progress")
        val USE_WAVY_INDICATOR = booleanPreferencesKey("use_wavy_indicator")
        val SHOW_DASHBOARD_FAB = booleanPreferencesKey("show_dashboard_fab")
        val SHOW_GOAL_REACHED_NOTIFICATION = booleanPreferencesKey("show_goal_reached_notification")
        val FIRST_DAY_OF_WEEK = stringPreferencesKey("first_day_of_week")
        val USE_EXPRESSIVE_THEME = booleanPreferencesKey("use_expressive_theme")
        val USE_SYSTEM_COLORS = booleanPreferencesKey("use_system_colors")
    }

    // Centralized safe flow access to handle DataStore corruption/decryption errors
    private val safeDataStoreData: Flow<Preferences> = dataStore.data
        .catch { exception ->
            // Catch all exceptions (including BadPaddingException) and emit empty preferences
            // This allows the app to reset/recover instead of crashing.
            emit(emptyPreferences())
        }

    override val userData: Flow<UserData> = safeDataStoreData
        .map { preferences ->
            val themeName = preferences[PreferencesKeys.THEME_KEY] ?: AppTheme.SYSTEM.name
            val theme = AppTheme.valueOf(themeName)

            val fastingGoalInSeconds = preferences[PreferencesKeys.FASTING_GOAL_KEY] ?: (16 * 60 * 60)
            val fastingGoal = Duration.ofSeconds(fastingGoalInSeconds)
            val seedColor = preferences[PreferencesKeys.SEED_COLOR_KEY]
            val accentColor = preferences[PreferencesKeys.ACCENT_COLOR_KEY]
            val useWavyIndicator = preferences[PreferencesKeys.USE_WAVY_INDICATOR] ?: true
            val useExpressiveTheme = preferences[PreferencesKeys.USE_EXPRESSIVE_THEME] ?: false
            val useSystemColors = preferences[PreferencesKeys.USE_SYSTEM_COLORS] ?: true
            UserData(
                fastingGoal = fastingGoal,
                theme = theme,
                seedColor = seedColor,
                accentColor = accentColor,
                useWavyIndicator = useWavyIndicator,
                useExpressiveTheme = useExpressiveTheme,
                useSystemColors = useSystemColors
            )
        }

    override val showLiveProgress: Flow<Boolean> =
        safeDataStoreData.map { it[PreferencesKeys.SHOW_LIVE_PROGRESS] ?: false }

    override suspend fun setShowLiveProgress(show: Boolean) {
        dataStore.edit { it[PreferencesKeys.SHOW_LIVE_PROGRESS] = show }
    }

    override val showDashboardFab: Flow<Boolean> =
        safeDataStoreData.map { it[PreferencesKeys.SHOW_DASHBOARD_FAB] ?: true }

    override suspend fun setShowDashboardFab(show: Boolean) {
        dataStore.edit { it[PreferencesKeys.SHOW_DASHBOARD_FAB] = show }
    }

    override val showGoalReachedNotification: Flow<Boolean> =
        safeDataStoreData.map { it[PreferencesKeys.SHOW_GOAL_REACHED_NOTIFICATION] ?: true }

    override suspend fun setShowGoalReachedNotification(show: Boolean) {
        dataStore.edit { it[PreferencesKeys.SHOW_GOAL_REACHED_NOTIFICATION] = show }
    }

    override val theme: Flow<AppTheme> =
        safeDataStoreData.map { AppTheme.valueOf(it[PreferencesKeys.THEME_KEY] ?: AppTheme.SYSTEM.name) }

    override suspend fun setTheme(theme: AppTheme) {
        dataStore.edit { it[PreferencesKeys.THEME_KEY] = theme.name }
    }

    override suspend fun setSeedColor(color: Long) {
        dataStore.edit { it[PreferencesKeys.SEED_COLOR_KEY] = color }
    }

    override suspend fun clearSeedColor() {
        dataStore.edit { it.remove(PreferencesKeys.SEED_COLOR_KEY) }
    }

    override suspend fun setAccentColor(color: Long) {
        dataStore.edit { it[PreferencesKeys.ACCENT_COLOR_KEY] = color }
    }

    override suspend fun clearAccentColor() {
        dataStore.edit { it.remove(PreferencesKeys.ACCENT_COLOR_KEY) }
    }

    override val confettiShownForFastId: Flow<Long?> =
        safeDataStoreData.map { it[PreferencesKeys.CONFETTI_SHOWN_FOR_FAST_ID] }

    override suspend fun setConfettiShownForFastId(fastId: Long) {
        dataStore.edit { it[PreferencesKeys.CONFETTI_SHOWN_FOR_FAST_ID] = fastId }
    }

    override val firstDayOfWeek: Flow<String> =
        safeDataStoreData.map { it[PreferencesKeys.FIRST_DAY_OF_WEEK] ?: "Monday" }

    override suspend fun setFirstDayOfWeek(day: String) {
        dataStore.edit { it[PreferencesKeys.FIRST_DAY_OF_WEEK] = day }
    }

    override val showFab: Flow<Boolean> =
        safeDataStoreData.map { it[PreferencesKeys.SHOW_FAB] ?: true }

    override suspend fun setShowFab(show: Boolean) {
        dataStore.edit { it[PreferencesKeys.SHOW_FAB] = show }
    }

    override suspend fun setUseWavyIndicator(useWavy: Boolean) {
        dataStore.edit { it[PreferencesKeys.USE_WAVY_INDICATOR] = useWavy }
    }

    override suspend fun setUseExpressiveTheme(useExpressive: Boolean) {
        dataStore.edit { it[PreferencesKeys.USE_EXPRESSIVE_THEME] = useExpressive }
    }

    override suspend fun setUseSystemColors(useSystemColors: Boolean) {
        dataStore.edit { it[PreferencesKeys.USE_SYSTEM_COLORS] = useSystemColors }
    }
}
