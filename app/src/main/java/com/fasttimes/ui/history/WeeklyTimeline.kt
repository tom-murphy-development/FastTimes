package com.fasttimes.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fasttimes.ui.theme.FastTimesTheme

@Composable
fun WeeklyTimeline(
    segments: List<TimelineSegment>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
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
}
