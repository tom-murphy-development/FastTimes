package com.fasttimes.ui.settings

import com.fasttimes.data.AppTheme

data class SettingsUiState(
    val showLiveProgress: Boolean = false,
    val showGoalReachedNotification: Boolean = true,
    val theme: AppTheme = AppTheme.SYSTEM,
    val seedColor: Long? = null,
    val brandColor: Long? = null,
    val firstDayOfWeek: String = "Sunday",
    val showFab: Boolean = true,
    val useWavyIndicator: Boolean = true,
    val useExpressiveTheme: Boolean = false,
    val useSystemColors: Boolean = false
)
