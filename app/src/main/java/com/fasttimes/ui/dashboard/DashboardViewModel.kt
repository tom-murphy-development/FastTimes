/*
 * Copyright (C) 2025 tom-murphy-development
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.fasttimes.ui.dashboard

import android.Manifest
import android.app.AlarmManager
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasttimes.alarms.AlarmScheduler
import com.fasttimes.data.DefaultFastingProfile
import com.fasttimes.data.fast.Fast
import com.fasttimes.data.fast.FastsRepository
import com.fasttimes.data.profile.FastingProfile
import com.fasttimes.data.profile.FastingProfileRepository
import com.fasttimes.data.settings.SettingsRepository
import com.fasttimes.data.settings.UserData
import com.fasttimes.service.FastTimerService
import com.fasttimes.ui.statistics.FastingStreak
import com.fasttimes.ui.statistics.FastingTrend
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

data class DayProgress(
    val date: LocalDate,
    val isCompleted: Boolean,
    val isFuture: Boolean
)

// Placeholder for stats data class
data class DashboardStats(
    val totalFasts: Int = 0,
    val longestFast: Fast? = null,
    val totalFastingTime: Duration = Duration.ZERO,
    val averageFast: Duration = Duration.ZERO,
    val streak: FastingStreak = FastingStreak(),
    val trend: FastingTrend = FastingTrend(),
    val weeklyProgress: List<DayProgress> = emptyList(),
    val fastsThisMonth: Int = 0,
    val longestFastThisMonth: Fast? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val fastsRepository: FastsRepository,
    private val settingsRepository: SettingsRepository,
    fastingProfileRepository: FastingProfileRepository,
    private val alarmScheduler: AlarmScheduler,
    private val alarmManager: AlarmManager,
    private val application: Application
) : ViewModel() {

    // --- STATE ---

    private val _isEditing = MutableStateFlow(false)
    private val _completedFast = MutableStateFlow<Fast?>(null)
    val completedFast: StateFlow<Fast?> = _completedFast.asStateFlow()

    val profiles: StateFlow<List<FastingProfile>> = fastingProfileRepository.getProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteProfile: StateFlow<FastingProfile?> = profiles
        .map { it.firstOrNull { profile -> profile.isFavorite } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val stats: StateFlow<DashboardStats> = fastsRepository.getFasts()
        .map { fasts ->
            val completedFasts = fasts.filter { it.endTime != null }
            val totalFastingTime = completedFasts.sumOf { it.duration() }.milliseconds
            val completedFastsCount = completedFasts.size

            val currentMonth = YearMonth.now()
            val systemZone = ZoneId.systemDefault()
            val currentMonthStart = currentMonth.atDay(1).atStartOfDay(systemZone)
            val currentMonthEnd = currentMonth.atEndOfMonth().plusDays(1).atStartOfDay(systemZone)
            
            val thisMonthFasts = completedFasts.filter { fast ->
                fast.end?.isBefore(currentMonthEnd) == true &&
                        fast.end?.isAfter(currentMonthStart) == true
            }

            DashboardStats(
                totalFasts = fasts.size,
                longestFast = fasts.maxByOrNull { it.duration() },
                totalFastingTime = totalFastingTime,
                averageFast = if (completedFastsCount > 0) {
                    totalFastingTime / completedFastsCount
                } else {
                    Duration.ZERO
                },
                streak = calculateStreak(completedFasts),
                trend = calculateTrend(completedFasts),
                weeklyProgress = calculateWeeklyProgress(completedFasts),
                fastsThisMonth = thisMonthFasts.size,
                longestFastThisMonth = thisMonthFasts.maxByOrNull { it.duration() }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardStats()
        )

    private data class DashboardData(
        val fasts: List<Fast>,
        val profiles: List<FastingProfile>,
        val isEditing: Boolean,
        val confettiShownForFastId: Long?,
        val showFab: Boolean,
        val userData: UserData
    )

    private val dashboardData = combine(
        fastsRepository.getFasts(),
        profiles,
        _isEditing,
        settingsRepository.confettiShownForFastId,
        settingsRepository.showFab
    ) { fasts, profiles, isEditing, confettiShown, showFab ->
        // Create a temporary tuple for the first 5 results
        Triple(fasts, profiles, isEditing) to (confettiShown to showFab)
    }.combine(settingsRepository.userData) { fiveResults, userData ->
        val (triple, pair) = fiveResults
        val (fasts, profiles, isEditing) = triple
        val (confettiShown, showFab) = pair
        DashboardData(
            fasts = fasts,
            profiles = profiles,
            isEditing = isEditing,
            confettiShownForFastId = confettiShown,
            showFab = showFab,
            userData = userData
        )
    }

    private val ticker = flow {
        while (true) {
            emit(Unit)
            delay(1000)
        }
    }

    val uiState: StateFlow<DashboardUiState> = dashboardData.flatMapLatest { data ->
        val activeFast = data.fasts.firstOrNull { it.endTime == null }

        if (activeFast == null && data.profiles.isEmpty()) {
            return@flatMapLatest flowOf(DashboardUiState.Loading(showSkeleton = true))
        }

        val uiFlow: Flow<DashboardUiState> = if (activeFast == null) {
            // No active fast, create the NoFast state and emit it once.
            val completedFasts = data.fasts.filter { it.endTime != null }.take(10)
            val now = ZonedDateTime.now()
            val startOfWeek =
                now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toLocalDate()
                    .atStartOfDay(now.zone)
            val startOfLastWeek = startOfWeek.minusWeeks(1)
            val thisWeekFasts = completedFasts.filter { it.start.isAfter(startOfWeek) }
            val lastWeekFasts = completedFasts.filter {
                it.start.isAfter(startOfLastWeek) && it.start.isBefore(
                    startOfWeek
                )
            }

            flowOf(
                DashboardUiState.NoFast(
                    thisWeekFasts,
                    lastWeekFasts,
                    completedFasts.firstOrNull(),
                    data.showFab
                )
            )
        } else {
            // Active fast, combine with the ticker to update the UI.
            ticker.map {
                val now = System.currentTimeMillis()
                val startTime = activeFast.startTime

                if (activeFast.targetDuration == null) {
                    val elapsedTime = (now - startTime).milliseconds
                    DashboardUiState.OpenFasting(
                        activeFast,
                        elapsedTime,
                        data.isEditing,
                        data.userData.useWavyIndicator
                    )
                } else {
                    val targetDuration = activeFast.targetDuration.milliseconds
                    val targetEndTime = startTime + targetDuration.inWholeMilliseconds

                    if (now >= targetEndTime) {
                        val elapsedTime = (now - startTime).milliseconds
                        val showConfetti = data.confettiShownForFastId != activeFast.id
                        DashboardUiState.FastingGoalReached(
                            activeFast,
                            elapsedTime,
                            showConfetti,
                            data.isEditing,
                            data.userData.useWavyIndicator
                        )
                    } else {
                        val remainingTime = (targetEndTime - now).milliseconds
                        val progress =
                            1f - (remainingTime.inWholeMilliseconds.toFloat() / targetDuration.inWholeMilliseconds)
                        DashboardUiState.FastingInProgress(
                            activeFast,
                            remainingTime,
                            progress,
                            data.isEditing,
                            data.userData.useWavyIndicator
                        )
                    }
                }
            }
        }
        uiFlow
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState.Loading()
    )


    private val _modalProfile = MutableStateFlow<FastingProfile?>(null)
    val modalProfile: StateFlow<FastingProfile?> = _modalProfile.asStateFlow()

    private val _showAlarmPermissionRationale = MutableStateFlow(false)
    val showAlarmPermissionRationale: StateFlow<Boolean> =
        _showAlarmPermissionRationale.asStateFlow()

    val showGoalReachedNotification: StateFlow<Boolean> = settingsRepository.showGoalReachedNotification
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun onConfettiShown(fastId: Long) {
        viewModelScope.launch {
            settingsRepository.setConfettiShownForFastId(fastId)
        }
    }

    fun dismissProfileModal() {
        _modalProfile.value = null
    }

    fun dismissAlarmPermissionRationale() {
        _showAlarmPermissionRationale.value = false
    }

    fun onEditFast() {
        _isEditing.value = true
    }

    fun onEditFastDismissed() {
        _isEditing.value = false
    }

    fun startOpenFast() {
        viewModelScope.launch {
            val showLiveProgress = settingsRepository.showLiveProgress.first()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (showLiveProgress) {
                    val hasNotificationPermission = ContextCompat.checkSelfPermission(
                        application,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED

                    if (!hasNotificationPermission) {
                        _showAlarmPermissionRationale.value = true
                        return@launch
                    }
                }
            }

            val fast = Fast(
                startTime = System.currentTimeMillis(),
                profileName = DefaultFastingProfile.OPEN.displayName,
                targetDuration = null,
                endTime = null,
                notes = null
            )
            fastsRepository.insertFast(fast)
            startService()
        }
    }

    fun startProfileFast(profile: FastingProfile) {
        viewModelScope.launch {
            val showGoalReached = settingsRepository.showGoalReachedNotification.first()
            val showLiveProgress = settingsRepository.showLiveProgress.first()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (showGoalReached || showLiveProgress) {
                    val hasNotificationPermission = ContextCompat.checkSelfPermission(
                        application,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED

                    if (!hasNotificationPermission) {
                        _showAlarmPermissionRationale.value = true
                        return@launch
                    }
                }
            }

            if (showGoalReached && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                _showAlarmPermissionRationale.value = true
                return@launch
            }

            val fast = Fast(
                startTime = System.currentTimeMillis(),
                profileName = profile.displayName,
                targetDuration = profile.duration,
                endTime = null,
                notes = "Started ${profile.displayName} fast"
            )
            val fastId = fastsRepository.insertFast(fast)
            alarmScheduler.schedule(fast.copy(id = fastId))
            startService()
            dismissProfileModal()
        }
    }

    fun endCurrentFast() {
        val uiValue = uiState.value
        val fastToEnd = when (uiValue) {
            is DashboardUiState.FastingInProgress -> uiValue.activeFast
            is DashboardUiState.FastingGoalReached -> uiValue.activeFast
            is DashboardUiState.OpenFasting -> uiValue.activeFast
            else -> null
        }

        val fast = fastToEnd ?: return
        viewModelScope.launch {
            alarmScheduler.cancel(fast)

            val endTime = System.currentTimeMillis()
            if (fast.profileName == DefaultFastingProfile.OPEN.displayName) {
                val elapsedTime = endTime - fast.startTime
                fastsRepository.updateFast(fast.copy(targetDuration = elapsedTime))
            }

            fastsRepository.endFast(fast.id, endTime)
            _completedFast.value = fast.copy(endTime = endTime)
            stopService()
        }
    }

    fun onFastingSummaryDismissed() {
        _completedFast.value = null
    }

    fun saveFastRating(fastId: Long, rating: Int) {
        viewModelScope.launch {
            fastsRepository.updateRating(fastId, rating)
        }
    }

    private suspend fun startService() {
        if (settingsRepository.showLiveProgress.first()) {
            try {
                Intent(application, FastTimerService::class.java).also {
                    it.action = FastTimerService.ACTION_START
                    application.startService(it)
                }
            } catch (e: Exception) {
                // Ignore for tests or if service cannot be started
            }
        }
    }

    private fun stopService() {
        try {
            Intent(application, FastTimerService::class.java).also {
                it.action = FastTimerService.ACTION_STOP
                application.startService(it)
            }
        } catch (e: Exception) {
            // Ignore for tests or if service cannot be started
        }
    }

    private fun calculateStreak(completedFasts: List<Fast>): FastingStreak {
        if (completedFasts.isEmpty()) return FastingStreak()

        val fastDates = completedFasts
            .flatMap { fast ->
                val startDate = fast.start.toLocalDate()
                val endDate = fast.end?.toLocalDate() ?: startDate
                listOf(startDate, endDate)
            }
            .distinct()
            .sorted()

        if (fastDates.isEmpty()) return FastingStreak()

        val today = LocalDate.now()
        var currentDate = today
        var streakDays = 0

        while (true) {
            when {
                currentDate in fastDates -> {
                    streakDays++
                    currentDate = currentDate.minusDays(1)
                }
                currentDate == today && fastDates.isNotEmpty() -> {
                    currentDate = currentDate.minusDays(1)
                }
                else -> {
                    break
                }
            }
        }

        if (streakDays == 0 && fastDates.isNotEmpty()) {
            for (date in fastDates.reversed()) {
                currentDate = date
                streakDays = 1

                var checkDate = date.minusDays(1)
                while (checkDate in fastDates) {
                    streakDays++
                    checkDate = checkDate.minusDays(1)
                }
                break
            }
        }

        val lastFastDate = fastDates.lastOrNull()
        val streakStartDate = if (streakDays > 0) {
            lastFastDate?.minusDays((streakDays - 1).toLong())
        } else {
            null
        }

        return FastingStreak(
            daysInARow = streakDays,
            startDate = streakStartDate,
            lastFastDate = lastFastDate
        )
    }

    private fun calculateTrend(completedFasts: List<Fast>): FastingTrend {
        val currentMonth = YearMonth.now()
        val previousMonth = currentMonth.minusMonths(1)

        val systemZone = ZoneId.systemDefault()
        val currentMonthStart = currentMonth.atDay(1)
            .atStartOfDay(systemZone)
        val currentMonthEnd = currentMonth.atEndOfMonth()
            .plusDays(1)
            .atStartOfDay(systemZone)

        val previousMonthStart = previousMonth.atDay(1)
            .atStartOfDay(systemZone)
        val previousMonthEnd = previousMonth.atEndOfMonth()
            .plusDays(1)
            .atStartOfDay(systemZone)

        val currentMonthFasts = completedFasts.count { fast ->
            fast.end?.isBefore(currentMonthEnd) == true &&
                    fast.end?.isAfter(currentMonthStart) == true
        }

        val previousMonthFasts = completedFasts.count { fast ->
            fast.end?.isBefore(previousMonthEnd) == true &&
                    fast.end?.isAfter(previousMonthStart) == true
        }

        val percentageChange = when {
            previousMonthFasts > 0 -> {
                ((currentMonthFasts - previousMonthFasts).toFloat() / previousMonthFasts) * 100
            }
            currentMonthFasts > 0 -> 100f
            else -> 0f
        }

        val isUpward = currentMonthFasts >= previousMonthFasts

        return FastingTrend(
            currentCount = currentMonthFasts,
            previousCount = previousMonthFasts,
            percentageChange = percentageChange,
            isUpward = isUpward
        )
    }

    private fun calculateWeeklyProgress(completedFasts: List<Fast>): List<DayProgress> {
        val today = LocalDate.now()
        // Rolling 7 days ending today
        val startDate = today.minusDays(6)
        val fastDates = completedFasts.flatMap { fast ->
            val start = fast.start.toLocalDate()
            val end = fast.end?.toLocalDate() ?: start
            listOf(start, end)
        }.toSet()

        return (0..6).map { i ->
            val date = startDate.plusDays(i.toLong())
            DayProgress(
                date = date,
                isCompleted = date in fastDates,
                isFuture = date.isAfter(today)
            )
        }
    }
}
