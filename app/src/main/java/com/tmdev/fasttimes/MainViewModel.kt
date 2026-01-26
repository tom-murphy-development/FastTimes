/*
 * Copyright (C) 2025 tom-murphy-development
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.tmdev.fasttimes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tmdev.fasttimes.data.AppTheme
import com.tmdev.fasttimes.data.settings.SettingsRepository
import com.tmdev.fasttimes.data.settings.UserData
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
                accentColor = null,
                useWavyIndicator = true,
                useExpressiveTheme = false,
                useSystemColors = false
            ), isLoading = true
        )
    )
}
