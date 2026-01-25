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
package com.fasttimes.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

enum class Theme {
    LIGHT, DARK, SYSTEM
}

data class UserData(
    val fastingGoal: Duration,
    val theme: Theme,
    val accentColor: Long?,
    val useWavyIndicator: Boolean
)

@Singleton
class UserPreferencesRepository @Inject constructor(private val dataStore: DataStore<Preferences>) {

    private object PreferencesKeys {
        val THEME_KEY = stringPreferencesKey("theme")
        val FASTING_GOAL_KEY = longPreferencesKey("fasting_goal")
        val ACCENT_COLOR_KEY = longPreferencesKey("accent_color")
        val USE_WAVY_INDICATOR = booleanPreferencesKey("use_wavy_indicator")

    }

    val userData: Flow<UserData> = dataStore.data
        .catch { exception ->
            // DataStore issues, including BadPaddingException (decryption failure), will be caught here.
            // The corruptionHandler in AppModule should ideally handle file corruption/replacement,
            // but this catch block prevents the app from crashing during flow collection.
            emit(emptyPreferences())
        }
        .map { preferences ->
            val themeName = preferences[PreferencesKeys.THEME_KEY] ?: Theme.SYSTEM.name
            val theme = Theme.valueOf(themeName)
            val fastingGoalInSeconds = preferences[PreferencesKeys.FASTING_GOAL_KEY] ?: (16 * 60 * 60)
            val fastingGoal = Duration.ofSeconds(fastingGoalInSeconds)
            val accentColor = preferences[PreferencesKeys.ACCENT_COLOR_KEY]
            val useWavyIndicator = preferences[PreferencesKeys.USE_WAVY_INDICATOR] ?: true
            UserData(fastingGoal, theme, accentColor, useWavyIndicator)
        }

    suspend fun setTheme(theme: Theme) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_KEY] = theme.name
        }
    }

    suspend fun setFastingGoal(duration: Duration) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FASTING_GOAL_KEY] = duration.seconds
        }
    }

    suspend fun setAccentColor(color: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACCENT_COLOR_KEY] = color
        }
    }

    // New function to clear the user's choice
    suspend fun clearAccentColor() {
        dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.ACCENT_COLOR_KEY)
        }
    }
    suspend fun setUseWavyIndicator(useWavy: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_WAVY_INDICATOR] = useWavy
        }
    }
}
