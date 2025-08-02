package com.fasttimes.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fasttimes.ui.dashboard.DashboardViewModel
import com.fasttimes.ui.theme.FastTimesTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlinx.coroutines.flow.MutableStateFlow
import com.fasttimes.data.fast.Fast
import com.fasttimes.ui.dashboard.DashboardStats

@RunWith(AndroidJUnit4::class)
class DashboardScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun fab_is_visible_when_no_fast_in_progress() {
        val mockViewModel = mock<DashboardViewModel>()
        whenever(mockViewModel.currentFast).thenReturn(MutableStateFlow(null))
        whenever(mockViewModel.stats).thenReturn(MutableStateFlow(DashboardStats()))
        whenever(mockViewModel.history).thenReturn(MutableStateFlow(emptyList()))

        composeTestRule.setContent {
            FastTimesTheme {
                DashboardScreen(viewModel = mockViewModel)
            }
        }
        composeTestRule.onNodeWithContentDescription("Start Fast").assertIsDisplayed()
    }

    @Test
    fun end_fast_button_is_visible_when_fast_in_progress() {
        val fast = Fast(1, System.currentTimeMillis(), null, 1000, null)
        val mockViewModel = mock<DashboardViewModel>()
        whenever(mockViewModel.currentFast).thenReturn(MutableStateFlow(fast))
        whenever(mockViewModel.stats).thenReturn(MutableStateFlow(DashboardStats()))
        whenever(mockViewModel.history).thenReturn(MutableStateFlow(listOf(fast)))

        composeTestRule.setContent {
            FastTimesTheme {
                DashboardScreen(viewModel = mockViewModel)
            }
        }
        composeTestRule.onNodeWithText("End Fast").assertIsDisplayed()
    }
}

