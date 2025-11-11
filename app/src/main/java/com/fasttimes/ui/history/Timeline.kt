package com.fasttimes.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fasttimes.ui.theme.FastTimesPreviewTheme
import com.fasttimes.ui.theme.FastTimesTheme

@Composable
fun Timeline(
    segments: List<TimelineSegment>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(MaterialTheme.shapes.small)
        ) {
            segments.forEach { segment ->
                val color = when (segment.type) {
                    TimelineSegmentType.Fasting -> FastTimesTheme.accentColor
                    TimelineSegmentType.NonFasting -> Color.Gray
                }
                Box(
                    modifier = Modifier
                        .weight(segment.weight)
                        .fillMaxHeight()
                        .background(color)
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("12AM", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("6AM", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("12PM", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("6PM", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Preview(showBackground = true, widthDp = 300)
@Composable
private fun TimelinePreview() {
    FastTimesPreviewTheme {
        Timeline(
            segments = listOf(
                TimelineSegment(TimelineSegmentType.NonFasting, 0.25f),       // 12AM - 6AM
                TimelineSegment(TimelineSegmentType.Fasting, 0.5f),      // 6AM - 6PM
                TimelineSegment(TimelineSegmentType.NonFasting, 0.25f)       // 6PM - 12AM
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}
