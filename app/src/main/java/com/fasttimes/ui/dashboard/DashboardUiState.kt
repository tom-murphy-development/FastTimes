package com.fasttimes.ui.dashboard

import com.fasttimes.data.fast.Fast
import kotlin.time.Duration

/**
 * Represents the different states the Dashboard screen can be in.
 */
sealed class DashboardUiState {
    /**
     * The state where there is no active fast.
     */
    data object NoFast : DashboardUiState()

    /**
     * The state where a fast is currently in progress and the goal has not been reached yet.
     *
     * @param activeFast The currently active fast.
     * @param remainingTime The time remaining until the fast goal is reached.
     * @param progress The progress of the fast, from 0.0f to 1.0f.
     */
    data class FastingInProgress(
        val activeFast: Fast,
        val remainingTime: Duration,
        val progress: Float
    ) : DashboardUiState()

    /**
     * The state where the fasting goal has been reached and the timer is now counting up.
     *
     * @param activeFast The currently active fast.
     * @param totalElapsedTime The total time elapsed since the fast started.
     */
    data class FastingGoalReached(
        val activeFast: Fast,
        val totalElapsedTime: Duration
    ) : DashboardUiState()

    /**
     * The state where a manual fast is in progress.
     *
     * @param activeFast The currently active fast.
     * @param elapsedTime The time elapsed since the fast started.
     */
    data class ManualFasting(
        val activeFast: Fast,
        val elapsedTime: Duration
    ) : DashboardUiState()
}
