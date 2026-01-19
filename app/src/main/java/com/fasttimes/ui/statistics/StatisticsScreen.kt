/*
 * Copyright (C) 2025 tom-murphy-development
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fasttimes.ui.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import androidx.hilt.navigation.compose.hiltViewModel
import com.fasttimes.ui.formatDuration
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale
import kotlin.random.Random
import kotlin.time.Duration

/**
 * Statistics screen displaying key fasting metrics with Material 3 Expressive elements.
 *
 * This version uses dynamic [RoundedPolygon] shapes for stat cards, ensuring organic
 * proportions and centered content fit. Shapes are randomized each time the screen loads.
 *
 * @param onBackClick Callback for the navigation back button.
 * @param onHistoryClick Callback to navigate to the history screen.
 * @param viewModel The statistics view model.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StatisticsScreen(
    onBackClick: () -> Unit,
    onHistoryClick: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val state by viewModel.statisticsState.collectAsState()

    // Generate random stable expressive shapes for the summary cards.
    // By using remember { Random.nextInt() }, we get a new seed only when the screen
    // is initially composed (i.e., every time it "loads").
    val streakShape = rememberRandomExpressiveShape(seed = remember { Random.nextInt() })
    val trendShape = rememberRandomExpressiveShape(seed = remember { Random.nextInt() })
    val weeklyShape = rememberRandomExpressiveShape(seed = remember { Random.nextInt() })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Performance", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hero Header Section with Navigation
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "This week",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onHistoryClick, modifier = Modifier.size(32.dp)) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = "History",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = formatHoursOnly(state.averageFast),
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 56.sp),
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = " avg hrs",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 12.dp, start = 8.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "Goal met on ${state.weeklyActivity.count { it.isGoalMet }} days. Great progress!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Weekly Activity Chart with Dynamic Goal Lines
                WeeklyActivityChart(
                    activity = state.weeklyActivity,
                    goals = state.weeklyGoals,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )

                // Expressive Asymmetrical Summary Cards with Randomized Material Polygons
                Column {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Trends",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(1.dp)
                    )
                {
                    ExpressiveStatCard(
                        modifier = Modifier.weight(1f),
                        label = "Streak",
                        value = "${state.streak.daysInARow}",
                        unit = "days",
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        shape = streakShape
                    )
                    ExpressiveStatCard(
                        modifier = Modifier.weight(1f),
                        label = "Trend",
                        value = "${state.fastsPerWeek}",
                        unit = "vs last month",
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        shape = trendShape
                    )
                    ExpressiveStatCard(
                        modifier = Modifier.weight(1f),
                        label = "Weekly Fasts",
                        value = formatTrendPercentage(state.trend),
                        unit = "days",
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        shape = weeklyShape
                    )
                    }
                }

                // All-time Metrics Section
                Column {
                    Text(
                        text = "All-time statistics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            DetailRow("Total Fasts", state.totalFasts.toString())
                            HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            
                            DetailRow("Total Duration", formatDuration(state.totalFastingTime))
                            HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            DetailRow("Fasts Per Week", "%.1f".format(state.fastsPerWeek))
                            HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            
                            DetailRow(
                                "Common Day", 
                                state.mostFrequentDay?.getDisplayName(TextStyle.FULL, Locale.getDefault()) ?: "-"
                            )
                            
                            state.firstFastDate?.let { date ->
                                HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                                DetailRow("Journey Started", date.format(dateFormatter))
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Creates a [Shape] from a [RoundedPolygon], scaled uniformly to 90% and centered.
 */
@ExperimentalMaterial3ExpressiveApi
@Composable
fun rememberPolygonShape(polygon: RoundedPolygon): Shape {
    return remember(polygon) {
        object : Shape {
            override fun createOutline(
                size: Size,
                layoutDirection: LayoutDirection,
                density: Density
            ): Outline {
                val path = polygon.toPath().asComposePath()
                val bounds = path.getBounds()
                val matrix = Matrix()
                
                // Use uniform scaling to 90% of original size to prevent stretching
                val scale = minOf(size.width / bounds.width, size.height / bounds.height) * 0.95f

                // Center and scale to fit destination box
                matrix.translate(size.width / 2f, size.height / 2f)
                matrix.scale(scale, scale)
                matrix.translate(-(bounds.left + bounds.width / 2f), -(bounds.top + bounds.height / 2f))
                
                path.transform(matrix)
                return Outline.Generic(path)
            }
        }
    }
}

/**
 * Returns a random expressive shape from the [MaterialShapes] collection.
 */
@ExperimentalMaterial3ExpressiveApi
@Composable
fun rememberRandomExpressiveShape(seed: Int): Shape {
    val shapes = remember {
        listOf(
            MaterialShapes.Pentagon,
            MaterialShapes.Clover8Leaf,
            MaterialShapes.Sunny,
            MaterialShapes.Cookie9Sided,
            MaterialShapes.Ghostish,
            MaterialShapes.Slanted,
            MaterialShapes.Gem,
            MaterialShapes.Clover4Leaf
        )
    }
    // Using Math.abs to handle potential negative Random.nextInt() results
    val polygon = shapes[Math.abs(seed) % shapes.size]
    return rememberPolygonShape(polygon)
}

/**
 * A custom chart displaying weekly fasting activity with pill-shaped bars and success indicators.
 */
@Composable
fun WeeklyActivityChart(
    activity: List<DailyActivity>,
    goals: Set<Float>,
    modifier: Modifier = Modifier
) {
    val maxHours = activity.maxOfOrNull { it.durationHours }?.coerceAtLeast(24f) ?: 24f
    val goalHours = 16f
    
    Box(modifier = modifier) {
        goals.forEach { goalVal ->
            val goalYRatio = 1f - (goalVal / maxHours)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
                    .fillMaxHeight(goalYRatio)
            ) {
                HorizontalDivider(
                    modifier = Modifier.align(Alignment.BottomStart),
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f),
                    thickness = 1.dp
                )
                Text(
                    text = "${goalVal.toInt()}h",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 4.dp),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            activity.forEach { daily ->
                val barHeightWeight = (daily.durationHours / maxHours).coerceIn(0.06f, 1f)
                
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .fillMaxHeight(barHeightWeight)
                            .clip(CircleShape) // Rounded pill shape
                            .background(
                                if (daily.isGoalMet) MaterialTheme.colorScheme.tertiary 
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            ),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        if (daily.isGoalMet) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.padding(4.dp).size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.padding(2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = daily.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * An expressive summary card with a custom [Shape].
 * Content is centered and sized to fit comfortably with flexible sizing.
 */
@Composable
fun ExpressiveStatCard(
    label: String,
    value: String,
    unit: String,
    containerColor: Color,
    contentColor: Color,
    shape: Shape,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label, 
                style = MaterialTheme.typography.labelLarge,
                color = contentColor.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value, 
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 40.sp,
                    lineHeight = 44.sp
                ),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = unit, 
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

/**
 * A detail row for secondary statistics.
 */
@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}

/**
 * Helper to format duration as a decimal number of hours.
 */
private fun formatHoursOnly(duration: Duration): String {
    val totalHours = duration.inWholeMinutes / 60f
    return String.format(Locale.getDefault(), "%.1f", totalHours)
}

/**
 * Helper to format trend percentage with a sign.
 */
private fun formatTrendPercentage(trend: FastingTrend): String {
    val sign = if (trend.percentageChange > 0) "+" else ""
    return "$sign${String.format(Locale.getDefault(), "%.0f", trend.percentageChange)}%"
}
