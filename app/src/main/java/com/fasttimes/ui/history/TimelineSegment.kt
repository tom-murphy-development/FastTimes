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

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * A data class representing a single colored segment of the timeline.
 *
 * @param color The color of the segment.
 * @param weight The proportion of the total width this segment should occupy.
 */
@Immutable
data class TimelineSegment(
    val color: Color,
    val weight: Float
)
