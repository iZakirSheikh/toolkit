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

package com.zs.compose.theme.appbar

import android.util.Log
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.LayoutIdParentData
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.lerp
import com.zs.compose.theme.ExperimentalThemeApi
import kotlin.math.roundToInt

private const val TAG = "CollapsableAppBarLayout"

/**
 * The layout ID for the navigation icon container.
 * @see LAYOUT_ID_BACKGROUND
 */
internal const val APP_BAR_LAYOUT_ID_NAVIGATION_ICON = "layout_nav_icon"

/**
 * The layout ID for the main title container.
 *
 * Used for static titles. For collapsible titles, see [LAYOUT_ID_COLLAPSABLE_TITLE].
 * @see LAYOUT_ID_BACKGROUND
 */
internal const val APP_BAR_LAYOUT_ID_TITLE = "layout_title"

/**
 * The layout ID for the collapsible title container.
 *
 * Use this to create titles that animate or scale based on scroll state.
 * @see LAYOUT_ID_BACKGROUND
 */
internal const val APP_BAR_LAYOUT_ID_COLLAPSABLE_TITLE = "layout_collapsable_title"

/**
 * The layout ID for the actions container.
 * @see LAYOUT_ID_BACKGROUND
 */
internal const val APP_BAR_LAYOUT_ID_ACTIONS = "layout_actions"

/**
 * Constants defining special layout IDs for default layouts supported by [CollapsableAppbarLayout].
 * Use these IDs to create default implementations and customize effects like parallax title scaling.
 *
 * The layout ID for the background container.
 */
internal const val APP_BAR_LAYOUT_ID_BACKGROUND = "layout_background"

// TODO - These are as per material design; should i include these in here.
//  Needs further investigation.
private val ExpandableTitleBottomPadding = 28.dp
private val ExpandableTitleHorizontalPadding = 12.dp

// TODO: Find better way to do this.
private fun WindowInsets.values(scope: MeasureScope): Array<Int>{
    return arrayOf(
        getLeft(scope, scope.layoutDirection),
        getTop(scope),
        getRight(scope, scope.layoutDirection),
        getBottom(scope),
    )
}

/**
 * A helper property that indicates whether this [Placeable] has alignment information within its parent layout.
 *
 * @return `true` if the parent data is an [AppBarParentDataNode] and it contains both `alignment` and `targetAlignment` values,
 *         indicating that specific placement information is available.
 *         Otherwise, returns `false`.
 */
private val Placeable.hasPlacementInfo
    get() = (parentData as? AppBarParentDataNode).let {
        Log.d(TAG, "hasPlacementInfo: ${it.toString()} {} ")
        if (it == null) false else it.alignment != null && it.targetAlignment != null
    }

/**
 * A property that provides access to the layout ID associated with a [Placeable], if available.
 *
 * @return The layout ID, if the parent data is a [LayoutIdParentData] and it contains a layout ID.
 *         Otherwise, returns `null`.
 */
private val Placeable.layoutId
    get() = (parentData as? LayoutIdParentData)?.layoutId

/**
 * Linearly interpolates between two [IntSize] values, creating a new IntSize that represents
 * a weighted combination of the two.
 *
 * @param start The starting IntSize.
 * @param end The ending IntSize.
 * @param fraction A value between 0 and 1 indicating the weight of the interpolation.
 *                 0 results in the starting IntSize, 1 results in the ending IntSize,
 *                 and values in between provide a blend.
 * @return A new IntSize that is the interpolated result.
 */
private fun lerp(start: IntSize, end: IntSize, fraction: Float): IntSize =
    IntSize(lerp(start.width, end.width, fraction), lerp(start.height, end.height, fraction))

/**
 * Places a [Placeable] within a given space, handling alignment, interpolation, and optional parallax effects.
 *
 * @param box The available space for placement.
 * @param targetBox The target space for placement during expansion.
 * @param fraction The fraction of expansion, ranging from 0 (collapsed) to 1 (expanded).
 * @param offset An optional offset to apply to the initial placement.
 * @param targetOffset An optional offset to apply to the final placement.
 *
 * @context(MeasureScope, Placeable.PlacementScope)
 */
