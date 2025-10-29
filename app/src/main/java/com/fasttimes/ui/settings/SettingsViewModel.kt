package com.fasttimes.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasttimes.data.AppTheme
import com.fasttimes.data.DataManagementUseCase
import com.fasttimes.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed interface SettingsScreenEffect {
    data class ShowSnackbar(val message: String) : SettingsScreenEffect
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val dataManagementUseCase: DataManagementUseCase
) : ViewModel() {

    private val _effects = Channel<SettingsScreenEffect>()
    val effects = _effects.receiveAsFlow()

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.showLiveProgress,
        settingsRepository.showGoalReachedNotification,
        settingsRepository.theme,
        settingsRepository.firstDayOfWeek
    ) { showLiveProgress, showGoalReachedNotification, theme, firstDayOfWeek ->
        SettingsUiState(showLiveProgress, showGoalReachedNotification, theme, firstDayOfWeek)
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

    fun onShowGoalReachedNotificationChanged(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setShowGoalReachedNotification(enabled)
        }
    }

    fun onThemeChanged(theme: AppTheme) {
        viewModelScope.launch {
            settingsRepository.setTheme(theme)
        }
    }

    fun onFirstDayOfWeekChanged(day: String) {
        viewModelScope.launch {
            settingsRepository.setFirstDayOfWeek(day)
        }
    }

    fun onExportData(uri: Uri) {
        viewModelScope.launch {
            dataManagementUseCase.export(uri)
                .onSuccess { _effects.send(SettingsScreenEffect.ShowSnackbar("Export successful")) }
                .onFailure { _effects.send(SettingsScreenEffect.ShowSnackbar("Export failed")) }
        }
    }

    fun onImportData(uri: Uri) {
        viewModelScope.launch {
            dataManagementUseCase.import(uri)
                .onSuccess { _effects.send(SettingsScreenEffect.ShowSnackbar("Import successful")) }
                .onFailure { _effects.send(SettingsScreenEffect.ShowSnackbar("Import failed")) }
        }
    }
}
