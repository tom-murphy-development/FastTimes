package com.fasttimes.ui.theme

import androidx.compose.ui.graphics.Color
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
        initialValue = ThemeState()
    )

    suspend fun setAccentColor(color: Long) {
        userPreferencesRepository.setAccentColor(color)
    }
}

data class ThemeState(
    val theme: com.fasttimes.data.Theme = com.fasttimes.data.Theme.SYSTEM,
    val accentColor: Long = Color(0xFF3DDC84).value.toLong()
)
