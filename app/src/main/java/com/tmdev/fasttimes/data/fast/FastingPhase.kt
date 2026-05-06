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

import com.tmdev.fasttimes.R

enum class FastingPhase(
    val nameResId: Int,
    val descriptionResId: Int,
    val startHour: Int,
    val endHour: Int? = null
) {
    POST_ABSORPTIVE(
        R.string.phase_post_absorptive_name,
        R.string.phase_post_absorptive_desc,
        0,
        12
    ),
    METABOLIC_SWITCH(
        R.string.phase_metabolic_switch_name,
        R.string.phase_metabolic_switch_desc,
        12,
        18
    ),
    EARLY_AUTOPHAGY(
        R.string.phase_early_autophagy_name,
        R.string.phase_early_autophagy_desc,
        18,
        24
    ),
    DEEP_KETOSIS_REPAIR(
        R.string.phase_deep_ketosis_repair_name,
        R.string.phase_deep_ketosis_repair_desc,
        24,
        72
    ),
    IMMUNE_REGENERATION(
        R.string.phase_immune_regeneration_name,
        R.string.phase_immune_regeneration_desc,
        72,
        null
    );

    companion object {
        fun getPhaseForDuration(durationHours: Double): FastingPhase {
            return entries.find { phase ->
                val startsAfterOrAt = durationHours >= phase.startHour
                val endsBefore = phase.endHour?.let { durationHours < it } ?: true
                startsAfterOrAt && endsBefore
            } ?: IMMUNE_REGENERATION
        }

        fun getPhasesForGoal(goalHours: Double): List<FastingPhase> {
            return entries.filter { it.startHour < goalHours }
        }
    }
}
