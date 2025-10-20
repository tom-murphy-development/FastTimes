package com.fasttimes.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fasttimes.data.FastingProfile
import com.fasttimes.data.fast.Fast
import com.fasttimes.ui.dashboard.DashboardStats
import com.fasttimes.ui.dashboard.DashboardUiState
import com.fasttimes.ui.dashboard.DashboardViewModel
import com.fasttimes.ui.theme.FastTimesTheme
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.time.Duration.Companion.milliseconds

@RunWith(AndroidJUnit4::class)
class DashboardScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun fab_is_visible_when_no_fast_in_progress() {
        val mockViewModel = mock<DashboardViewModel>()
        whenever(mockViewModel.uiState).thenReturn(MutableStateFlow(DashboardUiState.NoFast))
        whenever(mockViewModel.stats).thenReturn(MutableStateFlow(DashboardStats()))
        whenever(mockViewModel.history).thenReturn(MutableStateFlow(emptyList()))
        whenever(mockViewModel.profiles).thenReturn(MutableStateFlow(emptyList()))
        whenever(mockViewModel.modalProfile).thenReturn(MutableStateFlow(null))
        whenever(mockViewModel.showAlarmPermissionRationale).thenReturn(MutableStateFlow(false))

        composeTestRule.setContent {
            FastTimesTheme {
                DashboardScreen(
                    viewModel = mockViewModel,
                    onSettingsClick = {},
                    onHistoryClick = {}
                )
            }
        }
        composeTestRule.onNodeWithContentDescription("Start Manual Fast").assertIsDisplayed()
    }

    @Test
    fun end_fast_button_is_visible_when_fast_in_progress() {
        val fast = Fast(
            id = 1,
            startTime = System.currentTimeMillis(),
            endTime = null,
            profile = FastingProfile.MANUAL,
            targetDuration = 1000,
            notes = null
        )
        val mockViewModel = mock<DashboardViewModel>()
        whenever(mockViewModel.uiState).thenReturn(MutableStateFlow(DashboardUiState.FastingGoalReached(fast, 0L.milliseconds)))
        whenever(mockViewModel.stats).thenReturn(MutableStateFlow(DashboardStats()))
        whenever(mockViewModel.history).thenReturn(MutableStateFlow(listOf(fast)))
        whenever(mockViewModel.profiles).thenReturn(MutableStateFlow(emptyList()))
        whenever(mockViewModel.modalProfile).thenReturn(MutableStateFlow(null))
        whenever(mockViewModel.showAlarmPermissionRationale).thenReturn(MutableStateFlow(false))

        composeTestRule.setContent {
            FastTimesTheme {
                DashboardScreen(
                    viewModel = mockViewModel,
                    onSettingsClick = {},
                    onHistoryClick = {}
                )
            }
        }
        composeTestRule.onNodeWithText("End Fast").assertIsDisplayed()
    }
}
