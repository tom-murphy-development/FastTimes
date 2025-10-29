package com.fasttimes.ui.history

import androidx.compose.runtime.Immutable

@Immutable
enum class TimelineSegmentType {
    Fasting,
    NonFasting
}

@Immutable
data class TimelineSegment(
    val type: TimelineSegmentType,
    val weight: Float
)
