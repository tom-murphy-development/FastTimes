package com.fasttimes.ui.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * Represents the available theme options in the app.
 */
enum class Theme {
    LIGHT,
    DARK
}

/**
 * UI state for the Settings screen.
 *
 * @param selectedTheme The currently selected theme option.
 */
data class SettingsUiState(
    val selectedTheme: Theme = Theme.LIGHT
)

/**
 * ViewModel for the Settings screen.
 *
 * This class holds the business logic for user-configurable settings, such as theme preferences.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    /**
     * Handles the theme change event from the UI.
     *
     * @param theme The new theme selected by the user.
     */
    fun onThemeChange(theme: Theme) {
        _uiState.update { currentState ->
            currentState.copy(selectedTheme = theme)
        }
    }
}
