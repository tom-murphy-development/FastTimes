package com.fasttimes.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.fasttimes.data.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DefaultSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private val showLiveProgressKey = booleanPreferencesKey("show_live_progress")
    private val showDashboardFabKey = booleanPreferencesKey("show_dashboard_fab")
    private val showGoalReachedNotificationKey = booleanPreferencesKey("show_goal_reached_notification")
    private val themeKey = stringPreferencesKey("theme")
    private val confettiShownForFastIdKey = longPreferencesKey("confetti_shown_for_fast_id")
    private val firstDayOfWeekKey = stringPreferencesKey("first_day_of_week")
    private val showFabKey = booleanPreferencesKey("show_fab")

    override val showLiveProgress: Flow<Boolean> =
        dataStore.data.map { it[showLiveProgressKey] ?: false }

    override suspend fun setShowLiveProgress(show: Boolean) {
        dataStore.edit { it[showLiveProgressKey] = show }
    }

    override val showDashboardFab: Flow<Boolean> =
        dataStore.data.map { it[showDashboardFabKey] ?: true }

    override suspend fun setShowDashboardFab(show: Boolean) {
        dataStore.edit { it[showDashboardFabKey] = show }
    }

    override val showGoalReachedNotification: Flow<Boolean> =
        dataStore.data.map { it[showGoalReachedNotificationKey] ?: true }

    override suspend fun setShowGoalReachedNotification(show: Boolean) {
        dataStore.edit { it[showGoalReachedNotificationKey] = show }
    }

    override val theme: Flow<AppTheme> =
        dataStore.data.map { AppTheme.valueOf(it[themeKey] ?: AppTheme.SYSTEM.name) }

    override suspend fun setTheme(theme: AppTheme) {
        dataStore.edit { it[themeKey] = theme.name }
    }

    override val confettiShownForFastId: Flow<Long?> =
        dataStore.data.map { it[confettiShownForFastIdKey] }

    override suspend fun setConfettiShownForFastId(fastId: Long) {
        dataStore.edit { it[confettiShownForFastIdKey] = fastId }
    }

    override val firstDayOfWeek: Flow<String> =
        dataStore.data.map { it[firstDayOfWeekKey] ?: "Sunday" }

    override suspend fun setFirstDayOfWeek(day: String) {
        dataStore.edit { it[firstDayOfWeekKey] = day }
    }

    override val showFab: Flow<Boolean> =
        dataStore.data.map { it[showFabKey] ?: true }

    override suspend fun setShowFab(show: Boolean) {
        dataStore.edit { it[showFabKey] = show }
    }
}
