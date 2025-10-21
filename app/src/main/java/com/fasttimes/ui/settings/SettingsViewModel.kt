package com.fasttimes.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasttimes.data.AppTheme
import com.fasttimes.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val showLiveProgress: Boolean = false,
    val theme: AppTheme = AppTheme.SYSTEM
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.showLiveProgress,
        settingsRepository.theme
    ) { showLiveProgress, theme ->
        SettingsUiState(showLiveProgress, theme)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun onShowLiveProgressChanged(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setShowLiveProgress(enabled)
        }
    }

    fun onThemeChanged(theme: AppTheme) {
        viewModelScope.launch {
            settingsRepository.setTheme(theme)
        }
    }
}
