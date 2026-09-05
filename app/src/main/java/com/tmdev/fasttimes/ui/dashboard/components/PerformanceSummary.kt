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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.tmdev.fasttimes.ui.components.ExpressiveStatCard
import com.tmdev.fasttimes.ui.components.rememberRandomExpressiveShape
import com.tmdev.fasttimes.ui.theme.spacing
import kotlin.random.Random

/** Both tiles share this floor so they stay peers and grow, rather than clip, with the font scale. */
private val StatTileMinHeight = 112.dp

/**
 * Secondary "how am I doing" block: two equal stats that read as a pair and hand off to the full
 * statistics screen. Both tiles use the same surface role and the same shape token, so neither
 * competes with the goal picker above and neither changes shape between launches.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PerformanceSummary(
    fastsThisMonth: String,
    longestFastThisMonth: String,
    onStatisticsClick: () -> Unit,
    modifier: Modifier = Modifier,
    useExpressiveTheme: Boolean = false
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    onClickLabel = "Go to Statistics",
                    role = Role.Button,
                    onClick = onStatisticsClick
                )
                .defaultMinSize(minHeight = 48.dp)
                .padding(vertical = MaterialTheme.spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Performance",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            if (useExpressiveTheme) {
                val fastsMonthShape = rememberRandomExpressiveShape(seed = remember { Random.nextInt() })
                val longestFastMonthShape = rememberRandomExpressiveShape(seed = remember { Random.nextInt() })

                ExpressiveStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Fasts",
                    value = fastsThisMonth,
                    unit = "this month",
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = fastsMonthShape,
                    height = MaterialTheme.spacing.dashboardStatCardHeight
                )

                ExpressiveStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Longest Fast",
                    value = longestFastThisMonth,
                    unit = "this month",
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    shape = longestFastMonthShape,
                    height = MaterialTheme.spacing.dashboardStatCardHeight
                )
            } else {
                StatTile(
                    label = "Fasts",
                    value = fastsThisMonth,
                    unit = "this month",
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    label = "Longest Fast",
                    value = longestFastThisMonth,
                    unit = "this month",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.defaultMinSize(minHeight = StatTileMinHeight),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