private fun Placeable.place(
    scope1: MeasureScope,
    scope2: Placeable.PlacementScope,
    fraction: Float,
    box: IntSize,
    targetBox: IntSize = box,
    offset: IntOffset = IntOffset.Zero,
    targetOffset: IntOffset = offset
) {
    with(scope1){
        with(scope2){
            // Retrieve parent data for alignment and parallax information.
            val data = parentData as? AppBarParentDataNode
            val parallax = data?.parallax ?: Float.NaN

            // Ensure parallax is only applied to the background.
            require(data?.layoutId == APP_BAR_LAYOUT_ID_BACKGROUND || parallax.isNaN()) {
                "Parallax is only supported for the background: $APP_BAR_LAYOUT_ID_BACKGROUND"
            }
            // --- Alignment Determination ---

            // Determine initial and final alignments based on parent data or layoutId defaults.
            val component = IntSize(width, height)
            val iPosition = let {
                val alignment = data?.alignment ?: when (layoutId) {
                    APP_BAR_LAYOUT_ID_TITLE, APP_BAR_LAYOUT_ID_COLLAPSABLE_TITLE -> Alignment.CenterStart
                    else -> Alignment.Center
                }
                alignment.align(component, box, layoutDirection)
            }
            val fPosition = let {
                val alignment = data?.targetAlignment ?: when (layoutId) {
                    APP_BAR_LAYOUT_ID_TITLE -> Alignment.CenterStart
                    APP_BAR_LAYOUT_ID_COLLAPSABLE_TITLE -> Alignment.BottomStart
                    else -> Alignment.Center
                }
                alignment.align(component, targetBox, layoutDirection)
            }
            // Interpolate between initial and final positions based on the expansion fraction.
            val position = lerp(iPosition + offset, fPosition + targetOffset, fraction)
            Log.d(TAG, "onPlace: ID: $layoutId Box: $box, Size: $component, pos: $position Direction: $layoutDirection")
            // No parallax applied, use normal placement
            if (parallax.isNaN()) return placeRelative(position)
            // Apply parallax effect using a layer.
            placeRelativeWithLayer(position) {
                // Apply parallax effect by scaling and translating the element
                val scale = lerp(1f, 1f + parallax, fraction)
                scaleX = scale
                scaleY = scale
                // translationY = lerp(0f, backgroundPlaceable.height * parallax, fraction)
            }
        }
    }
}

/**
 * A composable function that creates a collapsible top bar layout.
 *
 * @param height The height of the top bar when collapsed.
 * @param maxHeight The height of the top bar when expanded.
 * @param modifier The modifier to be applied to the top bar.
 * @param insets The window insets to be applied to the top bar.
 * @param scrollBehavior The scroll behavior to control the collapse and expansion of the top bar.
 * @param content The content of the top bar, which can access the TopAppBarScope.
 */
