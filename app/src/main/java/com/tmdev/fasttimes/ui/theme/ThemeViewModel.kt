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
package com.tmdev.fasttimes.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tmdev.fasttimes.data.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(private val userPreferencesRepository: UserPreferencesRepository) :
    ViewModel() {

    val themeState: StateFlow<ThemeState> = userPreferencesRepository.userData.map {
        ThemeState(it.theme, it.accentColor)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ThemeState.DEFAULT
    )

    suspend fun setAccentColor(color: Long) {
        userPreferencesRepository.setAccentColor(color)
    }

    suspend fun clearAccentColor() {
        userPreferencesRepository.clearAccentColor()
    }
}

data class ThemeState(
    val theme: com.tmdev.fasttimes.data.Theme,
    val accentColor: Long?
) {
    companion object {
        val DEFAULT = ThemeState(
            theme = com.tmdev.fasttimes.data.Theme.SYSTEM,
            accentColor = null
        )
    }
}
