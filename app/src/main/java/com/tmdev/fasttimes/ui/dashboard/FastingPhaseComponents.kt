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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmdev.fasttimes.data.AppTheme
import com.tmdev.fasttimes.data.fast.FastingPhase
import com.tmdev.fasttimes.ui.theme.FastTimesTheme
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

val FastingPhase.color: Color
    get() = when (this) {
        FastingPhase.POST_ABSORPTIVE -> Color(0xFF4CAF50)
        FastingPhase.METABOLIC_SWITCH -> Color(0xFFFF9800)
        FastingPhase.EARLY_AUTOPHAGY -> Color(0xFFFF5722)
        FastingPhase.DEEP_KETOSIS_REPAIR -> Color(0xFFE91E63)
        FastingPhase.IMMUNE_REGENERATION -> Color(0xFF9C27B0)
    }

@Composable
fun FastingPhaseChip(
    phase: FastingPhase,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(phase.color.copy(alpha = 0.1f))
            .border(1.dp, phase.color.copy(alpha = 0.2f), CircleShape)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = stringResource(phase.nameResId),
            style = MaterialTheme.typography.labelSmall,
            color = phase.color,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun FastingPhasesCanvas(
    relevantPhases: List<FastingPhase>,
    goalHours: Double,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 16.dp,
    showSegments: Boolean = true,
    currentPhase: FastingPhase? = null,
    onPhaseClick: (FastingPhase) -> Unit = {}
) {
    if (goalHours <= 0) return

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .onSizeChanged { canvasSize = it }
            .pointerInput(relevantPhases, goalHours, showSegments, currentPhase) {
                detectTapGestures { offset ->
                    if (canvasSize.width <= 0 || canvasSize.height <= 0) return@detectTapGestures

                    val center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
                    val dx = offset.x - center.x
                    val dy = offset.y - center.y
                    
                    val distance = sqrt(dx * dx + dy * dy)
                    val strokeWidthPx = strokeWidth.toPx()
                    val radius = (min(canvasSize.width.toDouble(), canvasSize.height.toDouble()).toFloat() - strokeWidthPx) / 2f
                    
                    // Allow a generous buffer around the arc for easier clicking
                    if (distance < radius - strokeWidthPx * 2f || distance > radius + strokeWidthPx * 2f) {
                        return@detectTapGestures
                    }

                    if (!showSegments) {
                        currentPhase?.let(onPhaseClick)
                        return@detectTapGestures
                    }

                    val angleRadians = atan2(dy, dx)
                    val angleDegrees = Math.toDegrees(angleRadians.toDouble()).toFloat()
                    
                    // Normalize to [0, 360) where 0 is at -90 degrees (Top)
                    var normalizedAngle = (angleDegrees + 90f) % 360f
                    if (normalizedAngle < 0) normalizedAngle += 360f
                    
                    val hour = (normalizedAngle / 360f) * goalHours.toFloat()
                    
                    val clickedPhase = relevantPhases.find { phase ->
                        if (phase.startHour == 0) return@find false
                        val angleDegreesForMarker = -90f + (phase.startHour / goalHours.toFloat() * 360f)
                        var normalizedMarkerAngle = (angleDegreesForMarker + 90f) % 360f
                        if (normalizedMarkerAngle < 0) normalizedMarkerAngle += 360f
                        
                        val angleDiff = abs(normalizedAngle - normalizedMarkerAngle)
                        val circularAngleDiff = min(angleDiff.toDouble(), 360.0 - angleDiff)
                        
                        // Dot radius is 7dp, let's use a bit more for touch target (e.g. 15-20dp)
                        // At a radius of ~150dp, 10 degrees is about 26dp.
                        circularAngleDiff < 8.0
                    } ?: relevantPhases.findLast { it.startHour <= hour }
                    ?: relevantPhases.firstOrNull()
                    
                    clickedPhase?.let(onPhaseClick)
                }
            }
            .semantics {
                onClick(label = "View fasting phase details") {
                    relevantPhases.lastOrNull()?.let { onPhaseClick(it) }
                    true
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidthPx = strokeWidth.toPx()
            val arcSize = Size(size.width - strokeWidthPx, size.height - strokeWidthPx)
            val arcTopLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2)
            val center = Offset(size.width / 2, size.height / 2)
            val radius = (size.minDimension - strokeWidthPx) / 2

            // Draw phase segments
            if (showSegments) {
                relevantPhases.forEach { phase ->
                    val startAngle = -90f + (phase.startHour / goalHours.toFloat() * 360f)
                    val endHour = phase.endHour?.toFloat() ?: goalHours.toFloat()
                    val clampedEndHour = endHour.coerceAtMost(goalHours.toFloat())
                    val sweepAngle = ((clampedEndHour - phase.startHour) / goalHours.toFloat() * 360f)

                    if (sweepAngle > 0) {
                        drawArc(
                            color = phase.color.copy(alpha = 0.25f),
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = arcTopLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidthPx)
                        )
                    }
                }

                // Draw phase markers (dots)
                relevantPhases.forEach { phase ->
                    if (phase.startHour > 0 && phase.startHour < goalHours) {
                        val angleDegrees = -90f + (phase.startHour / goalHours.toFloat() * 360f)
                        val angleRadians = Math.toRadians(angleDegrees.toDouble())
                        val x = center.x + radius * cos(angleRadians).toFloat()
                        val y = center.y + radius * sin(angleRadians).toFloat()

                        drawCircle(
                            color = Color.Black.copy(alpha = 0.5f),
                            radius = 7.dp.toPx(),
                            center = Offset(x, y)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 4.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FastingPhaseInfoContent(
    phase: FastingPhase,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 16.dp, top = 0.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(phase.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = phase.color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = stringResource(phase.nameResId),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = phase.color
            )
        }
        
        Text(
            text = AnnotatedString.fromHtml(stringResource(phase.descriptionResId)),
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 22.sp
        )
        
        Spacer(Modifier.height(4.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Timing",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                val timeRange = if (phase.endHour != null) {
                    "${phase.startHour} – ${phase.endHour} hours"
                } else {
                    "${phase.startHour}+ hours"
                }
                Text(
                    text = timeRange,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .width(2.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Benefit",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = when(phase) {
                        FastingPhase.POST_ABSORPTIVE -> "Glucose Stabilisation"
                        FastingPhase.METABOLIC_SWITCH -> "Fat Burning"
                        FastingPhase.EARLY_AUTOPHAGY -> "Cellular Repair"
                        FastingPhase.DEEP_KETOSIS_REPAIR -> "Mental Clarity & Metabolic Flexibility"
                        FastingPhase.IMMUNE_REGENERATION -> "Immune Reset & Heart Health"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FastingPhaseInfoModal(
    phase: FastingPhase,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        FastingPhaseInfoContent(phase = phase, modifier = Modifier.padding(bottom = 16.dp))
    }
}

@Preview(showBackground = true, name = "28h Fast Scenario")
@Composable
fun FastingPhasesActiveFastPreview() {
    val goalHours = 28.0
    val elapsedHours = 26.0
    val progress = (elapsedHours / goalHours).toFloat()
    val currentPhase = FastingPhase.DEEP_KETOSIS_REPAIR
    val relevantPhases = FastingPhase.entries.filter { it.startHour < goalHours }

    FastTimesTheme(
        theme = AppTheme.SYSTEM,
        seedColor = Color(0xFF6750A4),
        accentColor = Color(0xFF4CAF50),
        useExpressiveTheme = true,
        useSystemColors = false
    ) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                Text(
                    text = "Active Fast Visualization",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Box(contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.size(240.dp)) {
                        FastingPhasesCanvas(
                            relevantPhases = relevantPhases,
                            goalHours = goalHours,
                            modifier = Modifier.fillMaxSize(),
                            currentPhase = currentPhase
                        )
                        
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxSize(),
                            color = Color(0xFF4CAF50),
                            strokeWidth = 12.dp,
                            trackColor = Color(0xFF4CAF50).copy(alpha = 0.1f),
                            strokeCap = StrokeCap.Round
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        FastingPhaseChip(phase = currentPhase)
                        Spacer(Modifier.height(8.dp))
                        Text("Remaining", style = MaterialTheme.typography.titleSmall)
                        Text(
                            text = "02:00:00",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    tonalElevation = 2.dp
                ) {
                    FastingPhaseInfoContent(phase = currentPhase)
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "All Phases Gallery")
@Composable
fun FastingPhasesPreview() {
    FastTimesTheme(
        theme = AppTheme.SYSTEM,
        seedColor = Color(0xFF6750A4),
        accentColor = Color(0xFF9C27B0),
        useExpressiveTheme = false,
        useSystemColors = false
    ) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Fasting Phases Gallery", style = MaterialTheme.typography.titleLarge)

                // Canvas with all phases for a 24h goal
                Box(modifier = Modifier.size(200.dp)) {
                    FastingPhasesCanvas(
                        relevantPhases = FastingPhase.entries.filter { it.startHour < 24 },
                        goalHours = 24.0,
                        modifier = Modifier.fillMaxSize()
                    )
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("24h Goal", style = MaterialTheme.typography.labelMedium)
                    }
                }

                // All phases detail
                FastingPhase.entries.forEach { phase ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        FastingPhaseInfoContent(phase = phase)
                    }
                }
            }
        }
    }
}
