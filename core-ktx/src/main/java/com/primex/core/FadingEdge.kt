/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 04-12-2024.
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

package com.primex.core

import android.util.Log
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.layer.CompositingStrategy
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalGraphicsContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private const val TAG = "FadingEdge"

private val DEFAULT_FADING_EDGE_LENGTH = 10.dp

/**
 * Calculates the length of the fading edge for the start/end or top/bottom in pixels.
 *
 * @param lengthPx The maximum length of the fading edge in pixels.
 * @return The lengths of the start/end or top/bottom fading edges as an [Offset]. The [Offset] class is used here because it is a value class that holds floats.
 */
private fun ScrollableState.calculateFadingEdgeLengthPx(lengthPx: Float): Offset {
    return when (this) {
        //
        is ScrollState -> {
            // Calculate the fraction value between 0 and 1.
            // Since the value moves from 0 to max (i.e., the size of the list in pixels),
            // we need to animate for the first lengthPx pixels; for the rest, it is fully visible.
            val topFadingLength = if (value > lengthPx) 1f else (value / lengthPx).coerceAtMost(1f)

            // Invert the fraction for the last item.
            // Animate for the last lengthPx pixels but inverted.
            val bottomFadingLength = ((maxValue - value) / lengthPx).coerceAtMost(1f)
            return Offset(topFadingLength * lengthPx, bottomFadingLength * lengthPx)
        }
        // lazy list state
        is LazyListState -> {
            val layoutInfo = layoutInfo
            // Get the layout info of the LazyList.
            // Determine the effective length for the top fading edge.
            // If the first visible item is not at index 0 (not at the top),
            // then the full fading edge length is used. Otherwise, the length is
            // adjusted based on the scroll fraction.
            val topFadingLength =
                if (firstVisibleItemIndex != 0) lengthPx else lengthPx * (firstVisibleItemScrollOffset / lengthPx).coerceAtMost(1f)

            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
            // Determine the effective length for the bottom fading edge.
            // If the last item is not visible, the full fading edge length is used.
            // Otherwise, the length is adjusted based on how much of the last item is visible.
            val bottomFadingLength =
                if (lastVisibleItem == null || lastVisibleItem.index < layoutInfo.totalItemsCount - 1) lengthPx else lengthPx * run {
                    val pastVisiblePortion = (lastVisibleItem.offset + lastVisibleItem.size) - layoutInfo.viewportEndOffset
                    // Calculate the visible portion of the last item.
                    // Calculate the fraction of the last item that is invisible
                    // and use it to adjust the bottom fading edge length.
                    (pastVisiblePortion / lengthPx).coerceAtMost(1f)
                }

            // Return the lengths as an Offset.
            Offset(topFadingLength, bottomFadingLength)
        }
        // TODO - Add in future
        else -> error("The $this is not supported.")
    }
}

private class FadingEdgeElement(
    val state: ScrollableState,
    val horizontal: Boolean,
    val length: Dp,
): ModifierNodeElement<FadingEdgeNode>() {
    override fun create(): FadingEdgeNode = FadingEdgeNode(state, horizontal, length)
    override fun update(node: FadingEdgeNode) {
        node.state = state
        node.horizontal = horizontal
        node.length = length
    }
    override fun InspectorInfo.inspectableProperties() {
        name = "fadingEdge"
        properties["state"] = state
        properties["horizontal"] = horizontal
        properties["length"] = length
    }
    override fun hashCode(): Int {
        var result = state.hashCode()
        result = 31 * result + horizontal.hashCode()
        result = 31 * result + length.hashCode()
        return result
    }
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        val otherModifier = other as? FadingEdgeElement ?: return false
        return state == otherModifier.state &&
                horizontal == otherModifier.horizontal &&
                length == otherModifier.length
    }

    override fun toString(): String {
        return "FadingEdgeElement(state=$state, horizontal=$horizontal, length=$length)"
    }
}

