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
package com.tmdev.fasttimes.ui.history

import com.tmdev.fasttimes.data.fast.Fast
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

fun generateTimelineSegments(date: LocalDate, fasts: List<Fast>): List<TimelineSegment> {
    val dayStart = ZonedDateTime.of(date, LocalTime.MIN, ZoneId.systemDefault())
    val dayEnd = dayStart.plusDays(1)
    val totalMinutesInDay = 1440f

    val relevantFasts = fasts.filter {
        val fastEnd = it.end ?: ZonedDateTime.now()
        it.start.isBefore(dayEnd) && fastEnd.isAfter(dayStart)
    }

    if (relevantFasts.isEmpty()) {
        return listOf(TimelineSegment(TimelineSegmentType.NonFasting, 1f))
    }

    val sortedFasts = relevantFasts.sortedBy { it.start }

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

    val segments = mutableListOf<TimelineSegment>()
    val pointList = eventPoints.toList()

    for (i in 0 until pointList.size - 1) {
        val start = pointList[i]
        val end = pointList[i + 1]

        val midpoint = start.plus(Duration.between(start, end).dividedBy(2))
        val isFasting = mergedIntervals.any { (fastStart, fastEnd) ->
            !midpoint.isBefore(fastStart) && midpoint.isBefore(fastEnd)
        }
        val type = if (isFasting) TimelineSegmentType.Fasting else TimelineSegmentType.NonFasting

        val duration = Duration.between(start, end).toMinutes()
        if (duration > 0) {
            segments.add(TimelineSegment(type, duration / totalMinutesInDay))
        }
    }

    val coalesced = mutableListOf<TimelineSegment>()
    if (segments.isNotEmpty()) {
        var current = segments.first()
        for (i in 1 until segments.size) {
            val next = segments[i]
            if (next.type == current.type) {
                current = current.copy(weight = current.weight + next.weight)
            } else {
                coalesced.add(current)
                current = next
            }
        }
        coalesced.add(current)
    }

    return coalesced.ifEmpty { listOf(TimelineSegment(TimelineSegmentType.NonFasting, 1f)) }
}
