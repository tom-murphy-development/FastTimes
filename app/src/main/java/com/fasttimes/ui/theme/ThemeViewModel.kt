package com.fasttimes.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasttimes.data.UserPreferencesRepository
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
    val theme: com.fasttimes.data.Theme,
    val accentColor: Long?
) {
    companion object {
        val DEFAULT = ThemeState(
            theme = com.fasttimes.data.Theme.SYSTEM,
            accentColor = null
        )
    }
}