/**
 * Applies a fading edge effect to the content of a scrollable composable.
 *
 * The fading edge is a visual effect that gradually fades out the content at the edges
 * of a scrollable container, providing a visual cue that there is more content to scroll.
 *
 * @param state The [ScrollableState] of the scrollable composable.
 * @param horizontal Whether the fading edge should be applied horizontally (along the x-axis)
 * or vertically (along the y-axis).
 * @param length The length of the fading edge in Dp. Defaults to [DEFAULT_FADING_EDGE_LENGTH].
 *
 * @return A [Modifier] that applies the fading edge effect.
 */
fun Modifier.fadingEdge(
    state: ScrollableState,
    horizontal: Boolean,
    length: Dp = DEFAULT_FADING_EDGE_LENGTH
) = this then FadingEdgeElement(state, horizontal, length)

private class FadingEdgeNode(
    var state: ScrollableState,
    var horizontal: Boolean,
    var length: Dp,
): Modifier.Node(), DrawModifierNode, CompositionLocalConsumerModifierNode {

    private val fadeColorsStart = listOf(Color.Transparent, Color.Black)
    private val fadeColorsEnd = listOf(Color.Black, Color.Transparent)
    private lateinit var layer: GraphicsLayer

    override fun onAttach() {
        super.onAttach()
        layer = currentValueOf(LocalGraphicsContext).createGraphicsLayer()
        layer.compositingStrategy = CompositingStrategy.Offscreen
    }

    override fun onDetach() {
        super.onDetach()
        currentValueOf(LocalGraphicsContext).releaseGraphicsLayer(layer)
    }

    override fun ContentDrawScope.draw() {

        // Use a layer to draw the fading edge on top of the content.
        layer.record {
            this@draw.drawContent()
            // Calculate the actual fading edge lengths based on the scrollable state.
            val lengthPx = length.toPx()
            val (first, last) = state.calculateFadingEdgeLengthPx(lengthPx)
            val (width, height) = size
            // Apply the fading edge based on the orientation (horizontal or vertical).
            when{
                horizontal -> {
                    // start
                    // Log.d(TAG, "draw: startX: ${first - lengthPx} endX: ${first}")
                    // Draw the start fading edge (left side).
                    drawRect(
                        Brush.horizontalGradient(
                            fadeColorsStart,
                            startX = first - lengthPx,
                            endX = first
                        ),
                        topLeft = Offset(0f, 0f),
                        size = Size( lengthPx, height),
                        blendMode = BlendMode.DstIn
                    )

                    // end
                    Log.d(TAG, "draw: width: $width startX: ${width - last} endX: ${width + lengthPx *(1 - (last / lengthPx))}")
                    drawRect(
                        Brush.horizontalGradient(
                            fadeColorsEnd,
                            startX = width - last,
                            endX = width + lengthPx * (1 - (last / lengthPx))
                        ),
                        topLeft = Offset(width - lengthPx, 0f),
                        size = Size( lengthPx, height),
                        blendMode = BlendMode.DstIn
                    )
                }
                else -> {
                    // start
                    Log.d(TAG, "draw: startX: ${first - lengthPx} endX: ${first}")
                    drawRect(
                        Brush.verticalGradient(
                            fadeColorsStart,
                            startY = first - lengthPx,
                            endY = first
                        ),
                        topLeft = Offset(0f, 0f),
                        size = Size( width, lengthPx),
                        blendMode = BlendMode.DstIn
                    )

                    // end
                   // Log.d(TAG, "draw: width: $height startX: ${height - last} endX: ${height + lengthPx *(1 - (last / lengthPx))}")
                    drawRect(
                        Brush.verticalGradient(
                            fadeColorsEnd,
                            startY = height - last,
                            endY = height + lengthPx * (1 - (last / lengthPx))
                        ),
                        topLeft = Offset(0f, height - lengthPx),
                        size = Size( width, lengthPx),
                        blendMode = BlendMode.DstIn
                    )
                }
            }
        }
        drawLayer(layer)
    }
}
