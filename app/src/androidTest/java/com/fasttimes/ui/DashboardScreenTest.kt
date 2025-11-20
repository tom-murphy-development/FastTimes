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
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.milliseconds

@RunWith(AndroidJUnit4::class)
class DashboardScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockViewModel: DashboardViewModel

    @Before
    fun setUp() {
        mockViewModel = mockk(relaxed = true)
        every { mockViewModel.stats } returns (MutableStateFlow(DashboardStats()))
        every { mockViewModel.profiles } returns (MutableStateFlow(emptyList()))
        every { mockViewModel.favoriteProfile } returns (MutableStateFlow(null))
        every { mockViewModel.showAlarmPermissionRationale } returns (MutableStateFlow(false))
        every { mockViewModel.completedFast } returns (MutableStateFlow(null))
    }

    @Test
    fun fab_is_visible_when_no_fast_in_progress() {
        every { mockViewModel.uiState } returns (MutableStateFlow(DashboardUiState.NoFast(
            thisWeekFasts = emptyList(),
            lastWeekFasts = emptyList(),
            lastFast = null,
            showFab = true
        )))

        composeTestRule.setContent {
            FastTimesTheme(
                theme = AppTheme.LIGHT,
                seedColor = Color.Blue,
                accentColor = Color.Blue,
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
        every { mockViewModel.uiState } returns (
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
                accentColor = Color.Blue,
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
        every { mockViewModel.profiles } returns (MutableStateFlow(profiles))
        every { mockViewModel.uiState } returns (MutableStateFlow(DashboardUiState.NoFast(
            thisWeekFasts = emptyList(),
            lastWeekFasts = emptyList(),
            lastFast = null,
            showFab = true
        )))

        composeTestRule.setContent {
            FastTimesTheme(
                theme = AppTheme.LIGHT,
                seedColor = Color.Blue,
                accentColor = Color.Blue,
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
