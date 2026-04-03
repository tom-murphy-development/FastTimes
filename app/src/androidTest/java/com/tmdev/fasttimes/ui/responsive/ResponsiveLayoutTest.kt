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
 * along with this program.  See the LICENSE file for more details.
 */
package com.tmdev.fasttimes.ui.responsive

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tmdev.fasttimes.data.AppTheme
import com.tmdev.fasttimes.data.fast.Fast
import com.tmdev.fasttimes.ui.DashboardScreen
import com.tmdev.fasttimes.ui.dashboard.DashboardStats
import com.tmdev.fasttimes.ui.dashboard.DashboardUiState
import com.tmdev.fasttimes.ui.dashboard.DashboardViewModel
import com.tmdev.fasttimes.ui.editfast.EditFastScreen
import com.tmdev.fasttimes.ui.editfast.EditFastUiState
import com.tmdev.fasttimes.ui.statistics.StatisticsScreen
import com.tmdev.fasttimes.ui.statistics.StatisticsUiState
import com.tmdev.fasttimes.ui.statistics.StatisticsViewModel
import com.tmdev.fasttimes.ui.theme.FastTimesTheme
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResponsiveLayoutTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockDashboardViewModel: DashboardViewModel
    private lateinit var mockStatisticsViewModel: StatisticsViewModel

    @Before
    fun setUp() {
        mockDashboardViewModel = mockk(relaxed = true)
        every { mockDashboardViewModel.stats } returns (MutableStateFlow(DashboardStats()))
        every { mockDashboardViewModel.profiles } returns (MutableStateFlow(emptyList()))
        every { mockDashboardViewModel.favoriteProfile } returns (MutableStateFlow(null))
        every { mockDashboardViewModel.showAlarmPermissionRationale } returns (MutableStateFlow(false))
        every { mockDashboardViewModel.completedFast } returns (MutableStateFlow(null))
        
        // Default state: No fast in progress
        every { mockDashboardViewModel.uiState } returns (MutableStateFlow(DashboardUiState.NoFast(
            thisWeekFasts = emptyList(),
            lastWeekFasts = emptyList(),
            lastFast = null,
            showFab = true
        )))

        mockStatisticsViewModel = mockk(relaxed = true)
        every { mockStatisticsViewModel.statisticsState } returns (MutableStateFlow(StatisticsUiState()))
    }

    @Test
    fun dashboard_elements_visible_on_compact() {
        renderDashboard(WindowWidthSizeClass.Compact)
        
        // Assert key sections are visible
        composeTestRule.onNodeWithText("History").assertIsDisplayed()
        composeTestRule.onNodeWithText("Performance").assertIsDisplayed()
    }

    @Test
    fun dashboard_elements_visible_on_expanded() {
        renderDashboard(WindowWidthSizeClass.Expanded)
        
        // Assert key sections are visible
        composeTestRule.onNodeWithText("History").assertIsDisplayed()
        composeTestRule.onNodeWithText("Performance").assertIsDisplayed()
    }

    @Test
    fun statistics_elements_visible_on_compact() {
        renderStatistics(WindowWidthSizeClass.Compact)

        composeTestRule.onNodeWithText("Performance").assertIsDisplayed()
        composeTestRule.onNodeWithText("Trends").assertIsDisplayed()
    }

    @Test
    fun statistics_elements_visible_on_expanded() {
        renderStatistics(WindowWidthSizeClass.Expanded)

        composeTestRule.onNodeWithText("Performance").assertIsDisplayed()
        composeTestRule.onNodeWithText("Trends").assertIsDisplayed()
    }

    @Test
    fun edit_fast_elements_visible_on_compact() {
        renderEditFast(WindowWidthSizeClass.Compact)

        composeTestRule.onNodeWithText("Edit Fast").assertIsDisplayed()
        composeTestRule.onNodeWithText("Start Time").assertIsDisplayed()
        composeTestRule.onNodeWithText("End Time").assertIsDisplayed()
    }

    @Test
    fun edit_fast_elements_visible_on_expanded() {
        renderEditFast(WindowWidthSizeClass.Expanded)

        composeTestRule.onNodeWithText("Edit Fast").assertIsDisplayed()
        composeTestRule.onNodeWithText("Start Time").assertIsDisplayed()
        composeTestRule.onNodeWithText("End Time").assertIsDisplayed()
    }

    private fun renderDashboard(widthClass: WindowWidthSizeClass) {
        composeTestRule.setContent {
            FastTimesTheme(
                theme = AppTheme.LIGHT,
                seedColor = Color.Blue,
                accentColor = Color.Blue,
                useExpressiveTheme = false,
                useSystemColors = false,
                windowWidthSizeClass = widthClass
            ) {
                DashboardScreen(
                    onHistoryClick = {},
                    onStatisticsClick = {},
                    onViewFastDetails = {},
                    onManageProfilesClick = {},
                    viewModel = mockDashboardViewModel
                )
            }
        }
    }

    private fun renderStatistics(widthClass: WindowWidthSizeClass) {
        composeTestRule.setContent {
            FastTimesTheme(
                theme = AppTheme.LIGHT,
                seedColor = Color.Blue,
                accentColor = Color.Blue,
                useExpressiveTheme = false,
                useSystemColors = false,
                windowWidthSizeClass = widthClass
            ) {
                StatisticsScreen(
                    onBackClick = {},
                    onHistoryClick = {},
                    viewModel = mockStatisticsViewModel
                )
            }
        }
    }

    private fun renderEditFast(widthClass: WindowWidthSizeClass) {
        val testFast = Fast(
            id = 1L,
            startTime = System.currentTimeMillis() - 3600000,
            endTime = System.currentTimeMillis(),
            targetDuration = 3600000,
            profileName = "Test Profile",
            rating = 4
        )
        val uiState = EditFastUiState(
            isLoading = false,
            fast = testFast
        )
        composeTestRule.setContent {
            FastTimesTheme(
                theme = AppTheme.LIGHT,
                seedColor = Color.Blue,
                accentColor = Color.Blue,
                useExpressiveTheme = false,
                useSystemColors = false,
                windowWidthSizeClass = widthClass
            ) {
                EditFastScreen(
                    uiState = uiState,
                    onStartTimeChanged = {},
                    onEndTimeChanged = {},
                    onRatingChanged = {},
                    onSave = {},
                    onCancel = {},
                    onErrorDismissed = {},
                    onDelete = {}
                )
            }
        }
    }
}
