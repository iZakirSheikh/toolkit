/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by 2024 on 19-09-2024.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.primex.core.shapes

import androidx.annotation.FloatRange
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

/**
 * A value class representing a squircle shape.
 *
 * A squircle is a mathematical shape that combines aspects of a square and a circle.
 * This implementation uses Bézier curves to create a smooth, rounded shape with adjustable curvature.
 *
 * @param radius The radius of the corners, as a fraction of the minimum dimension of the shape.
 * Should be a value between 0.0 (square) and 1.0 (approaches a circle).
 */
@JvmInline
value class SquircleShape(
    @FloatRange(0.0, 1.0) private val radius: Float
): Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        if (radius == 0.0f) return Outline.Rectangle(size.toRect())
        // TODO - Maybe return circle for 1.0f
        val cornerSmoothing: Float = 0.55f
        // The curvature must not exceed the half of minDimension of the shape
        val radius =  (radius * (size.minDimension / 2))
        // Extract the shape width & height
        val (width, height) = size
        // return the shape
        return Outline.Generic(
            Path().apply {
                // Set the starting point at the coordinate of (x = topLeft corner, y = 0).
                moveTo(
                    x = radius,
                    y = 0f
                )

                // Draw a Line to the coordinate of (x = the width - the top right corner).
                lineTo(
                    x = width - radius,
                    y = 0f
                )

                // Draw a Cubic from the coordinate of (x1 = the width - the top right corner * (1 - the corner smoothing), y1 = 0)
                // with a mid point at the coordinate of (x2 = the width, y2 = the top right corner * (1 - the corner smoothing))
                // to the end point at the coordinate of (x3 = the width, y3 = the top right corner).
                cubicTo(
                    x1 = width - radius * (1 - cornerSmoothing),
                    y1 = 0f,
                    x2 = width,
                    y2 = radius * (1 - cornerSmoothing),
                    x3 = width,
                    y3 = radius
                )

                // Draw a Line to the coordinate of (x = the width, y = the height - the bottom right corner).
                lineTo(
                    x = width,
                    y = height - radius
                )

                // Draw a Cubic from the coordinate of (x1 = the width, y1 = the height - the bottom right corner * (1 - the corner smoothing))
                // with a mid point at the coordinate of (x2 = the width - the bottom right corner * (1 - the corner smoothing), y2 = the height)
                // to the end point at the coordinate of (x3 = the width - the bottom right corner, y3 = the height).
                cubicTo(
                    x1 = width,
                    y1 = height - radius * (1 - cornerSmoothing),
                    x2 = width - radius * (1 - cornerSmoothing),
                    y2 = height,
                    x3 = width - radius,
                    y3 = height
                )

                // Draw a Line to the coordinate of (x = the bottom left corner, y = the height).
                lineTo(
                    x = radius,
                    y = height
                )

                // Draw a Cubic from the coordinate of (x1 = the bottom left corner * (1 - the corner smoothing), y1 = the height)
                // with a mid point at the coordinate of (x2 = 0, y2 = the height - the bottom left corner * (1 - the corner smoothing))
                // to the end point at the coordinate of (x3 = 0, y3 = the height - the bottom left corner).
                cubicTo(
                    x1 = radius * (1 - cornerSmoothing),
                    y1 = height,
                    x2 = 0f,
                    y2 = height - radius * (1 - cornerSmoothing),
                    x3 = 0f,
                    y3 = height - radius
                )

                // Draw a Line to the coordinate of (x = 0, y = the top left corner).
                lineTo(
                    x = 0f,
                    y = radius
                )

                // Draw a Cubic from the coordinate of (x1 = 0, y1 = the top left corner * (1 - the corner smoothing))
                // with a mid point at the coordinate of (x2 = the top left corner * (1 - the corner smoothing), y2 = 0)
                // to the end point at the coordinate of (x3 = the top left corner, y3 = 0.
                cubicTo(
                    x1 = 0f,
                    y1 = radius * (1 - cornerSmoothing),
                    x2 = radius * (1 - cornerSmoothing),
                    y2 = 0f,
                    x3 = radius,
                    y3 = 0f
                )

                // Close the [Path].
                close()
            }
        )
    }
}