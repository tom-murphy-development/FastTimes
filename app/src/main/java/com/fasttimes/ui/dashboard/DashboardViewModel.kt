/**
 * ViewModel for the Dashboard screen.
 *
 * This ViewModel is responsible for providing data to the Dashboard screen and handling user
 * interactions. It uses a [FastRepository] to interact with the data layer.
 *
 * @property fastRepository The repository for accessing fast data.
 */
package com.fasttimes.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasttimes.data.fast.Fast
import com.fasttimes.data.fast.FastRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// Placeholder for stats data class
data class DashboardStats(
    val totalFasts: Int = 0,
    val longestFast: Long = 0L // Changed to Long to match duration calculation
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val fastRepository: FastRepository
) : ViewModel() {

    /**
     * A flow of the user's entire fasting history, sorted from newest to oldest.
     */
    val history: StateFlow<List<Fast>> = fastRepository.getAllFasts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * The currently active fast, or `null` if no fast is in progress.
     */
    val currentFast: StateFlow<Fast?> = history
        .map { it.firstOrNull { fast -> fast.endTime == null } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    /**
     * A flow that emits the elapsed time of the current fast every second.
     * Emits 0 if no fast is in progress.
     */
    val elapsedTime: StateFlow<Long> = currentFast.flatMapLatest { fast ->
        if (fast == null) {
            flowOf(0L)
        } else {
            flow {
                while (true) {
                    emit(System.currentTimeMillis() - fast.startTime)
                    delay(1000)
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0L
    )

    /**
     * A flow of calculated statistics based on the user's fasting history.
     */
    val stats: StateFlow<DashboardStats> = history
        .map { fasts ->
            DashboardStats(
                totalFasts = fasts.size,
                longestFast = fasts.maxOfOrNull { fast ->
                    (fast.endTime ?: System.currentTimeMillis()) - fast.startTime
                }?.let { it / (1000 * 60 * 60) } ?: 0L // Convert to hours
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardStats()
        )

    /**
     * Starts a new fast.
     *
     * @param targetDuration The target duration of the fast in milliseconds.
     * @param notes Optional notes for the fast.
     */
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

    /**
     * Ends the currently active fast.
     */
    fun endCurrentFast() {
        val fast = currentFast.value ?: return
        viewModelScope.launch {
            fastRepository.endFast(fast.id, System.currentTimeMillis())
        }
    }
}
