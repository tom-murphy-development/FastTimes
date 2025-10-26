/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fasttimes.ui.history

import androidx.compose.ui.graphics.Color
import com.fasttimes.data.fast.Fast
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Generates a list of [TimelineSegment] for a given day based on a list of fasts.
 *
 * The timeline represents a 24-hour period. Segments are colored green during fasting periods
 * and gray during non-fasting periods.
 *
 * @param date The specific date for which to generate the timeline.
 * @param fasts A list of [Fast] objects that might overlap with the given date.
 * @return A list of [TimelineSegment] that represents the 24-hour timeline for the day.
 */
fun generateTimelineSegments(date: LocalDate, fasts: List<Fast>): List<TimelineSegment> {
    val dayStart = ZonedDateTime.of(date, LocalTime.MIN, ZoneId.systemDefault())
    val dayEnd = dayStart.plusDays(1)
    val totalMinutesInDay = 1440f

    val relevantFasts = fasts.filter {
        val fastEnd = it.end ?: ZonedDateTime.now()
        it.start.isBefore(dayEnd) && fastEnd.isAfter(dayStart)
    }

    if (relevantFasts.isEmpty()) {
        return listOf(TimelineSegment(Color.Gray, 1f))
    }

    val sortedFasts = relevantFasts.sortedBy { it.start }

    // 1. Merge overlapping and contiguous fast intervals
    val mergedIntervals = mutableListOf<Pair<ZonedDateTime, ZonedDateTime>>()
    if (sortedFasts.isNotEmpty()) {
        var currentStart = sortedFasts.first().start
        var currentEnd = sortedFasts.first().end ?: ZonedDateTime.now()

        for (i in 1 until sortedFasts.size) {
            val nextFast = sortedFasts[i]
            val nextStart = nextFast.start
            val nextEnd = nextFast.end ?: ZonedDateTime.now()

            if (!nextStart.isAfter(currentEnd)) { // Overlap or contiguous
                if (nextEnd.isAfter(currentEnd)) { // Merge
                    currentEnd = nextEnd
                }
            } else { // No overlap
                mergedIntervals.add(currentStart to currentEnd)
                currentStart = nextStart
                currentEnd = nextEnd
            }
        }
        mergedIntervals.add(currentStart to currentEnd)
    }

    // 2. Create a sorted list of unique event points (timestamps) within the day
    val eventPoints = sortedSetOf<ZonedDateTime>()
    eventPoints.add(dayStart)
    eventPoints.add(dayEnd)

    mergedIntervals.forEach { (start, end) ->
        if (start.isAfter(dayStart) && start.isBefore(dayEnd)) {
            eventPoints.add(start)
        }
        if (end.isAfter(dayStart) && end.isBefore(dayEnd)) {
            eventPoints.add(end)
        }
    }

    // 3. Create segments between each event point
    val segments = mutableListOf<TimelineSegment>()
    val pointList = eventPoints.toList()

    for (i in 0 until pointList.size - 1) {
        val start = pointList[i]
        val end = pointList[i + 1]

        // Determine the state of this segment by checking its midpoint
        val midpoint = start.plus(Duration.between(start, end).dividedBy(2))
        val isFasting = mergedIntervals.any { (fastStart, fastEnd) ->
            !midpoint.isBefore(fastStart) && midpoint.isBefore(fastEnd)
        }
        val color = if (isFasting) Color(0xFF3DDC84) else Color.Gray

        val duration = Duration.between(start, end).toMinutes()
        if (duration > 0) {
            segments.add(TimelineSegment(color, duration / totalMinutesInDay))
        }
    }

    // 4. Coalesce adjacent segments of the same color
    val coalesced = mutableListOf<TimelineSegment>()
    if (segments.isNotEmpty()) {
        var current = segments.first()
        for (i in 1 until segments.size) {
            val next = segments[i]
            if (next.color == current.color) {
                current = current.copy(weight = current.weight + next.weight)
            } else {
                coalesced.add(current)
                current = next
            }
        }
        coalesced.add(current)
    }

    return coalesced.ifEmpty { listOf(TimelineSegment(Color.Gray, 1f)) }
}
