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

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.abs

fun Modifier.horizontalSwipe(
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    enabled: Boolean = true
): Modifier = composed {
    if (!enabled) return@composed this

    var totalHorizontalDragOffset by remember { mutableStateOf(0f) }

    pointerInput(Unit) {
        detectHorizontalDragGestures(
            onHorizontalDrag = { change, dragAmount ->
                change.consume()
                totalHorizontalDragOffset += dragAmount
            },
            onDragStart = {
                totalHorizontalDragOffset = 0f
            },
            onDragEnd = {
                if (abs(totalHorizontalDragOffset) > 100) {
                    if (totalHorizontalDragOffset > 0) {
                        onSwipeRight()
                    } else {
                        onSwipeLeft()
                    }
                }
            }
        )
    }
}
