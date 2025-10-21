package com.fasttimes.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.fasttimes.data.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DefaultSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private val showLiveProgressKey = booleanPreferencesKey("show_live_progress")
    private val themeKey = stringPreferencesKey("theme")

    override val showLiveProgress: Flow<Boolean> =
        dataStore.data.map { it[showLiveProgressKey] ?: false }

    override suspend fun setShowLiveProgress(show: Boolean) {
        dataStore.edit { it[showLiveProgressKey] = show }
    }

    override val theme: Flow<AppTheme> =
        dataStore.data.map { AppTheme.valueOf(it[themeKey] ?: AppTheme.SYSTEM.name) }

    override suspend fun setTheme(theme: AppTheme) {
        dataStore.edit { it[themeKey] = theme.name }
    }
}
