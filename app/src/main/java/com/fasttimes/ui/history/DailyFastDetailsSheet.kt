package com.fasttimes.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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
import com.fasttimes.data.toLocalDateTime
import com.fasttimes.ui.theme.FastTimesTheme
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DailyFastDetailsSheet(
    date: LocalDate,
    fasts: List<Fast>,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMMM d")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "Details for ${date.format(dateFormatter)}",
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (fasts.isEmpty()) {
            Text(
                text = "No fasts recorded for this day.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            fasts.forEachIndexed { index, fast ->
                FastDetailItem(fast = fast)
                if (index < fasts.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
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
    val startTime = fast.startTime.toLocalDateTime()
    val endTime = fast.endTime?.toLocalDateTime()

    val durationString = if (endTime != null) {
        val duration = Duration.between(startTime, endTime)
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        "${hours}h ${minutes}m"
    } else {
        "In progress"
    }

    val timeRangeString = if (endTime != null) {
        val startFormatted = startTime.format(timeFormatter)
        val endFormatted = endTime.format(timeFormatter)
        val dayDiff = endTime.dayOfYear - startTime.dayOfYear
        if (dayDiff > 0) {
            "$startFormatted - $endFormatted (+$dayDiff)"
        } else {
            "$startFormatted - $endFormatted"
        }
    } else {
        "${startTime.format(timeFormatter)} - Present"
    }

    val goalString = if (fast.targetDuration != null) {
        val hours = fast.targetDuration / 3600
        "${hours} hours"
    } else {
        "No goal set"
    }

    val goalReached = fast.goalReached()

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
                imageVector = Icons.Default.CheckCircle,
                contentDescription = if (goalReached) "Goal Reached" else "Goal Not Reached",
                tint = if (goalReached) Color(0xFF388E3C) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

private fun Fast.goalReached(): Boolean {
    if (endTime == null || targetDuration == null) return false
    val duration = Duration.between(
        startTime.toLocalDateTime(),
        endTime.toLocalDateTime()
    ).seconds
    return duration >= targetDuration
}

@Preview(showBackground = true)
@Composable
private fun DailyFastDetailsSheetPreview() {
    FastTimesTheme {
        val previewDate = LocalDate.of(2024, 6, 24)
        val previewFasts = listOf(
            Fast(
                id = 1,
                startTime = previewDate.atTime(20, 15).toEpochSecond(java.time.ZoneOffset.UTC),
                endTime = previewDate.plusDays(1).atTime(14, 47).toEpochSecond(java.time.ZoneOffset.UTC),
                targetDuration = 16 * 3600
            ),
            Fast(
                id = 2,
                startTime = previewDate.atTime(6, 0).toEpochSecond(java.time.ZoneOffset.UTC),
                endTime = previewDate.atTime(10, 11).toEpochSecond(java.time.ZoneOffset.UTC),
                targetDuration = 12 * 3600
            )
        )
        DailyFastDetailsSheet(date = previewDate, fasts = previewFasts)
    }
}
