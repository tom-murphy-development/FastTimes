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
package com.fasttimes.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import kotlin.math.abs

/**
 * An expressive summary card with a custom [Shape].
 * Content is centered and sized to fit comfortably with flexible sizing.
 */
@Composable
fun ExpressiveStatCard(
    label: String,
    value: String,
    unit: String,
    containerColor: Color,
    contentColor: Color,
    shape: Shape,
    modifier: Modifier = Modifier,
    height: Dp = 240.dp
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label, 
                style = MaterialTheme.typography.labelLarge,
                color = contentColor.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value, 
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 30.sp,
                    lineHeight = 26.sp
                ),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = unit, 
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                color = contentColor.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 12.sp,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Creates a [Shape] from a [RoundedPolygon], scaled uniformly to 95% and centered.
 */
@ExperimentalMaterial3ExpressiveApi
@Composable
fun rememberPolygonShape(polygon: RoundedPolygon): Shape {
    return remember(polygon) {
        object : Shape {
            override fun createOutline(
                size: Size,
                layoutDirection: LayoutDirection,
                density: Density
            ): Outline {
                val path = polygon.toPath().asComposePath()
                val bounds = path.getBounds()
                val matrix = Matrix()
                
                // Use uniform scaling to 95% of original size to prevent stretching
                val scale = minOf(size.width / bounds.width, size.height / bounds.height) * 0.95f

                // Center and scale to fit destination box
                matrix.translate(size.width / 2f, size.height / 2f)
                matrix.scale(scale, scale)
                matrix.translate(-(bounds.left + bounds.width / 2f), -(bounds.top + bounds.height / 2f))
                
                path.transform(matrix)
                return Outline.Generic(path)
            }
        }
    }
}

/**
 * Returns a random expressive shape from the [MaterialShapes] collection.
 */
@ExperimentalMaterial3ExpressiveApi
@Composable
fun rememberRandomExpressiveShape(seed: Int): Shape {
    val shapes = remember {
        listOf(
            MaterialShapes.Pentagon,
            MaterialShapes.Clover8Leaf,
            MaterialShapes.Sunny,
            MaterialShapes.Cookie9Sided,
            MaterialShapes.Ghostish,
            MaterialShapes.Slanted,
            MaterialShapes.Gem,
            MaterialShapes.Clover4Leaf
        )
    }
    // Using abs to handle potential negative seed results
    val polygon = shapes[abs(seed) % shapes.size]
    return rememberPolygonShape(polygon)
}
