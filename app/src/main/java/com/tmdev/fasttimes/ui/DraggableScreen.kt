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
package com.tmdev.fasttimes.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class DragAnchors {
    Dashboard,
    History,
}

@OptIn(ExperimentalFoundationApi::class)
class DraggableScreenState(
    internal val state: AnchoredDraggableState<DragAnchors>,
    private val scope: CoroutineScope
) {
    fun openHistory() {
        scope.launch {
            state.animateTo(DragAnchors.History)
        }
    }

    fun closeHistory() {
        scope.launch {
            state.animateTo(DragAnchors.Dashboard)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun rememberDraggableScreenState(): DraggableScreenState {
    // The state constructor is now very simple.
    val state = remember {
        AnchoredDraggableState(
            initialValue = DragAnchors.Dashboard
        )
    }
    val scope = rememberCoroutineScope()
    return remember { DraggableScreenState(state, scope) }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DraggableScreen(
    modifier: Modifier = Modifier,
    state: DraggableScreenState,
    dashboardContent: @Composable () -> Unit,
    historyContent: @Composable () -> Unit,
) {
    val density = LocalDensity.current

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val anchors = DraggableAnchors {
            DragAnchors.Dashboard at 0f
            DragAnchors.History at -maxWidthPx
        }
        state.state.updateAnchors(anchors)

        val flingBehavior = AnchoredDraggableDefaults.flingBehavior(
            state = state.state,
            positionalThreshold = { totalDistance -> totalDistance * 0.5f },
        )

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            historyContent()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset {
                        IntOffset(
                            x = state.state
                                .requireOffset()
                                .roundToInt(),
                            y = 0,
                        )
                    }
                    // The modifier now takes the state and the new flingBehavior.
                    .anchoredDraggable(
                        state = state.state,
                        orientation = Orientation.Horizontal,
                        flingBehavior = flingBehavior
                    )
            ) {
                dashboardContent()
            }
        }
    }
}
