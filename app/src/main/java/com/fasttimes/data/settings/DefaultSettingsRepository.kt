package com.fasttimes.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.fasttimes.data.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.Duration
import javax.inject.Inject

class DefaultSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private object PreferencesKeys {
        val THEME_KEY = stringPreferencesKey("theme")
        val FASTING_GOAL_KEY = longPreferencesKey("fasting_goal")
        val ACCENT_COLOR_KEY = longPreferencesKey("accent_color")
        val CONFETTI_SHOWN_FOR_FAST_ID = longPreferencesKey("confetti_shown_for_fast_id")
        val SHOW_FAB = booleanPreferencesKey("show_fab")
        val SHOW_LIVE_PROGRESS = booleanPreferencesKey("show_live_progress")
        val USE_WAVY_INDICATOR = booleanPreferencesKey("use_wavy_indicator")
        val SHOW_DASHBOARD_FAB = booleanPreferencesKey("show_dashboard_fab")
        val SHOW_GOAL_REACHED_NOTIFICATION = booleanPreferencesKey("show_goal_reached_notification")
        val FIRST_DAY_OF_WEEK = stringPreferencesKey("first_day_of_week")
    }

    override val userData: Flow<UserData> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val themeName = preferences[PreferencesKeys.THEME_KEY] ?: AppTheme.SYSTEM.name
            val theme = AppTheme.valueOf(themeName)

            val fastingGoalInSeconds = preferences[PreferencesKeys.FASTING_GOAL_KEY] ?: (16 * 60 * 60)
            val fastingGoal = Duration.ofSeconds(fastingGoalInSeconds)
            val accentColor = preferences[PreferencesKeys.ACCENT_COLOR_KEY]
            val useWavyIndicator = preferences[PreferencesKeys.USE_WAVY_INDICATOR] ?: true
            UserData(fastingGoal, theme, accentColor, useWavyIndicator)
        }

    override val showLiveProgress: Flow<Boolean> =
        dataStore.data.map { it[PreferencesKeys.SHOW_LIVE_PROGRESS] ?: false }

    override suspend fun setShowLiveProgress(show: Boolean) {
        dataStore.edit { it[PreferencesKeys.SHOW_LIVE_PROGRESS] = show }
    }

    override val showDashboardFab: Flow<Boolean> =
        dataStore.data.map { it[PreferencesKeys.SHOW_DASHBOARD_FAB] ?: true }

    override suspend fun setShowDashboardFab(show: Boolean) {
        dataStore.edit { it[PreferencesKeys.SHOW_DASHBOARD_FAB] = show }
    }

    override val showGoalReachedNotification: Flow<Boolean> =
        dataStore.data.map { it[PreferencesKeys.SHOW_GOAL_REACHED_NOTIFICATION] ?: true }

    override suspend fun setShowGoalReachedNotification(show: Boolean) {
        dataStore.edit { it[PreferencesKeys.SHOW_GOAL_REACHED_NOTIFICATION] = show }
    }

    override val theme: Flow<AppTheme> =
        dataStore.data.map { AppTheme.valueOf(it[PreferencesKeys.THEME_KEY] ?: AppTheme.SYSTEM.name) }

    override suspend fun setTheme(theme: AppTheme) {
        dataStore.edit { it[PreferencesKeys.THEME_KEY] = theme.name }
    }

    override val confettiShownForFastId: Flow<Long?> =
        dataStore.data.map { it[PreferencesKeys.CONFETTI_SHOWN_FOR_FAST_ID] }

    override suspend fun setConfettiShownForFastId(fastId: Long) {
        dataStore.edit { it[PreferencesKeys.CONFETTI_SHOWN_FOR_FAST_ID] = fastId }
    }

    override val firstDayOfWeek: Flow<String> =
        dataStore.data.map { it[PreferencesKeys.FIRST_DAY_OF_WEEK] ?: "Sunday" }

    override suspend fun setFirstDayOfWeek(day: String) {
        dataStore.edit { it[PreferencesKeys.FIRST_DAY_OF_WEEK] = day }
    }

    override val showFab: Flow<Boolean> =
        dataStore.data.map { it[PreferencesKeys.SHOW_FAB] ?: true }

    override suspend fun setShowFab(show: Boolean) {
        dataStore.edit { it[PreferencesKeys.SHOW_FAB] = show }
    }

    override suspend fun setUseWavyIndicator(useWavy: Boolean) {
        dataStore.edit { it[PreferencesKeys.USE_WAVY_INDICATOR] = useWavy }
    }
}