@ExperimentalThemeApi
@Composable
fun CollapsableTopBarLayout(
    height: Dp,
    maxHeight: Dp,
    insets: WindowInsets,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    content: @Composable @UiComposable TopAppBarScope.() -> Unit
) {
    // Get the top app bar scope from the current composition
    val scope = rememberTopAppBarScope() as TopAppBarState
    // Ensure the max height is valid
    // Throw an exception if the max height is less than the pinned height
    if (maxHeight < height) {
        throw IllegalArgumentException(
            "A TopAppBar max height: $maxHeight should be greater than or equal to its height: $height height"
        )
    }
    // Check if this represents an expandable top bar.
    // An expandable topBar is the one which expands/collapses beyond/until normal height.
    // Compare the max height and the pinned height to determine if the top bar is expandable or not
    val isLargeAppBar = maxHeight > height
    // **SideEffect for Height Offset Adjustment:**
    // **Purpose:**
    // - Controls the app bar's height offset limit to ensure that only the bottom title area
    //   disappears when collapsing, preserving the top title's visibility.
    val density =
        LocalDensity.current // Obtains the device's screen density for scaling calculations.
    SideEffect {
        // **Calculating Height Offset Limit:**
        // - Determines the appropriate height offset limit based on whether the app bar is large:
        //     - For large app bars, subtracts `maxHeight` from `height` to partially hide the bottom.
        //     - For non-large app bars, uses a negative `height` to fully hide it.
        // - Converts the calculated limit to pixels for accurate layout using density.
        val limit = with(density) { (if (isLargeAppBar) height - maxHeight else -height).toPx() }
        // - Assigns the current scroll behavior state to the app bar scope for coordination.
        scrollBehavior?.state = scope
        if (scope.heightOffsetLimit != limit) {
            scope.heightOffsetLimit = limit
        }
    }
    // Set up support for resizing the top app bar when vertically dragging the bar itself.
    // Check if the scroll behavior is not null and not pinned
    val appBarDragModifier = if (scrollBehavior != null && !scrollBehavior.isPinned) {
        val state = scope as TopAppBarState
        Modifier.draggable(
            orientation = Orientation.Vertical,
            state = rememberDraggableState { delta ->
                state.heightOffset = state.heightOffset + delta
            },
            onDragStopped = { velocity ->
                settleAppBar(
                    state,
                    velocity,
                    scrollBehavior.flingAnimationSpec,
                    scrollBehavior.snapAnimationSpec
                )
            }
        )
    } else Modifier
    // Place the content
    Layout(
        content = { scope.content() },
        modifier = modifier.then(appBarDragModifier),
        measurePolicy = { measurables, incomming ->
            // Calculate the static height in pixels of the suggested height
            val topBarHeight = height.roundToPx()
            val topBarMaxHeight = maxHeight.roundToPx()
            // Dependencies
            val fraction = scope.fraction
            val (start, top, end, bottom) = insets.values(this)
            // **Calculate Dynamic Top Bar Height & create Modified Constraints:**
            // Copy the incoming constraints and adjust maxHeight:
            // Determine the base height for collapsed state based on whether it's a large app bar:
            // Interpolate between baseHeight and topBarMaxHeight based on the expansion fraction.
            val height = lerp(
                if (isLargeAppBar) topBarHeight else 0,
                topBarMaxHeight,
                fraction
            ) + top + bottom
            val constraints = incomming.copy(maxHeight = height)
            val width = constraints.maxWidth

            // Ensure that in the 2nd slot, the title has maximum space allocated.
            var titleSlotMaxWidth = width - (start + end)
            val placeables = measurables.let { list ->
                // Ensure that the title is placed last in the layout.
                // Find the index of the title within the measurables list.
                val index = list.indexOfFirst {
                    it.layoutId == APP_BAR_LAYOUT_ID_TITLE || it.layoutId == APP_BAR_LAYOUT_ID_COLLAPSABLE_TITLE
                }
                // If the title is already last or not found, return the original list.
                if (index == -1 || index == list.size - 1) return@let list
                // Move the title to the end of the list.
                val mut = list.toMutableList()
                val removed = mut.removeAt(index)
                mut += removed
                mut
            }
                .map { measurable ->
                    when (measurable.layoutId) {
                        // Background is not insetted; it uses the full width of topBar.
                        // Measure the background with constraints ensuring it doesn't exceed the layout height.
                        APP_BAR_LAYOUT_ID_BACKGROUND -> measurable.measure(
                            constraints.copy(
                                maxHeight = height
                            )
                        )
                        // Navigation icon and actions are always measured with respect to the small topBar.
                        APP_BAR_LAYOUT_ID_NAVIGATION_ICON, APP_BAR_LAYOUT_ID_ACTIONS -> {
                            // Measure with constraints allowing for their natural width/height.
                            // Reduce the available width for the title slot based on their widths.
                            measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
                                .also { titleSlotMaxWidth -= it.width }
                        }
                        // Title and collapsible title are measured differently based on app bar size and expansion.
                        APP_BAR_LAYOUT_ID_COLLAPSABLE_TITLE, APP_BAR_LAYOUT_ID_TITLE -> {
                            // Calculate the maximum available width for the title in expanded app bar (70% of width).
                            val expandedAvailableWidth = width - (width * 0.3f).roundToInt()
                            // Determine the maximum width for the title based on app bar size and expansion state.
                            val maxWidth = when {
                                // In small app bar or for the regular title, use the available width.
                                !isLargeAppBar || measurable.layoutId == APP_BAR_LAYOUT_ID_TITLE -> titleSlotMaxWidth
                                // In large app bar for collapsible title, interpolate between available
                                // widths based on expansion fraction.
                                else -> lerp(titleSlotMaxWidth, expandedAvailableWidth, fraction)
                            }.coerceAtLeast(0)
                            // Measure the title with calculated constraints.
                            measurable.measure(constraints.copy(0, maxWidth, 0))
                        }
                        // Other measurables are measured with default constraints.
                        else -> measurable.measure(
                            constraints.copy(
                                0,
                                width - (start + end),
                                0,
                                height - (top + bottom)
                            )
                        )
                    }
                }
            layout(width, height) {
                val topStartOffset = IntOffset(start, top)
                val fullAvailableSpace = IntSize(width - start - end, height - top - bottom)
                var navigationWidth = 0
                placeables.forEach { placeable ->
                    when (placeable.layoutId) {
                        APP_BAR_LAYOUT_ID_BACKGROUND -> placeable.place(
                            this@Layout, this,
                            fraction,
                            IntSize(width, height)
                        )

                        APP_BAR_LAYOUT_ID_NAVIGATION_ICON -> {
                            val box = IntSize(placeable.width, topBarHeight)
                            val finalBox =
                                if (!placeable.hasPlacementInfo) box else fullAvailableSpace
                            navigationWidth = placeable.width
                            placeable.place(this@Layout, this,fraction, box, finalBox, topStartOffset)
                        }

                        APP_BAR_LAYOUT_ID_ACTIONS -> {
                            val box = IntSize(placeable.width, topBarHeight)
                            val finalBox =
                                if (!placeable.hasPlacementInfo) box else fullAvailableSpace
                            val offset = IntOffset(width - (end + placeable.width), top)
                            val finalOffset =
                                if (!placeable.hasPlacementInfo) offset else topStartOffset
                            // The offset actions is the same as the third slot offset if the actions do not have placement info
                            // Else, it is the linear interpolation of the third slot offset and the top start offset
                            // Else, the offset animates from top-start to third slot offset
                            placeable.place(this@Layout, this,fraction, box, finalBox, offset, finalOffset)
                        }

                        APP_BAR_LAYOUT_ID_TITLE, APP_BAR_LAYOUT_ID_COLLAPSABLE_TITLE -> {
                            val box = IntSize(titleSlotMaxWidth, topBarHeight)
                            val collapsable =
                                placeable.layoutId == APP_BAR_LAYOUT_ID_COLLAPSABLE_TITLE
                            val finalBox =
                                if (!collapsable || !isLargeAppBar) box else fullAvailableSpace
                            val offset = IntOffset((navigationWidth + start), top)
                            val finalOffset = when {
                                !collapsable || !isLargeAppBar -> offset
                                placeable.hasPlacementInfo -> topStartOffset
                                // padded offset
                                // TODO- here ltr/rtl needs to be investigated.
                                else -> IntOffset(
                                    ExpandableTitleHorizontalPadding.roundToPx() + start,
                                    -ExpandableTitleBottomPadding.roundToPx() + top
                                )
                            }
                            placeable.place(this@Layout, this,fraction, box, finalBox, offset, finalOffset)
                        }

                        else -> placeable.place(
                            this@Layout, this,
                            fraction,
                            fullAvailableSpace,
                            offset = topStartOffset
                        )
                    }
                }
            }
        }
    )
}
