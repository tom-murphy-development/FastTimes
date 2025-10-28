package com.fasttimes.ui.editfast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasttimes.data.fast.Fast
import com.fasttimes.data.fast.FastsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditFastUiState(
    val isLoading: Boolean = true,
    val fast: Fast? = null,
    val error: String? = null
)

@HiltViewModel(assistedFactory = EditFastViewModel.Factory::class)
class EditFastViewModel @AssistedInject constructor(
    private val fastsRepository: FastsRepository,
    @Assisted private val fastId: Long?
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(fastId: Long?): EditFastViewModel
    }

    private val _uiState = MutableStateFlow(EditFastUiState())
    val uiState: StateFlow<EditFastUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val fastFlow = if (fastId != null && fastId != -1L) {
                fastsRepository.getFast(fastId)
            } else {
                fastsRepository.getActiveFast()
            }

            val fast = fastFlow.first()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    fast = fast
                )
            }
        }
    }

    fun updateStartTime(newStartTime: Long) {
        _uiState.update {
            it.copy(fast = it.fast?.copy(startTime = newStartTime))
        }
    }

    fun updateEndTime(newEndTime: Long) {
        _uiState.update {
            it.copy(fast = it.fast?.copy(endTime = newEndTime))
        }
    }

    fun updateRating(newRating: Int) {
        _uiState.update {
            it.copy(fast = it.fast?.copy(rating = newRating))
        }
    }

    fun saveChanges(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val fastToSave = _uiState.value.fast
            if (fastToSave != null) {
                val validationError = validateFast(fastToSave)
                if (validationError == null) {
                    fastsRepository.updateFast(fastToSave)
                    onSuccess()
                } else {
                    _uiState.update {
                        it.copy(error = validationError)
                    }
                }
            }
        }
    }

    fun deleteFast(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val fastToDelete = _uiState.value.fast
            if (fastToDelete != null) {
                fastsRepository.deleteFast(fastToDelete.id)
                onSuccess()
            }
        }
    }

    private fun validateFast(fast: Fast): String? {
        if (fast.startTime > System.currentTimeMillis()) {
            return "Start time cannot be in the future."
        }
        val endTime = fast.endTime
        if (endTime != null) {
            if (endTime <= fast.startTime) {
                return "End time must be after start time."
            }
            if (endTime > System.currentTimeMillis()) {
                return "End time cannot be in the future."
            }
        }
        return null
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
