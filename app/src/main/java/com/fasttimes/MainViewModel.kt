package com.fasttimes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasttimes.data.AppTheme
import com.fasttimes.data.settings.SettingsRepository
import com.fasttimes.data.settings.UserData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Duration
import javax.inject.Inject

/**
 * UI State for the main activity.
 *
 * @param userData The user's settings data.
 * @param isLoading Whether the initial data is still loading.
 */
data class MainUiState(
    val userData: UserData,
    val isLoading: Boolean
)

@HiltViewModel
class MainViewModel @Inject constructor(
    settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<MainUiState> = settingsRepository.userData.map {
        MainUiState(userData = it, isLoading = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState(
            userData = UserData(
                fastingGoal = Duration.ofSeconds(57600),
                theme = AppTheme.SYSTEM,
                seedColor = null,
                brandColor = null,
                useWavyIndicator = true,
                useExpressiveTheme = false,
                useSystemColors = false
            ), isLoading = true
        )
    )
}
