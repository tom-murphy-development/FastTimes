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
package com.tmdev.fasttimes.ui.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tmdev.fasttimes.data.profile.FastingProfile
import com.tmdev.fasttimes.data.profile.durationMinutes
import com.tmdev.fasttimes.ui.theme.spacing

/** Minimum touch target, kept above the 48 dp floor at every font scale. */
private val MinTargetHeight = 56.dp

/**
 * The dashboard's primary task: pick a fasting goal and start it.
 *
 * Renders one recommended goal with its own description in a tonal container, then the remaining
 * goals as outlined secondary actions. The single filled [Button] is the obvious primary action;
 * every other goal is an [OutlinedButton]. There are no nested cards, and every shape is a
 * [MaterialTheme.shapes] token so the geometry is identical on every launch.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GoalPicker(
    profiles: List<FastingProfile>,
    onStartFast: (FastingProfile) -> Unit,
    onManageProfilesClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val recommended = recommendedProfile(profiles)
    val remaining = profiles.filter { it !== recommended }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Set your goal",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
            TextButton(
                onClick = onManageProfilesClick,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(text = "Manage Profiles")
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.padding(start = MaterialTheme.spacing.extraSmall)
                )
            }
        }

        recommended?.let { profile ->
            RecommendedGoal(profile = profile, onStartFast = onStartFast)
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            maxItemsInEachRow = 2
        ) {
            remaining.forEach { profile ->
                GoalCard(
                    profile = profile,
                    onStartFast = onStartFast,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * The goal offered first: the user's favourite, else the first timed profile, else whatever exists.
 * Deliberately derived from [profiles] so the choice is stable for a given profile list.
 */
internal fun recommendedProfile(profiles: List<FastingProfile>): FastingProfile? =
    profiles.firstOrNull { it.isFavorite }
        ?: profiles.firstOrNull { it.durationMinutes > 0L }
        ?: profiles.firstOrNull()

@Composable
private fun RecommendedGoal(
    profile: FastingProfile,
    onStartFast: (FastingProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = { onStartFast(profile) },
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 88.dp),
        shape = MaterialTheme.shapes.extraLarge,
        contentPadding = PaddingValues(MaterialTheme.spacing.medium),
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = profile.accessibilityLabel()
                    },
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = profile.durationLabel(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = profile.displayName,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (profile.description.isNotBlank()) {
                    Text(
                        text = profile.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    )
}

@Composable
private fun GoalCard(
    profile: FastingProfile,
    onStartFast: (FastingProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = { onStartFast(profile) },
        modifier = modifier
            .defaultMinSize(minHeight = MinTargetHeight),
        shape = MaterialTheme.shapes.large,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.medium)
                    .semantics {
                        contentDescription = profile.accessibilityLabel()
                    },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = profile.durationLabel(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = profile.displayName,
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
    )
}

private fun FastingProfile.accessibilityLabel(): String =
    buildString {
        append("Start ")
        append(durationLabel())
        append(' ')
        append(displayName)
        if (description.isNotBlank()) {
            append(". ")
            append(description)
        }
    }

/**
 * "16h" for a timed profile, "∞" for an open one — the same glyphs the dashboard has always used.
 * [FastingProfile.durationMinutes] holds milliseconds despite its name; the divisor matches the
 * arithmetic this screen used before extraction.
 */
internal fun FastingProfile.durationLabel(): String =
    if (durationMinutes == 0L) "∞" else "${durationMinutes / 3_600_000}h"
