
package com.fasttimes.ui.dashboard

import android.app.AlarmManager
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasttimes.alarms.AlarmScheduler
import com.fasttimes.data.FastingProfile
import com.fasttimes.data.fast.Fast
import com.fasttimes.data.fast.FastRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

// Placeholder for stats data class
data class DashboardStats(
    val totalFasts: Int = 0,
    val longestFast: Long = 0L, // Changed to Long to match duration calculation
    val totalFastingTime: Long = 0L // in hours
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val fastRepository: FastRepository,
    private val alarmScheduler: AlarmScheduler,
    private val alarmManager: AlarmManager
) : ViewModel() {

    // --- STATE ---

    /**
     * Exposes the list of selectable profiles (all except MANUAL).
     */
    val profiles: StateFlow<List<FastingProfile>> = flowOf(
        FastingProfile.entries.filter { it != FastingProfile.MANUAL }
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
            val totalTimeInMillis = fasts
                .filter { it.endTime != null } // only completed fasts
                .sumOf { it.endTime!! - it.startTime }

            DashboardStats(
                totalFasts = fasts.size,
                longestFast = fasts.maxOfOrNull { fast ->
                    (fast.endTime ?: System.currentTimeMillis()) - fast.startTime
                }?.let { it / (1000 * 60 * 60) } ?: 0L, // Convert to hours
                totalFastingTime = totalTimeInMillis / (1000 * 60 * 60) // Convert to hours
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardStats()
        )

    /**
     * The core UI state for the dashboard, representing the current fasting status.
     */
    val uiState: StateFlow<DashboardUiState> = history.flatMapLatest { fasts ->
        val activeFast = fasts.firstOrNull { it.endTime == null }

        if (activeFast == null) {
            flowOf<DashboardUiState>(DashboardUiState.NoFast)
        } else {
            // The main timer flow that updates every second
            flow {
                while (true) {
                    val now = System.currentTimeMillis()
                    val startTime = activeFast.startTime
                    val targetDuration = activeFast.targetDuration?.milliseconds ?: Duration.ZERO
                    val targetEndTime = startTime + targetDuration.inWholeMilliseconds

                    // Check if the goal has been reached
                    if (now >= targetEndTime) {
                        // For Manual fasts or fasts that have passed their goal
                        val elapsedTime = (now - startTime).milliseconds
                        emit(DashboardUiState.FastingGoalReached(activeFast, elapsedTime))
                    } else {
                        // For fasts still counting down
                        val remainingTime = (targetEndTime - now).milliseconds
                        val progress = 1f - (remainingTime.inWholeMilliseconds.toFloat() / targetDuration.inWholeMilliseconds)
                        emit(DashboardUiState.FastingInProgress(activeFast, remainingTime, progress))
                    }

                    delay(1000)
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState.NoFast)


    /**
     * State for the Profile Details Modal, holding the profile being viewed.
     */
    private val _modalProfile = MutableStateFlow<FastingProfile?>(null)
    val modalProfile: StateFlow<FastingProfile?> = _modalProfile.asStateFlow()

    /**
     * Signals the UI to show a rationale for the exact alarm permission.
     */
    private val _showAlarmPermissionRationale = MutableStateFlow(false)
    val showAlarmPermissionRationale: StateFlow<Boolean> = _showAlarmPermissionRationale.asStateFlow()


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
     * Dismisses the alarm permission rationale dialog.
     */
    fun dismissAlarmPermissionRationale() {
        _showAlarmPermissionRationale.value = false
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
     * Checks for exact alarm permissions before scheduling a notification.
     */
    fun startProfileFast(profile: FastingProfile) {
        viewModelScope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                _showAlarmPermissionRationale.value = true
                dismissProfileModal()
            } else {
                val durationMillis = profile.duration?.inWholeMilliseconds
                val fast = Fast(
                    startTime = System.currentTimeMillis(),
                    profile = profile,
                    targetDuration = durationMillis,
                    endTime = null,
                    notes = "Started ${profile.displayName} fast"
                )
                val fastId = fastRepository.insertFast(fast)
                alarmScheduler.schedule(fast.copy(id = fastId))
                dismissProfileModal() // Close modal after starting
            }
        }
    }

    /**
     * Ends the currently active fast.
     */
    fun endCurrentFast() {
        val fastToEnd = when (val state = uiState.value) {
            is DashboardUiState.FastingInProgress -> state.activeFast
            is DashboardUiState.FastingGoalReached -> state.activeFast
            else -> null
        }

        val fast = fastToEnd ?: return
        viewModelScope.launch {
            alarmScheduler.cancel(fast)
            fastRepository.endFast(fast.id, System.currentTimeMillis())
        }
    }
}
