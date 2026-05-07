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
package com.tmdev.fasttimes.data.fast

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FastingPhaseTest {

    @Test
    fun `getPhaseForDuration returns correct phase for various durations`() {
        assertEquals(FastingPhase.POST_ABSORPTIVE, FastingPhase.getPhaseForDuration(0.0))
        assertEquals(FastingPhase.POST_ABSORPTIVE, FastingPhase.getPhaseForDuration(6.0))
        assertEquals(FastingPhase.POST_ABSORPTIVE, FastingPhase.getPhaseForDuration(11.9))
        
        assertEquals(FastingPhase.METABOLIC_SWITCH, FastingPhase.getPhaseForDuration(12.0))
        assertEquals(FastingPhase.METABOLIC_SWITCH, FastingPhase.getPhaseForDuration(15.0))
        assertEquals(FastingPhase.METABOLIC_SWITCH, FastingPhase.getPhaseForDuration(17.9))
        
        assertEquals(FastingPhase.EARLY_AUTOPHAGY, FastingPhase.getPhaseForDuration(18.0))
        assertEquals(FastingPhase.EARLY_AUTOPHAGY, FastingPhase.getPhaseForDuration(20.0))
        assertEquals(FastingPhase.EARLY_AUTOPHAGY, FastingPhase.getPhaseForDuration(23.9))
        
        assertEquals(FastingPhase.DEEP_KETOSIS_REPAIR, FastingPhase.getPhaseForDuration(24.0))
        assertEquals(FastingPhase.DEEP_KETOSIS_REPAIR, FastingPhase.getPhaseForDuration(36.0))
        assertEquals(FastingPhase.DEEP_KETOSIS_REPAIR, FastingPhase.getPhaseForDuration(47.9))
        
        assertEquals(FastingPhase.DEEP_KETOSIS_REPAIR, FastingPhase.getPhaseForDuration(48.0))
        assertEquals(FastingPhase.IMMUNE_REGENERATION, FastingPhase.getPhaseForDuration(72.0))
        assertEquals(FastingPhase.IMMUNE_REGENERATION, FastingPhase.getPhaseForDuration(100.0))
    }

    @Test
    fun `getPhasesForGoal returns only relevant phases`() {
        // 16 hour goal
        val phases16 = FastingPhase.getPhasesForGoal(16.0)
        assertEquals(2, phases16.size)
        assertEquals(FastingPhase.POST_ABSORPTIVE, phases16[0])
        assertEquals(FastingPhase.METABOLIC_SWITCH, phases16[1])

        // 20 hour goal
        val phases20 = FastingPhase.getPhasesForGoal(20.0)
        assertEquals(3, phases20.size)
        assertEquals(FastingPhase.EARLY_AUTOPHAGY, phases20[2])

        // 72 hour goal
        val phases72 = FastingPhase.getPhasesForGoal(72.0)
        assertEquals(4, phases72.size)
        assertEquals(FastingPhase.DEEP_KETOSIS_REPAIR, phases72[3])
    }

    @Test
    fun `getPhasesForGoal returns empty for zero or negative goal`() {
        assertTrue(FastingPhase.getPhasesForGoal(0.0).isEmpty())
        assertTrue(FastingPhase.getPhasesForGoal(-1.0).isEmpty())
    }

    @Test
    fun `getPhaseForDuration handles very long durations`() {
        assertEquals(FastingPhase.IMMUNE_REGENERATION, FastingPhase.getPhaseForDuration(1000.0))
    }
}
