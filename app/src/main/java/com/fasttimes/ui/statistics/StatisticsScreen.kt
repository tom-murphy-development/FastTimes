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
package com.fasttimes.ui.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fasttimes.ui.components.StatisticTile
import com.fasttimes.ui.formatDuration
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

/**
 * Statistics screen displaying key fasting metrics.
 *
 * Displays three main statistics in a single row of tiles:
 * 1. **Streak**: The number of consecutive days with fasts
 * 2. **Average Fast**: The average duration of completed fasts
 * 3. **Trend**: Month-over-month fasting activity comparison
 *
 * @param onBackClick Callback invoked when the back button is pressed
 * @param onViewFastDetails Callback to navigate to fast details
 * @param viewModel The [StatisticsViewModel] for accessing statistics (injected via Hilt)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onBackClick: () -> Unit,
    onViewFastDetails: (Long) -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val state by viewModel.statisticsState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            // Loading state: centered progress indicator
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Content state: display statistics
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Text(
                    text = "Your Fasting Statistics",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Statistics tiles in a row with uniform height
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Streak tile
                    StatisticTileLeftAligned(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        description = "Streak",
                        value = "${state.streak.daysInARow} days",
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    // Average Fast tile
                    StatisticTileLeftAligned(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        description = "Average Fast",
                        value = formatDuration(state.averageFast),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    // Trend tile
                    StatisticTileLeftAligned(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        description = "Trend",
                        value = "${state.trend.currentMonthFasts} fasts",
                        subValue = formatTrendSubValue(state.trend),
                        trendIcon = if (state.trend.isUpward) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        trendIconColor = if (state.trend.isUpward) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }

                // Statistics Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Statistics",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                StatisticTile(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.BarChart,
                                    label = "Total Fasts",
                                    value = state.totalFasts.toString()
                                )
                                StatisticTile(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.Timer,
                                    label = "Total Time",
                                    value = formatDuration(state.totalFastingTime)
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                StatisticTile(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.Star,
                                    label = "Longest Fast",
                                    value = state.longestFast?.let { fast ->
                                        fast.endTime?.let { endTime ->
                                            formatDuration((endTime - fast.startTime).milliseconds)
                                        }
                                    } ?: "-",
                                    onClick = { state.longestFast?.id?.let(onViewFastDetails) }
                                )
                                StatisticTile(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.AvTimer,
                                    label = "Average Fast",
                                    value = formatDuration(state.averageFast)
                                )
                            }
                        }
                    }
                }

                // Additional details section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text(
                        text = "Additional Metrics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                            val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

                            // Total Fasts
                            DetailRow(
                                label = "Total Fasts",
                                value = state.totalFasts.toString()
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            // Total Fasting Time
                            DetailRow(
                                label = "Total Fasting Time",
                                value = formatDuration(state.totalFastingTime)
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            // Longest Fast
                            DetailRow(
                                label = "Longest Fast",
                                value = state.longestFast?.let { fast ->
                                    fast.endTime?.let { endTime ->
                                        formatDuration((endTime - fast.startTime).milliseconds)
                                    }
                                } ?: "-"
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            // Average Fasts Per Week
                            DetailRow(
                                label = "Avg. Fasts Per Week",
                                value = "%.1f".format(state.fastsPerWeek)
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            // Typical Start Time
                            DetailRow(
                                label = "Typical Start Time",
                                value = state.averageStartTime?.format(timeFormatter) ?: "-"
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            // Most Common Day
                            DetailRow(
                                label = "Most Common Day",
                                value = state.mostFrequentDay?.getDisplayName(TextStyle.FULL, Locale.getDefault()) ?: "-"
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            // First Fast Date
                            DetailRow(
                                label = "First Fast Date",
                                value = state.firstFastDate?.format(dateFormatter) ?: "-"
                            )

                            // Streak Details
                            if (state.streak.daysInARow > 0) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                DetailRow(
                                    label = "Streak Start Date",
                                    value = state.streak.startDate?.format(dateFormatter) ?: "-"
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                DetailRow(
                                    label = "Last Fast Date",
                                    value = state.streak.lastFastDate?.format(dateFormatter) ?: "-"
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            // Trend Details
                            DetailRow(
                                label = "Previous Month Fasts",
                                value = state.trend.previousMonthFasts.toString()
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            DetailRow(
                                label = "Month-over-Month Change",
                                value = formatTrendPercentage(state.trend),
                                valueColor = if (state.trend.isUpward) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * A composable that displays a statistic in a compact tile with left-aligned layout.
 */
@Composable
private fun StatisticTileLeftAligned(
    modifier: Modifier = Modifier,
    description: String,
    value: String,
    subValue: String? = null,
    trendIcon: ImageVector? = null,
    trendIconColor: Color = MaterialTheme.colorScheme.onSurface,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Description (top-left)
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Value (bottom-left) with optional trend indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (subValue != null) {
                        Text(
                            text = subValue,
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Trend icon (if provided)
                if (trendIcon != null) {
                    Icon(
                        imageVector = trendIcon,
                        contentDescription = "Trend indicator",
                        tint = trendIconColor,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

/**
 * A composable that displays a detail row with a label and value pair.
 */
@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

/**
 * Formats the trend sub-value as a readable string.
 */
private fun formatTrendSubValue(trend: FastingTrend): String {
    return if (trend.percentageChange != 0f) {
        val sign = if (trend.percentageChange > 0) "+" else ""
        "$sign${String.format("%.0f", trend.percentageChange)}%"
    } else {
        "No change"
    }
}

/**
 * Formats the trend percentage for the additional details section.
 */
private fun formatTrendPercentage(trend: FastingTrend): String {
    return if (trend.percentageChange != 0f) {
        val sign = if (trend.percentageChange > 0) "+" else ""
        "$sign${String.format("%.1f", trend.percentageChange)}% vs last month"
    } else {
        "No change vs last month"
    }
}
