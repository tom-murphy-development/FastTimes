package com.fasttimes.ui.settings

import com.fasttimes.data.AppTheme

data class SettingsUiState(
    val showLiveProgress: Boolean = false,
    val showGoalReachedNotification: Boolean = true,
    val theme: AppTheme = AppTheme.SYSTEM,
    val firstDayOfWeek: String = "Sunday"
)
