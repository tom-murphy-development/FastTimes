package com.fasttimes.ui.history

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fasttimes.data.fast.Fast
import com.fasttimes.ui.theme.FastTimesTheme
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun DailyFastDetailsSheet(
    date: LocalDate,
    fasts: List<Fast>,
    timeline: List<TimelineSegment>,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMMM d")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .horizontalSwipe(onSwipeLeft = onSwipeLeft, onSwipeRight = onSwipeRight)
            .padding(24.dp)
    ) {
        AnimatedContent(
            targetState = Triple(date, fasts, timeline),
            label = "DailyFastDetailsAnimation",
            transitionSpec = {
                if (targetState.first.isAfter(initialState.first)) {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                } else {
                    slideInHorizontally { width -> -width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> width } + fadeOut()
                }
            }
        ) { (targetDate, targetFasts, targetTimeline) ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Details for ${targetDate.format(dateFormatter)}",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Spacer(modifier = Modifier.height(16.dp))

                Timeline(segments = targetTimeline, modifier = Modifier.padding(bottom = 16.dp))

                if (targetFasts.isEmpty()) {
                    Text(
                        text = "No fasts recorded for this day.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    targetFasts.forEachIndexed { index, fast ->
                        FastDetailItem(fast = fast)
                        if (index < targetFasts.size - 1) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FastDetailItem(
    fast: Fast,
    modifier: Modifier = Modifier
) {
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    val multiDayFormatter = DateTimeFormatter.ofPattern("EEE d/MM")

    val startTime = fast.start
    val endTime = fast.end ?: ZonedDateTime.now()

    val durationString = if (fast.end != null) {
        val duration = Duration.between(startTime, endTime)
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        "${hours}h ${minutes}m"
    } else {
        "In progress"
    }

    val timeRangeString = if (fast.end != null) {
        val startFormatted = startTime.format(timeFormatter)
        val endFormatted = endTime.format(timeFormatter)
        val dayDiff = endTime.dayOfYear - startTime.dayOfYear

        if (dayDiff > 0) {
            val startDateFormatted = startTime.format(multiDayFormatter)
            val endDateFormatted = endTime.format(multiDayFormatter)
            "$startDateFormatted $startFormatted - $endDateFormatted $endFormatted"
        } else {
            "$startFormatted - $endFormatted"
        }
    } else {
        "${startTime.format(timeFormatter)} - Present"
    }

    val goalString = if (fast.targetDuration != null) {
        val duration = Duration.ofMillis(fast.targetDuration)
        val hours = duration.toHours()
        val minutes = (duration.toMinutes() % 60)
        if (hours > 0) {
            if (minutes > 0) "${hours}h ${minutes}m" else "${hours}h"
        } else {
            "${minutes}m"
        }
    } else {
        "No goal set"
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = durationString,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = timeRangeString,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = goalString,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = if (fast.goalMet()) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                contentDescription = if (fast.goalMet()) "Goal Reached" else "Goal Not Reached",
                tint = if (fast.goalMet()) Color(0xFF388E3C) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun DailyFastDetailsSheetPreview() {
    FastTimesTheme {
        val previewDate = LocalDate.of(2024, 6, 24)
        val previewFasts = listOf(
            Fast(
                id = 1,
                startTime = ZonedDateTime.of(
                    previewDate.atTime(20, 15),
                    java.time.ZoneId.systemDefault()
                ).toInstant().toEpochMilli(),
                endTime = ZonedDateTime.of(
                    previewDate.plusDays(1).atTime(14, 47),
                    java.time.ZoneId.systemDefault()
                ).toInstant().toEpochMilli(),
                targetDuration = 16 * 3600 * 1000L
            ),
            Fast(
                id = 2,
                startTime = ZonedDateTime.of(
                    previewDate.atTime(6, 0),
                    java.time.ZoneId.systemDefault()
                ).toInstant().toEpochMilli(),
                endTime = ZonedDateTime.of(
                    previewDate.atTime(10, 11),
                    java.time.ZoneId.systemDefault()
                ).toInstant().toEpochMilli(),
                targetDuration = 12 * 3600 * 1000L
            )
        )
        val timeline = generateTimelineSegments(previewDate, previewFasts)
        DailyFastDetailsSheet(
            date = previewDate,
            fasts = previewFasts,
            timeline = timeline,
            onSwipeLeft = {},
            onSwipeRight = {},
        )
    }
}
