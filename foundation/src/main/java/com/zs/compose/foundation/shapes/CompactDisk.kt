/*
 * Copyright 2025 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 10-02-2025.
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

package com.zs.compose.foundation.shapes

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

/**  The shape of a compact disk. */
val CompactDisk =
    object : Shape {
        override fun createOutline(
            size: Size,
            layoutDirection: LayoutDirection,
            density: Density
        ): Outline {
            val innerRadiusFraction = 0.259f
            val radius = kotlin.math.min(size.width, size.height) / 2f // the outer radius
            val innerRadius = radius * innerRadiusFraction // the inner radius
            val center = Offset(size.width / 2f, size.height / 2f) // the center of the shape

            // create a path for the outer circle
            val outerPath = Path().apply {
                addArc(
                    Rect(
                        left = center.x - radius,
                        top = center.y - radius,
                        right = center.x + radius,
                        bottom = center.y + radius,
                    ),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 360f
                )
            }

            // create a path for the inner circle
            val innerPath = Path().apply {
                addArc(
                    Rect(
                        left = center.x - innerRadius,
                        top = center.y - innerRadius,
                        right = center.x + innerRadius,
                        bottom = center.y + innerRadius,
                    ),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 360f
                )
            }

            // subtract the inner path from the outer path using PathOperation.Difference
            val resultPath = Path().apply {
                op(outerPath, innerPath, PathOperation.Difference)
            }

            // return an outline based on the result path
            return Outline.Generic(resultPath)
        }
    }