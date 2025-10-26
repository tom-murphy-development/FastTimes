package com.fasttimes.ui.dashboard

import android.app.AlarmManager
import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasttimes.alarms.AlarmScheduler
import com.fasttimes.data.FastingProfile
import com.fasttimes.data.fast.Fast
import com.fasttimes.data.fast.FastRepository
import com.fasttimes.data.settings.SettingsRepository
import com.fasttimes.service.FastTimerService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
    val longestFast: Fast? = null,
    val totalFastingTime: Duration = Duration.ZERO,
    val averageFast: Duration = Duration.ZERO
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val fastRepository: FastRepository,
    private val settingsRepository: SettingsRepository,
    private val alarmScheduler: AlarmScheduler,
    private val alarmManager: AlarmManager,
    private val application: Application
) : ViewModel() {

    // --- STATE ---

    private val _confettiShownForFast = MutableStateFlow<Long?>(null)

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
            val completedFasts = fasts.filter { it.endTime != null }
            val totalFastingTime = completedFasts.sumOf { it.endTime!! - it.startTime }.milliseconds
            val completedFastsCount = completedFasts.size

            DashboardStats(
                totalFasts = fasts.size,
                longestFast = fasts.maxByOrNull { fast ->
                    (fast.endTime ?: System.currentTimeMillis()) - fast.startTime
                },
                totalFastingTime = totalFastingTime,
                averageFast = if (completedFastsCount > 0) {
                    totalFastingTime / completedFastsCount
                } else {
                    Duration.ZERO
                }
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

                    if (activeFast.profile == FastingProfile.MANUAL) {
                        val elapsedTime = (now - startTime).milliseconds
                        emit(DashboardUiState.ManualFasting(activeFast, elapsedTime))
                    } else {
                        val targetDuration = activeFast.targetDuration?.milliseconds ?: Duration.ZERO
                        val targetEndTime = startTime + targetDuration.inWholeMilliseconds

                        // Check if the goal has been reached
                        if (now >= targetEndTime) {
                            // For fasts that have passed their goal
                            val elapsedTime = (now - startTime).milliseconds
                            val showConfetti = _confettiShownForFast.value != activeFast.id
                            emit(DashboardUiState.FastingGoalReached(activeFast, elapsedTime, showConfetti))
                        } else {
                            // For fasts still counting down
                            val remainingTime = (targetEndTime - now).milliseconds
                            val progress = 1f - (remainingTime.inWholeMilliseconds.toFloat() / targetDuration.inWholeMilliseconds)
                            emit(DashboardUiState.FastingInProgress(activeFast, remainingTime, progress))
                        }
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

    fun onConfettiShown(fastId: Long) {
        _confettiShownForFast.value = fastId
    }

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
                targetDuration = 0L, // Manual has 0 duration goal
                endTime = null,
                notes = null
            )
            fastRepository.insertFast(fast)
            startService()
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
                    notes = "Started ${'$'}{profile.displayName} fast"
                )
                val fastId = fastRepository.insertFast(fast)
                alarmScheduler.schedule(fast.copy(id = fastId))
                startService()
                dismissProfileModal() // Close modal after starting
            }
        }
    }

    /**
     * Ends the currently active fast.
     */
    fun endCurrentFast() {
        val uiValue = uiState.value
        val fastToEnd = when (uiValue) {
            is DashboardUiState.FastingInProgress -> uiValue.activeFast
            is DashboardUiState.FastingGoalReached -> uiValue.activeFast
            is DashboardUiState.ManualFasting -> uiValue.activeFast
            else -> null
        }

        val fast = fastToEnd ?: return
        viewModelScope.launch {
            alarmScheduler.cancel(fast)

            val endTime = System.currentTimeMillis()
            if (fast.profile == FastingProfile.MANUAL) {
                val elapsedTime = endTime - fast.startTime
                fastRepository.updateFast(fast.copy(targetDuration = elapsedTime))
            }

            fastRepository.endFast(fast.id, endTime)
            stopService()
        }
    }

    private suspend fun startService() {
        if (settingsRepository.showLiveProgress.first()) {
            Intent(application, FastTimerService::class.java).also {
                it.action = FastTimerService.ACTION_START
                application.startService(it)
            }
        }
    }

    private fun stopService() {
        Intent(application, FastTimerService::class.java).also {
            it.action = FastTimerService.ACTION_STOP
            application.startService(it)
        }
    }
}
