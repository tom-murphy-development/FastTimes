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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fasttimes.data.FastingProfile
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
    onEditClick: (Long) -> Unit,
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
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    Text(
                        text = "Details for ${targetDate.format(dateFormatter)}",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Timeline(
                        segments = targetTimeline,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                if (targetFasts.isEmpty()) {
                    item {
                        Text(
                            text = "No fasts recorded for this day.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    itemsIndexed(targetFasts) { index, fast ->
                        FastDetailItem(
                            fast = fast,
                            onEditClick = { onEditClick(fast.id) },
                            date = targetDate
                        )
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
    onEditClick: () -> Unit,
    date: LocalDate,
    modifier: Modifier = Modifier
) {
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")

    val durationString = if (fast.end != null) {
        val duration = Duration.between(fast.start, fast.end)
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        "${hours}h ${minutes}m"
    } else {
        "In progress"
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = durationString,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                if (fast.profile != FastingProfile.MANUAL) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = fast.profile.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (fast.targetDuration != null && fast.targetDuration > 0) {
                    Icon(
                        imageVector = if (fast.goalMet()) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                        contentDescription = if (fast.goalMet()) "Goal Reached" else "Goal Not Reached",
                        tint = if (fast.goalMet()) Color(0xFF3DDC84) else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = 0.5f
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Fast"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row {
                Text(
                    text = "Start:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(50.dp)
                )
                Text(
                    text = if (fast.start.toLocalDate() == date) {
                        fast.start.format(timeFormatter)
                    } else {
                        "${fast.start.format(timeFormatter)} - ${fast.start.format(dateFormatter)}"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    Text(
                        text = "End:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(50.dp)
                    )
                    Text(
                        text = fast.end?.let {
                            if (it.toLocalDate() == date) {
                                it.format(timeFormatter)
                            } else {
                                "${it.format(timeFormatter)} on ${it.format(dateFormatter)}"
                            }
                        } ?: "In progress",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (fast.rating != null) {
                    RatingBar(rating = fast.rating!!)
                }
            }
        }
    }
}

@Composable
private fun RatingBar(rating: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        repeat(5) { index ->
            val icon = if (index < rating) Icons.Filled.Star else Icons.Outlined.StarBorder
            Icon(
                imageVector = icon,
                contentDescription = null, // decorative
                tint = if (index < rating) Color(0xFF3DDC84) else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
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
                targetDuration = 16 * 3600 * 1000L,
                profile = FastingProfile.SIXTEEN_EIGHT,
                rating = 4
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
                targetDuration = 12 * 3600 * 1000L,
                profile = FastingProfile.TWELVE_TWELVE,
                rating = 5
            ),
            Fast(
                id = 3,
                startTime = ZonedDateTime.of(
                    previewDate.minusDays(1).atTime(18, 0),
                    java.time.ZoneId.systemDefault()
                ).toInstant().toEpochMilli(),
                endTime = null,
                targetDuration = 18 * 3600 * 1000L,
                profile = FastingProfile.EIGHTEEN_SIX
            )
        )
        val timeline = generateTimelineSegments(previewDate, previewFasts)
        DailyFastDetailsSheet(
            date = previewDate,
            fasts = previewFasts,
            timeline = timeline,
            onSwipeLeft = { },
            onSwipeRight = { },
            onEditClick = { }
        )
    }
}
