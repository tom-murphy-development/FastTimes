package com.fasttimes.data.settings

import com.fasttimes.data.AppTheme
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val showLiveProgress: Flow<Boolean>
    suspend fun setShowLiveProgress(show: Boolean)

    val showGoalReachedNotification: Flow<Boolean>
    suspend fun setShowGoalReachedNotification(show: Boolean)

    val theme: Flow<AppTheme>
    suspend fun setTheme(theme: AppTheme)

    val confettiShownForFastId: Flow<Long?>
    suspend fun setConfettiShownForFastId(fastId: Long)
}
