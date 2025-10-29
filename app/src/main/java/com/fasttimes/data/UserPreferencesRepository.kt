package com.fasttimes.data

import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

enum class Theme {
    LIGHT, DARK, SYSTEM
}

data class UserData(
    val fastingGoal: Duration,
    val theme: Theme,
    val accentColor: Long,
)

@Singleton
class UserPreferencesRepository @Inject constructor(private val dataStore: DataStore<Preferences>) {

    private object PreferencesKeys {
        val THEME_KEY = stringPreferencesKey("theme")
        val FASTING_GOAL_KEY = longPreferencesKey("fasting_goal")
        val ACCENT_COLOR_KEY = longPreferencesKey("accent_color")
    }

    private val defaultAccentColor = Color(0xFF3DDC84).value.toLong()

    val userData: Flow<UserData> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val themeName = preferences[PreferencesKeys.THEME_KEY] ?: Theme.SYSTEM.name
            val theme = Theme.valueOf(themeName)
            val fastingGoalInSeconds = preferences[PreferencesKeys.FASTING_GOAL_KEY] ?: (16 * 60 * 60)
            val fastingGoal = Duration.ofSeconds(fastingGoalInSeconds)
            val accentColor = preferences[PreferencesKeys.ACCENT_COLOR_KEY] ?: defaultAccentColor
            UserData(fastingGoal, theme, accentColor)
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
}
