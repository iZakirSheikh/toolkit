/*
 * Copyright 2025 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 30-01-2025.
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

package com.zs.compose.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

private val DefaultDividerThickness = 1.dp
private const val DividerAlpha = 0.12f

/**
 * [Material Design divider](https://m3.material.io/components/divider/overview)
 *
 * A divider is a thin line that groups content in lists and layouts.
 *
 * ![Divider image](https://developer.android.com/images/reference/androidx/compose/material3/divider.png)
 *
 * @param modifier the [Modifier] to be applied to this divider line.
 * @param thickness thickness of this divider line. Using [Dp.Hairline] will produce a single pixel
 *   divider regardless of screen density.
 * @param color color of this divider line.
 */
@Composable
fun HorizontalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = DefaultDividerThickness,
    color: Color = AppTheme.colors.onBackground.copy(alpha = DividerAlpha),
) =
    Canvas(modifier.fillMaxWidth().height(thickness)) {
        drawLine(
            color = color,
            strokeWidth = thickness.toPx(),
            start = Offset(0f, thickness.toPx() / 2),
            end = Offset(size.width, thickness.toPx() / 2),
        )
    }

/**
 * [Material Design divider](https://m3.material.io/components/divider/overview)
 *
 * A divider is a thin line that groups content in lists and layouts.
 *
 * ![Divider image](https://developer.android.com/images/reference/androidx/compose/material3/divider.png)
 *
 * @param modifier the [Modifier] to be applied to this divider line.
 * @param thickness thickness of this divider line. Using [Dp.Hairline] will produce a single pixel
 *   divider regardless of screen density.
 * @param color color of this divider line.
 */
@Composable
fun VerticalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = DefaultDividerThickness,
    color: Color = AppTheme.colors.onBackground.copy(alpha = DividerAlpha),
) =
    Canvas(modifier.fillMaxHeight().width(thickness)) {
        drawLine(
            color = color,
            strokeWidth = thickness.toPx(),
            start = Offset(thickness.toPx() / 2, 0f),
            end = Offset(thickness.toPx() / 2, size.height),
        )
    }

/**
 * Draws a horizontal divider at the bottom of the composable.
 *
 * A divider is a thin line that groups content in lists and layouts.
 *
 * ![Dividers image](https://developer.android.com/images/reference/androidx/compose/material/dividers.png)
 *
 * @param color The color of the divider line.
 * @param thickness The thickness of the divider line. By default, 1 dp is used. Using [Dp.Hairline]
 *                  will produce a single pixel divider regardless of screen density.
 * @param indent The offset of the line from the left and right edges of the composable. No offset is
 *               used by default.
 * @return A modified [Modifier] that draws a horizontal divider at the bottom of the composable.
 *
 * Usage example:
 * ```
 * Column {
 *     // content here
 *     Spacer(modifier = Modifier.height(16.dp))
 *     Text("Section 1", modifier = Modifier.drawHorizontalDivider(Color.Gray))
 *     // more content here
 * }
 * ```
 *
 * @since 1.0.0
 * @see [drawVerticalDivider]
 * @see [Divider]
 * @see [DividerVertical]
 */


fun Modifier.drawHorizontalDivider(
    color: Color,
    thickness: Dp = 1.dp,
    indent: DpOffset = DpOffset.Zero
) = drawWithContent {

    // calculate the respective indents.
    val startIndentPx = indent.x.toPx()
    val endIndentPx = indent.y.toPx()

    // width and height of the composable UI element.
    val (width, height) = size

    // constructs offsets of the divider.
    val start = Offset(
        startIndentPx,
        // top will get added and bottom will get subtracted.
        height
    )

    val end = Offset(
        width - endIndentPx,
        height
    )

    val thicknessPx = thickness.toPx()

    drawContent()
    drawLine(
        color.copy(DividerAlpha),
        strokeWidth = thicknessPx,
        start = start,
        end = end
    )
}

/**
 * Draws vertical [Divider] at the end of the composable
 * @see drawHorizontalDivider
 */
fun Modifier.drawVerticalDivider(
    color: Color,
    thickness: Dp = 1.dp,
    indent: DpOffset = DpOffset.Zero
) = drawWithContent {

    // calculate the respective indents.
    val topIndentPx = indent.x.toPx()
    val bottomIndentPx = indent.y.toPx()

    // width and height of the composable UI element.
    val (width, height) = size

    // constructs offsets of the divider.
    val start = Offset(
        width,

        // top will get added and bottom will get subtracted.
        topIndentPx
    )

    val end = Offset(
        width,
        height - bottomIndentPx
    )

    val thicknessPx = thickness.toPx()

    drawContent()
    drawLine(
        color.copy(DividerAlpha),
        strokeWidth = thicknessPx,
        start = start,
        end = end
    )
}
