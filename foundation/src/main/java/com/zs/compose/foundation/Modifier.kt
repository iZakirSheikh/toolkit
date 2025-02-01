/*
 * Copyright 2025 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 26-01-2025.
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

package com.zs.compose.foundation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.NonSkippableComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalLayoutDirection

private const val TAG = "Modifier"

/**
 * Conditionally applies another [Modifier] if the given [condition] is true.
 *
 * @param condition The condition to evaluate.
 * @param other The [Modifier] to apply if the condition is true.
 * @return This [Modifier] if the condition is false, otherwise this [Modifier] combined with [other].
 */
inline fun Modifier.thenIf(condition: Boolean, other: Modifier.() -> Modifier) =
    if (condition) this then Modifier.other() else this

/**
 * Rotates/transforms the composable by 90 degrees in the clockwise or anti-clockwise direction.
 *
 * @param clockwise `true` if the composable should be rotated clockwise, `false` if anti-clockwise.
 * @return A modified [Modifier] with the rotation applied.
 *
 * Usage example:
 * ```
 * Box(
 *     modifier = Modifier
 *         .fillMaxSize()
 *         .rotate(clockwise = true)
 * ) {
 *     // content here
 * }
 * ```
 *
 * @since 1.0.0
 * @author Zakir Sheikh
 */
@ExperimentalFoundationApi
fun Modifier.rotateTransform(
    clockwise: Boolean
): Modifier {
    val transform = Modifier.layout { measurable, constraints ->
        // as rotation is taking place
        // the height becomes so construct new set of construnts from old one.
        val newConstraints = constraints.copy(
            minWidth = constraints.minHeight,
            minHeight = constraints.minWidth,
            maxHeight = constraints.maxWidth,
            maxWidth = constraints.maxHeight
        )

        // measure measurable with new constraints.
        val placeable = measurable.measure(newConstraints)

        layout(placeable.height, placeable.width) {

            //Compute where to place the measurable.
            // TODO needs to rethink these
            val x = -(placeable.width / 2 - placeable.height / 2)
            val y = -(placeable.height / 2 - placeable.width / 2)

            placeable.place(x = x, y = y)
        }
    }

    val rotated = Modifier.rotate(if (clockwise) 90f else -90f)

    // transform and then apply rotation.
    return this
        .then(transform)
        .then(rotated)
}

