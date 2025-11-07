package com.fasttimes.ui.dashboard

import com.fasttimes.data.fast.Fast
import kotlin.time.Duration

/**
 * Represents the different states the Dashboard screen can be in.
 */
sealed class DashboardUiState {
    /**
     * The state where the UI is still loading.
     */
    data object Loading : DashboardUiState()

    /**
     * The state where there is no active fast.
     *
     * @param thisWeekFasts Fasts completed this week.
     * @param lastWeekFasts Fasts completed last week.
     * @param lastFast The most recently completed fast, for fallback.
     * @param showFab Whether to show the Floating Action Button.
     */
    data class NoFast(
        val thisWeekFasts: List<Fast>,
        val lastWeekFasts: List<Fast>,
        val lastFast: Fast? = null,
        val showFab: Boolean = true
    ) : DashboardUiState()


    /**
     * The state where a fast is currently in progress and the goal has not been reached yet.
     *
     * @param activeFast The currently active fast.
     * @param remainingTime The time remaining until the fast goal is reached.
     * @param progress The progress of the fast, from 0.0f to 1.0f.
     * @param isEditing Whether the user is currently editing the fast.
     */
    data class FastingInProgress(
        val activeFast: Fast,
        val remainingTime: Duration,
        val progress: Float,
        val isEditing: Boolean = false,
        val useWavyIndicator: Boolean
    ) : DashboardUiState()

    /**
     * The state where the fasting goal has been reached and the timer is now counting up.
     *
     * @param activeFast The currently active fast.
     * @param totalElapsedTime The total time elapsed since the fast started.
     * @param showConfetti Whether to show the confetti animation.
     * @param isEditing Whether the user is currently editing the fast.
     */
    data class FastingGoalReached(
        val activeFast: Fast,
        val totalElapsedTime: Duration,
        val showConfetti: Boolean,
        val isEditing: Boolean = false
    ) : DashboardUiState()

    /**
     * The state where a manual fast is in progress.
     *
     * @param activeFast The currently active fast.
     * @param elapsedTime The time elapsed since the fast started.
     * @param isEditing Whether the user is currently editing the fast.
     */
    data class ManualFasting(
        val activeFast: Fast,
        val elapsedTime: Duration,
        val isEditing: Boolean = false
    ) : DashboardUiState()
}
