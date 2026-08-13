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
package com.tmdev.fasttimes.ui.dashboard

import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tmdev.fasttimes.data.fast.FastingPhase
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FastingPhaseComponentsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun fastingPhasesCanvas_clickOnTopSegment_callsOnPhaseClickWithFirstPhase() {
        var clickedPhase: FastingPhase? = null
        val phases = listOf(FastingPhase.POST_ABSORPTIVE, FastingPhase.METABOLIC_SWITCH)
        
        composeTestRule.setContent {
            FastingPhasesCanvas(
                relevantPhases = phases,
                goalHours = 16.0,
                modifier = Modifier.size(300.dp),
                onPhaseClick = { clickedPhase = it }
            )
        }

        // Compose touch coordinates are pixels, so derive the ring edge from the measured node.
        composeTestRule.onNodeWithContentDescription("View fasting phase details").performTouchInput {
            click(androidx.compose.ui.geometry.Offset(center.x, 1f))
        }

        assertEquals(FastingPhase.POST_ABSORPTIVE, clickedPhase)
    }

    @Test
    fun fastingPhasesCanvas_clickOnMarker_callsOnPhaseClickWithCorrespondingPhase() {
        var clickedPhase: FastingPhase? = null
        // METABOLIC_SWITCH starts at 12h. For a 16h goal, this is at 3/4 of the circle (270 degrees from top).
        // 270 degrees from top (starting at -90) is 180 degrees (Left side).
        val phases = listOf(FastingPhase.POST_ABSORPTIVE, FastingPhase.METABOLIC_SWITCH)
        
        composeTestRule.setContent {
            FastingPhasesCanvas(
                relevantPhases = phases,
                goalHours = 16.0,
                modifier = Modifier.size(300.dp),
                onPhaseClick = { clickedPhase = it }
            )
        }

        // Click at the 9 o'clock position (left), independent of screen density.
        composeTestRule.onNodeWithContentDescription("View fasting phase details").performTouchInput {
            click(androidx.compose.ui.geometry.Offset(1f, center.y))
        }

        assertEquals(FastingPhase.METABOLIC_SWITCH, clickedPhase)
    }

    @Test
    fun fastingPhasesCanvas_showSegmentsFalse_clickAnywhereCallsOnPhaseClickWithCurrentPhase() {
        var clickedPhase: FastingPhase? = null
        val currentPhase = FastingPhase.EARLY_AUTOPHAGY
        
        composeTestRule.setContent {
            FastingPhasesCanvas(
                relevantPhases = emptyList(),
                goalHours = 24.0,
                showSegments = false,
                currentPhase = currentPhase,
                modifier = Modifier.size(300.dp),
                onPhaseClick = { clickedPhase = it }
            )
        }

        // Click the measured ring edge rather than assuming one pixel per dp.
        composeTestRule.onNodeWithContentDescription("View fasting phase details").performTouchInput {
            click(androidx.compose.ui.geometry.Offset(center.x, 1f))
        }

        assertEquals(currentPhase, clickedPhase)
    }
}
