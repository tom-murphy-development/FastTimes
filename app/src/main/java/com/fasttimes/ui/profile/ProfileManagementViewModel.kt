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
package com.fasttimes.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasttimes.data.profile.FastingProfile
import com.fasttimes.data.profile.FastingProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileManagementViewModel @Inject constructor(
    private val fastingProfileRepository: FastingProfileRepository
) : ViewModel() {

    val profiles: StateFlow<List<FastingProfile>> = fastingProfileRepository.getProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addProfile(name: String, duration: Long?, description: String, isFavorite: Boolean) {
        viewModelScope.launch {
            val profile = FastingProfile(
                displayName = name,
                duration = duration,
                description = description,
                isFavorite = isFavorite
            )
            val newId = fastingProfileRepository.addProfile(profile)
            if (isFavorite) {
                fastingProfileRepository.setFavoriteProfile(profile.copy(id = newId))
            }
        }
    }

    fun updateProfile(profile: FastingProfile) {
        viewModelScope.launch {
            if (profile.isFavorite) {
                fastingProfileRepository.setFavoriteProfile(profile)
            } else {
                fastingProfileRepository.updateProfile(profile)
            }
        }
    }

    fun deleteProfile(profile: FastingProfile) {
        viewModelScope.launch {
            fastingProfileRepository.deleteProfile(profile)
        }
    }

    fun setFavoriteProfile(profile: FastingProfile) {
        viewModelScope.launch {
            fastingProfileRepository.setFavoriteProfile(profile)
        }
    }
}
