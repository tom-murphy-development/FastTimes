package com.fasttimes.data.settings

import com.fasttimes.data.AppTheme
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val showLiveProgress: Flow<Boolean>
    suspend fun setShowLiveProgress(show: Boolean)

    val theme: Flow<AppTheme>
    suspend fun setTheme(theme: AppTheme)
}
