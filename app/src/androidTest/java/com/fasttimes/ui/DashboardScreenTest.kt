package com.fasttimes.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fasttimes.data.AppTheme
import com.fasttimes.data.fast.Fast
import com.fasttimes.data.profile.FastingProfile
import com.fasttimes.ui.dashboard.DashboardStats
import com.fasttimes.ui.dashboard.DashboardUiState
import com.fasttimes.ui.dashboard.DashboardViewModel
import com.fasttimes.ui.theme.FastTimesTheme
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
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

    private lateinit var mockViewModel: DashboardViewModel

    @Before
    fun setUp() {
        mockViewModel = mock()
        whenever(mockViewModel.stats).thenReturn(MutableStateFlow(DashboardStats()))
        whenever(mockViewModel.profiles).thenReturn(MutableStateFlow(emptyList()))
        whenever(mockViewModel.favoriteProfile).thenReturn(MutableStateFlow(null))
        whenever(mockViewModel.showAlarmPermissionRationale).thenReturn(MutableStateFlow(false))
        whenever(mockViewModel.completedFast).thenReturn(MutableStateFlow(null))
    }

    @Test
    fun fab_is_visible_when_no_fast_in_progress() {
        whenever(mockViewModel.uiState).thenReturn(MutableStateFlow(DashboardUiState.NoFast(
            thisWeekFasts = emptyList(),
            lastWeekFasts = emptyList(),
            lastFast = null,
            showFab = true
        )))

        composeTestRule.setContent {
            FastTimesTheme(
                theme = AppTheme.LIGHT,
                seedColor = Color.Blue,
                brandColor = Color.Blue,
                useExpressiveTheme = false,
                useSystemColors = false
            ) {
                DashboardScreen(
                    onHistoryClick = {},
                    onViewFastDetails = {},
                    onManageProfilesClick = {},
                    viewModel = mockViewModel
                )
            }
        }
        composeTestRule.onNodeWithContentDescription("Add Fast").assertIsDisplayed()
    }

    @Test
    fun end_fast_button_is_visible_when_fast_in_progress() {
        val fast = Fast(
            id = 1,
            startTime = System.currentTimeMillis(),
            endTime = null,
            profileName = "16:8",
            targetDuration = 1000,
            notes = null
        )
        whenever(mockViewModel.uiState).thenReturn(
            MutableStateFlow(
                DashboardUiState.FastingInProgress(
                    activeFast = fast,
                    remainingTime = 0.milliseconds,
                    progress = 0f,
                    isEditing = false,
                    useWavyIndicator = false
                )
            )
        )

        composeTestRule.setContent {
            FastTimesTheme(
                theme = AppTheme.LIGHT,
                seedColor = Color.Blue,
                brandColor = Color.Blue,
                useExpressiveTheme = false,
                useSystemColors = false
            ) {
                DashboardScreen(
                    onHistoryClick = {},
                    onViewFastDetails = {},
                    onManageProfilesClick = {},
                    viewModel = mockViewModel
                )
            }
        }
        composeTestRule.onNodeWithText("End Fast").assertIsDisplayed()
    }

    @Test
    fun no_fast_state_shows_profiles() {
        val profiles = listOf(
            FastingProfile(1, "16:8", 16, "desc", true),
            FastingProfile(2, "18:6", 18, "desc2", false)
        )
        whenever(mockViewModel.profiles).thenReturn(MutableStateFlow(profiles))
        whenever(mockViewModel.uiState).thenReturn(MutableStateFlow(DashboardUiState.NoFast(
            thisWeekFasts = emptyList(),
            lastWeekFasts = emptyList(),
            lastFast = null,
            showFab = true
        )))

        composeTestRule.setContent {
            FastTimesTheme(
                theme = AppTheme.LIGHT,
                seedColor = Color.Blue,
                brandColor = Color.Blue,
                useExpressiveTheme = false,
                useSystemColors = false
            ) {
                DashboardScreen(
                    onHistoryClick = {},
                    onViewFastDetails = {},
                    onManageProfilesClick = {},
                    viewModel = mockViewModel
                )
            }
        }

        composeTestRule.onNodeWithText("16:8").assertIsDisplayed()
        composeTestRule.onNodeWithText("18:6").assertIsDisplayed()
    }
}
