/*
 * Copyright 2025 sheik
 *
 * Created by sheik on 02-05-2025.
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

package com.zs.compose.theme.adaptive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.zs.compose.foundation.thenIf
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.LocalContentColor
import com.zs.compose.theme.None
import com.zs.compose.theme.contentColorFor
import com.zs.compose.theme.internal.Slot
import androidx.compose.runtime.CompositionLocalProvider as Provider

private const val INDEX_CONTENT = 0
private const val INDEX_TOP_BAR = 1
private const val INDEX_FAB = 2

// FAB spacing above the bottom bar / bottom of the Scaffold
private val FabSpacing = 16.dp

/**
 * A composable that implements the basic material design layout structure.
 *
 * The `Scaffold` provides slots for a [topBar], [floatingActionButton], and a content area.
 *
 * @param modifier The [Modifier] to be applied to the `Scaffold`.
 * @param topBar The composable to be placed at the top of the `Scaffold`.
 * @param floatingActionButton The composable to be used as the floating action button.
 * @param fabPosition The position of the [floatingActionButton]. See [FabPosition].
 * @param containerColor The background color of the `Scaffold`.
 * @param contentColor The preferred color for content inside the `Scaffold`.
 *   It will use [contentColorFor] by default.
 * @param content The main content of the `Scaffold`.
 *
 * Example usage:
 *
 * @see Scaffold
 */
@Composable
fun Scaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    fabPosition: FabPosition = FabPosition.End,
    containerColor: Color = AppTheme.colors.background,
    contentColor: Color = contentColorFor(containerColor),
    content: @Composable () -> Unit
) {
    // The indent propagated through window.contentIndent
    // The removes its old value; which means child has access to only topBar indent.
    val (indent, onIndentUpdated) =
        remember { mutableStateOf(WindowInsets.None) }

    //
    Layout(
        modifier = modifier
            .thenIf(containerColor != Color.Unspecified || containerColor != Color.Transparent) {
                background(
                    containerColor
                )
            }
            .fillMaxSize(),
        content = {
            // Content (index 0)
            Slot {
                Provider(
                    LocalContentColor provides contentColor,
                    LocalContentInsets provides indent,
                    content = content
                )
            }
            // Top Bar (index 1)
            Slot(topBar)
            // Floating Action Button (index 2)
            Slot(floatingActionButton)
        },
        measurePolicy = remember(fabPosition) {
            ScaffoldMeasurePolicy(
                fabPosition,
                onIndentUpdated
            )
        }
    )
}

private data class ScaffoldMeasurePolicy(
    private val fabPosition: FabPosition,
    private val onUpdateIntent: (WindowInsets) -> Unit
) : MeasurePolicy {
    override fun MeasureScope.measure(
        measurables: List<Measurable>,
        c: Constraints
    ): MeasureResult {
        val width = c.maxWidth;
        val height = c.maxHeight
        // measure content with original coordinates.
        // Loose constraints for initial measurements
        val contentPlaceable = measurables[INDEX_CONTENT].measure(c)
        val constraints = c.copy(0, minHeight = 0)
        val topBarPlaceable = measurables[INDEX_TOP_BAR].measure(constraints)
        val fabPlaceable = measurables[INDEX_FAB].measure(constraints)
        // Update content insets (padding) based on the height of the top bar.
        // This ensures content doesn't overlap with the top bar.
        // include fab as well
        onUpdateIntent(WindowInsets(top = topBarPlaceable.height.toDp(), bottom = fabPlaceable.height.toDp()))
        // Layout the measured components within the Scaffold.
        return layout(width, height) {
            // place the content at top
            contentPlaceable.placeRelative(0, 0)
            // place topBar at top the content
            topBarPlaceable.placeRelative(0, 0)
            // place fab according to fabPosition.
            val fabSpacingPx = FabSpacing.roundToPx()
            fabPlaceable.placeRelative(
                // Calculate the vertical position of the FAB, placing it above the bottom edge.
                y = contentPlaceable.height - fabPlaceable.height - fabSpacingPx / 2,
                // Calculate the horizontal position based on the fabPosition.
                x = when (fabPosition) {
                    FabPosition.End -> contentPlaceable.width - fabPlaceable.width - fabSpacingPx
                    FabPosition.Center -> (contentPlaceable.width - fabPlaceable.width) / 2
                    FabPosition.Start -> fabSpacingPx
                    else -> error("Invalid fab position specified.")
                }
            )
        }
    }
}