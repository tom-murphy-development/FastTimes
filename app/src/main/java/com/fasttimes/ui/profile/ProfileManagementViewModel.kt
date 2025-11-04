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

    fun addProfile(name: String, duration: Long?, description: String) {
        viewModelScope.launch {
            val profile = FastingProfile(
                displayName = name,
                duration = duration,
                description = description
            )
            fastingProfileRepository.addProfile(profile)
        }
    }

    fun updateProfile(profile: FastingProfile) {
        viewModelScope.launch {
            fastingProfileRepository.updateProfile(profile)
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
