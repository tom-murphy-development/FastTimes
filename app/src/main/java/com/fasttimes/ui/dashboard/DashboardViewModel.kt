package com.fasttimes.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasttimes.data.fast.Fast
import com.fasttimes.data.fast.FastRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Placeholder for stats data class
data class DashboardStats(
    val totalFasts: Int = 0,
    val longestFast: Long = 0L // Changed to Long to match duration calculation
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val fastRepository: FastRepository
) : ViewModel() {
    private val _currentFast = MutableStateFlow<Fast?>(null)
    val currentFast: StateFlow<Fast?> = _currentFast.asStateFlow()

    private val _history = MutableStateFlow<List<Fast>>(emptyList())
    val history: StateFlow<List<Fast>> = _history.asStateFlow()

    private val _stats = MutableStateFlow(DashboardStats())
    val stats: StateFlow<DashboardStats> = _stats.asStateFlow()

    init {
        viewModelScope.launch {
            fastRepository.getAllFasts().collect { fasts ->
                _history.value = fasts
                _currentFast.value = fasts.firstOrNull { it.endTime == null }
                _stats.value = DashboardStats(
                    totalFasts = fasts.size,
                    longestFast = fasts.maxOfOrNull { fast ->
                        (fast.endTime ?: System.currentTimeMillis()) - fast.startTime
                    }?.let { it / (1000 * 60 * 60) } ?: 0L // Convert to hours
                )
            }
        }
    }

    fun startFast(targetDuration: Long = 16 * 60 * 60 * 1000L, notes: String? = null) {
        viewModelScope.launch {
            val fast = Fast(
                startTime = System.currentTimeMillis(),
                endTime = null,
                targetDuration = targetDuration,
                notes = notes
            )
            fastRepository.insertFast(fast)
        }
    }

    fun endCurrentFast() {
        val fast = _currentFast.value ?: return
        viewModelScope.launch {
            fastRepository.endFast(fast.id, System.currentTimeMillis())
        }
    }
}
