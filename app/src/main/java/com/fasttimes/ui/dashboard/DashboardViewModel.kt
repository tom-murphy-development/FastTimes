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
import com.fasttimes.data.FastingProfile
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

    // --- STATE ---

    /**
     * Exposes the list of selectable profiles (all except MANUAL).
     */
    val profiles: StateFlow<List<FastingProfile>> = flowOf(
        FastingProfile.values().filter { it != FastingProfile.MANUAL }
    ).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

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
     * State for the Profile Details Modal, holding the profile being viewed.
     */
    private val _modalProfile = MutableStateFlow<FastingProfile?>(null)
    val modalProfile: StateFlow<FastingProfile?> = _modalProfile.asStateFlow()

    /**
     * Elapsed time for Manual (count up) fasts. Emits 0 otherwise.
     */
    val elapsedTime: StateFlow<Long> = currentFast.flatMapLatest { fast ->
        if (fast?.profile == FastingProfile.MANUAL) {
            flow {
                while (true) {
                    emit(System.currentTimeMillis() - fast.startTime)
                    delay(1000)
                }
            }
        } else {
            flowOf(0L)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    /**
     * Remaining time for Profile (count down) fasts. Emits 0 otherwise.
     */
    val remainingTime: StateFlow<Long> = currentFast.flatMapLatest { fast ->
        if (fast != null && fast.profile != FastingProfile.MANUAL && fast.targetDuration != null) {
            val targetEndTime = fast.startTime + fast.targetDuration!!
            flow {
                while (true) {
                    val remaining = targetEndTime - System.currentTimeMillis()
                    emit(if (remaining > 0) remaining else 0)
                    if (remaining <= 0) break
                    delay(1000)
                }
            }
        } else {
            flowOf(0L)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)


    // --- ACTIONS ---

    /**
     * Shows the profile details modal for the given profile.
     */
    fun showProfileModal(profile: FastingProfile) {
        _modalProfile.value = profile
    }

    /**
     * Dismisses the profile details modal.
     */
    fun dismissProfileModal() {
        _modalProfile.value = null
    }

    /**
     * Starts a new fast in Manual (count-up) mode.
     */
    fun startManualFast() {
        viewModelScope.launch {
            val fast = Fast(
                startTime = System.currentTimeMillis(),
                profile = FastingProfile.MANUAL,
                targetDuration = null, // Manual has no target
                endTime = null,
                notes = null
            )
            fastRepository.insertFast(fast)
        }
    }

    /**
     * Starts a new fast in Profile (count-down) mode.
     */
    fun startProfileFast(profile: FastingProfile) {
        viewModelScope.launch {
            val durationMillis = profile.durationHours?.let { it * 60 * 60 * 1000L }
            val fast = Fast(
                startTime = System.currentTimeMillis(),
                profile = profile,
                targetDuration = durationMillis,
                endTime = null,
                notes = "Started ${profile.displayName} fast"
            )
            fastRepository.insertFast(fast)
            dismissProfileModal() // Close modal after starting
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
