package com.fasttimes.data.settings

import com.fasttimes.data.AppTheme
import kotlinx.coroutines.flow.Flow
import java.time.Duration

data class UserData(
    val fastingGoal: Duration,
    val theme: AppTheme,
    val accentColor: Long?,
    val useWavyIndicator: Boolean
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

    val confettiShownForFastId: Flow<Long?>
    suspend fun setConfettiShownForFastId(fastId: Long)

    val firstDayOfWeek: Flow<String>
    suspend fun setFirstDayOfWeek(day: String)

    val showFab: Flow<Boolean>
    suspend fun setShowFab(show: Boolean)

    suspend fun setUseWavyIndicator(useWavy: Boolean)
}
