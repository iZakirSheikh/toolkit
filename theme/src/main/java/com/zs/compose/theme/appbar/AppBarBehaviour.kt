// Bismillah Hir Rahman Nir Rahim

/*
 * Copyright 2024 sheik
 *
 * Created by sheik on 05-02-2024.
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

import androidx.annotation.FloatRange
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutIdParentData
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ParentDataModifierNode
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Density
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.animateTo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import com.zs.compose.theme.ExperimentalThemeApi
import kotlin.math.abs

private const val TAG = "CollapsableAppBarBehaviour"

/**
 * Manages parent data for [AppBar] components, ensuring correct alignment, parallax effects, and layout ID propagation.
 *
 * @constructor Creates an [AppBarParentDataNode] with optional initial values for alignment, parallax, and expanded alignment.
 *
 * @property alignment The initial alignment of the AppBar content.
 * @property parallax The parallax factor for applying a parallax effect (NaN if not used).
 * @property targetAlignment The target alignment when the AppBar is expanded.
 */
internal class AppBarParentDataNode(
    var alignment: Alignment? = null,
    var parallax: Float = Float.NaN,
    var targetAlignment: Alignment? = null,
) : ParentDataModifierNode, Modifier.Node(), LayoutIdParentData {

    /**
     * The layout ID associated with this AppBar component.
     */
    override var layoutId: Any = Unit

    /**
     * Combines this AppBarParentDataNode with existing parent data, preserving layout ID and updating alignment, parallax, and expanded alignment values.
     *
     * @param parentData The existing parent data to merge with.
     * @return The combined AppBarParentDataNode with updated values.
     */
    override fun Density.modifyParentData(parentData: Any?): Any {
        val id = (parentData as? LayoutIdParentData)?.layoutId ?: Unit
        return (parentData as? AppBarParentDataNode ?: AppBarParentDataNode()).also {
            it.alignment = alignment ?: it.alignment // Use this node's alignment if set
            it.targetAlignment = targetAlignment ?: it.targetAlignment
            it.parallax = if (!parallax.isNaN()) parallax else it.parallax
            it.layoutId = id
        }
    }

    override fun toString(): String {
        return "AppBarParentDataNode(alignment=$alignment, parallax=$parallax, targetAlignment=$targetAlignment, layoutId=$layoutId)"
    }
}

/**
 * Represents a modifiable element for [AppBarParentDataNode], used during layout composition.
 *
 * @constructor Creates an [AppBarParentDataElement] with optional initial values for alignment, expanded alignment, parallax, and inspector functions.
 *
 * @property alignment The initial alignment of the AppBar content.
 * @property targetAlignment The target alignment when the AppBar is expanded.
 * @property parallax The parallax factor for applying a parallax effect (NaN if not used).
 * @property inspector A function providing additional inspection capabilities for this element.
 */
private class AppBarParentDataElement(
    val alignment: Alignment? = null,
    val targetAlignment: Alignment? = null,
    val parallax: Float = Float.NaN,
    val inspector: InspectorInfo.() -> Unit,
) : ModifierNodeElement<AppBarParentDataNode>() {
    override fun create() = AppBarParentDataNode(alignment, parallax, targetAlignment)
    override fun InspectorInfo.inspectableProperties() {
        inspector()
    }

    override fun update(node: AppBarParentDataNode) {
        if (!parallax.isNaN()) node.parallax = parallax
        if (alignment != null) node.alignment = alignment
        if (targetAlignment != null) node.targetAlignment = targetAlignment
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AppBarParentDataElement

        if (alignment != other.alignment) return false
        if (targetAlignment != other.targetAlignment) return false
        if (parallax != other.parallax) return false
        return inspector == other.inspector
    }

    override fun hashCode(): Int {
        var result = alignment?.hashCode() ?: 0
        result = 31 * result + (targetAlignment?.hashCode() ?: 0)
        result = 31 * result + parallax.hashCode()
        result = 31 * result + inspector.hashCode()
        return result
    }
}

/**
 * Provides a scope for customizing the layout and behavior of elements within a TopAppBar.
 *
 * @property fraction A value between 0 and 1 representing the AppBar's expansion state:
 *      - 0 indicates a fully collapsed state.
 *      - 1 indicates a fully expanded state.
 *      - Values between 0 and 1 represent intermediate states.
 */
interface TopAppBarScope {

    val fraction: Float

    /**
     * Sets only the alignment of an element within the TopAppBar.
     *
     * @param alignment The desired alignment for the element.
     *
     * @see road
     */
    @Stable
    fun Modifier.align(alignment: Alignment) = this then AppBarParentDataElement(
        alignment = alignment,
        inspector = debugInspectorInfo {
            name = "Alignment"
            value = alignment
        }
    )

    /**
     * Applies a parallax effect to an element within the TopAppBar, creating a visual depth illusion.
     *
     * @param ratio The intensity of the parallax effect, ranging from 0 (no effect) to 1 (maximum effect).
     */
    @Stable
    fun Modifier.parallax(@FloatRange(0.0, 1.0) ratio: Float) = this then AppBarParentDataElement(
        parallax = ratio,
        inspector = debugInspectorInfo {
            name = "Parallax"
            value = ratio
        }
    )

    /**
     * Controls the alignment of an element based on the AppBar's expansion state.
     *
     * @param alignment The alignment for the element when the AppBar is collapsed.
     * @param targetAlignment The alignment for the element when the AppBar is expanded.
     */
    @Stable
    fun Modifier.road(alignment: Alignment, targetAlignment: Alignment) = this then AppBarParentDataElement(
        alignment = alignment,
        targetAlignment = targetAlignment,
        inspector = debugInspectorInfo {
            name = "Road"
            value = alignment
            properties["targetAlignment"] = targetAlignment
        }
    )
}

/**
 * A state object that can be hoisted to control and observe the top app bar state. The state is
 * read and updated by a [TopAppBarScrollBehavior] implementation.
 *
 * In most cases, this state will be created via [rememberTopAppBarState].
 *
 * @param initialHeightOffsetLimit the initial value for [TopAppBarState.heightOffsetLimit]
 * @param initialHeightOffset the initial value for [TopAppBarState.heightOffset]
 * @param initialContentOffset the initial value for [TopAppBarState.contentOffset]
 */
@ExperimentalThemeApi
@Stable
internal class TopAppBarState(
    initialHeightOffsetLimit: Float,
    initialHeightOffset: Float,
    initialContentOffset: Float
): TopAppBarScope {

    override val fraction: Float get() = 1 - collapsedFraction

    /**
     * The top app bar's height offset limit in pixels, which represents the limit that a top app
     * bar is allowed to collapse to.
     *
     * Use this limit to coerce the [heightOffset] value when it's updated.
     */
    var heightOffsetLimit by mutableFloatStateOf(initialHeightOffsetLimit)

    /**
     * The top app bar's current height offset in pixels. This height offset is applied to the fixed
     * height of the app bar to control the displayed height when content is being scrolled.
     *
     * Updates to the [heightOffset] value are coerced between zero and [heightOffsetLimit].
     */
    var heightOffset: Float
        get() = _heightOffset.floatValue
        set(newOffset) {
            _heightOffset.floatValue = newOffset.coerceIn(
                minimumValue = heightOffsetLimit,
                maximumValue = 0f
            )
        }

    /**
     * The total offset of the content scrolled under the top app bar.
     *
     * The content offset is used to compute the [overlappedFraction], which can later be read
     * by an implementation.
     *
     * This value is updated by a [TopAppBarScrollBehavior] whenever a nested scroll connection
     * consumes scroll events. A common implementation would update the value to be the sum of all
     * [NestedScrollConnection.onPostScroll] `consumed.y` values.
     */
    var contentOffset by mutableFloatStateOf(initialContentOffset)

    /**
     * A value that represents the collapsed height percentage of the app bar.
     *
     * A `0.0` represents a fully expanded bar, and `1.0` represents a fully collapsed bar (computed
     * as [heightOffset] / [heightOffsetLimit]).
     */
    val collapsedFraction: Float
        get() = if (heightOffsetLimit != 0f) {
            heightOffset / heightOffsetLimit
        } else {
            0f
        }

    /**
     * A value that represents the percentage of the app bar area that is overlapping with the
     * content scrolled behind it.
     *
     * A `0.0` indicates that the app bar does not overlap any content, while `1.0` indicates that
     * the entire visible app bar area overlaps the scrolled content.
     */
    val overlappedFraction: Float
        get() = if (heightOffsetLimit != 0f) {
            1 - ((heightOffsetLimit - contentOffset).coerceIn(
                minimumValue = heightOffsetLimit,
                maximumValue = 0f
            ) / heightOffsetLimit)
        } else {
            0f
        }

    companion object {
        /**
         * The default [Saver] implementation for [TopAppBarState].
         */
        val Saver: Saver<TopAppBarState, *> = listSaver(
            save = { listOf(it.heightOffsetLimit, it.heightOffset, it.contentOffset) },
            restore = {
                TopAppBarState(
                    initialHeightOffsetLimit = it[0],
                    initialHeightOffset = it[1],
                    initialContentOffset = it[2]
                )
            }
        )
    }

    private var _heightOffset = mutableFloatStateOf(initialHeightOffset)
}

/**
 * Creates a [TopAppBarState] that is remembered across compositions.
 *
 * @param initialHeightOffsetLimit the initial value for [TopAppBarState.heightOffsetLimit],
 * which represents the pixel limit that a top app bar is allowed to collapse when the scrollable
 * content is scrolled
 * @param initialHeightOffset the initial value for [TopAppBarState.heightOffset]. The initial
 * offset height offset should be between zero and [initialHeightOffsetLimit].
 * @param initialContentOffset the initial value for [TopAppBarState.contentOffset]
 */
@ExperimentalThemeApi
@Composable
internal fun rememberTopAppBarScope(
    initialHeightOffsetLimit: Float = -Float.MAX_VALUE,
    initialHeightOffset: Float = 0f,
    initialContentOffset: Float = 0f
): TopAppBarScope {
    return rememberSaveable(saver = TopAppBarState.Saver) {
        TopAppBarState(
            initialHeightOffsetLimit,
            initialHeightOffset,
            initialContentOffset
        )
    }
}

/**
 * A TopAppBarScrollBehavior defines how an app bar should behave when the content under it is
 * scrolled.
 *
 * @see [TopAppBarDefaults.pinnedScrollBehavior]
 * @see [TopAppBarDefaults.enterAlwaysScrollBehavior]
 * @see [TopAppBarDefaults.exitUntilCollapsedScrollBehavior]
 */
@ExperimentalThemeApi
@Stable
sealed class TopAppBarScrollBehavior {

    /**
     * A [TopAppBarState] that is attached to this behavior and is read and updated when scrolling
     * happens.
     */
    internal var state: TopAppBarState? = null

    /**
     * Indicates whether the top app bar is pinned.
     *
     * A pinned app bar will stay fixed in place when content is scrolled and will not react to any
     * drag gestures.
     */
    abstract val isPinned: Boolean

    /**
     * An optional [AnimationSpec] that defines how the top app bar snaps to either fully collapsed
     * or fully extended state when a fling or a drag scrolled it into an intermediate position.
     */
    internal abstract val snapAnimationSpec: AnimationSpec<Float>?

    /**
     * An optional [DecayAnimationSpec] that defined how to fling the top app bar when the user
     * flings the app bar itself, or the content below it.
     */
    internal abstract val flingAnimationSpec: DecayAnimationSpec<Float>?

    /**
     * A [NestedScrollConnection] that should be attached to a [Modifier.nestedScroll] in order to
     * keep track of the scroll events.
     */
    abstract val nestedScrollConnection: NestedScrollConnection
}

/**
 * Settles the app bar by flinging, in case the given velocity is greater than zero, and snapping
 * after the fling settles.
 */
@OptIn(ExperimentalThemeApi::class)
internal suspend fun settleAppBar(
    state: TopAppBarState,
    velocity: Float,
    flingAnimationSpec: DecayAnimationSpec<Float>?,
    snapAnimationSpec: AnimationSpec<Float>?
): Velocity {
    // Check if the app bar is completely collapsed/expanded. If so, no need to settle the app bar,
    // and just return Zero Velocity.
    // Note that we don't check for 0f due to float precision with the collapsedFraction
    // calculation.
    if (state.collapsedFraction < 0.01f || state.collapsedFraction == 1f) {
        return Velocity.Zero
    }
    var remainingVelocity = velocity
    // In case there is an initial velocity that was left after a previous user fling, animate to
    // continue the motion to expand or collapse the app bar.
    if (flingAnimationSpec != null && abs(velocity) > 1f) {
        var lastValue = 0f
        AnimationState(
            initialValue = 0f,
            initialVelocity = velocity,
        )
            .animateDecay(flingAnimationSpec) {
                val delta = value - lastValue
                val initialHeightOffset = state.heightOffset
                state.heightOffset = initialHeightOffset + delta
                val consumed = abs(initialHeightOffset - state.heightOffset)
                lastValue = value
                remainingVelocity = this.velocity
                // avoid rounding errors and stop if anything is unconsumed
                if (abs(delta - consumed) > 0.5f) this.cancelAnimation()
            }
    }
    // Snap if animation specs were provided.
    if (snapAnimationSpec != null) {
        if (state.heightOffset < 0 &&
            state.heightOffset > state.heightOffsetLimit
        ) {
            AnimationState(initialValue = state.heightOffset).animateTo(
                if (state.collapsedFraction < 0.5f) {
                    0f
                } else {
                    state.heightOffsetLimit
                },
                animationSpec = snapAnimationSpec
            ) { state.heightOffset = value }
        }
    }

    return Velocity(0f, remainingVelocity)
}

/**
 * Returns a [TopAppBarScrollBehavior] that only adjusts its content offset, without adjusting any
 * properties that affect the height of a top app bar.
 *
 * @param state a [TopAppBarState]
 * @param canScroll a callback used to determine whether scroll events are to be
 * handled by this [PinnedScrollBehavior]
 */
@OptIn(ExperimentalThemeApi::class)
internal class PinnedScrollBehavior(
    val canScroll: () -> Boolean = { true }
) : TopAppBarScrollBehavior() {
    override val isPinned: Boolean = true
    override val snapAnimationSpec: AnimationSpec<Float>? = null
    override val flingAnimationSpec: DecayAnimationSpec<Float>? = null
    override var nestedScrollConnection =
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val state = state ?: return Offset.Zero
                if (!canScroll()) return Offset.Zero
                if (consumed.y == 0f && available.y > 0f) {
                    // Reset the total content offset to zero when scrolling all the way down.
                    // This will eliminate some float precision inaccuracies.
                    state.contentOffset = 0f
                } else {
                    state.contentOffset += consumed.y
                }
                return Offset.Zero
            }
        }
}

/**
 * A [TopAppBarScrollBehavior] that adjusts its properties to affect the colors and height of a top
 * app bar.
 *
 * A top app bar that is set up with this [TopAppBarScrollBehavior] will immediately collapse when
 * the nested content is pulled up, and will immediately appear when the content is pulled down.
 *
 * @param state a [TopAppBarState]
 * @param snapAnimationSpec an optional [AnimationSpec] that defines how the top app bar snaps to
 * either fully collapsed or fully extended state when a fling or a drag scrolled it into an
 * intermediate position
 * @param flingAnimationSpec an optional [DecayAnimationSpec] that defined how to fling the top app
 * bar when the user flings the app bar itself, or the content below it
 * @param canScroll a callback used to determine whether scroll events are to be
 * handled by this [EnterAlwaysScrollBehavior]
 */
@OptIn(ExperimentalThemeApi::class)
internal class EnterAlwaysScrollBehavior(
    override val snapAnimationSpec: AnimationSpec<Float>?,
    override val flingAnimationSpec: DecayAnimationSpec<Float>?,
    val canScroll: () -> Boolean = { true }
) : TopAppBarScrollBehavior() {
    override val isPinned: Boolean = false
    override var nestedScrollConnection =
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val state = state ?: return Offset.Zero
                if (!canScroll()) return Offset.Zero
                val prevHeightOffset = state.heightOffset
                state.heightOffset = state.heightOffset + available.y
                return if (prevHeightOffset != state.heightOffset) {
                    // We're in the middle of top app bar collapse or expand.
                    // Consume only the scroll on the Y axis.
                    available.copy(x = 0f)
                } else {
                    Offset.Zero
                }
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val state = state ?: return Offset.Zero
                if (!canScroll()) return Offset.Zero
                state.contentOffset += consumed.y
                if (state.heightOffset == 0f || state.heightOffset == state.heightOffsetLimit) {
                    if (consumed.y == 0f && available.y > 0f) {
                        // Reset the total content offset to zero when scrolling all the way down.
                        // This will eliminate some float precision inaccuracies.
                        state.contentOffset = 0f
                    }
                }
                state.heightOffset = state.heightOffset + consumed.y
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                val superConsumed = super.onPostFling(consumed, available)
                val state = state ?: return Velocity.Zero
                return superConsumed + settleAppBar(
                    state,
                    available.y,
                    flingAnimationSpec,
                    snapAnimationSpec
                )
            }
        }
}

/**
 * A [TopAppBarScrollBehavior] that adjusts its properties to affect the colors and height of a top
 * app bar.
 *
 * A top app bar that is set up with this [TopAppBarScrollBehavior] will immediately collapse when
 * the nested content is pulled up, and will expand back the collapsed area when the content is
 * pulled all the way down.
 *
 * @param state a [TopAppBarState]
 * @param snapAnimationSpec an optional [AnimationSpec] that defines how the top app bar snaps to
 * either fully collapsed or fully extended state when a fling or a drag scrolled it into an
 * intermediate position
 * @param flingAnimationSpec an optional [DecayAnimationSpec] that defined how to fling the top app
 * bar when the user flings the app bar itself, or the content below it
 * @param canScroll a callback used to determine whether scroll events are to be
 * handled by this [ExitUntilCollapsedScrollBehavior]
 */
@OptIn(ExperimentalThemeApi::class)
internal class ExitUntilCollapsedScrollBehavior(
    override val snapAnimationSpec: AnimationSpec<Float>?,
    override val flingAnimationSpec: DecayAnimationSpec<Float>?,
    val canScroll: () -> Boolean = { true }
) : TopAppBarScrollBehavior() {
    override val isPinned: Boolean = false
    override var nestedScrollConnection =
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val state = state ?: return Offset.Zero
                // Don't intercept if scrolling down.
                if (!canScroll() || available.y > 0f) return Offset.Zero

                val prevHeightOffset = state.heightOffset
                state.heightOffset = state.heightOffset + available.y
                return if (prevHeightOffset != state.heightOffset) {
                    // We're in the middle of top app bar collapse or expand.
                    // Consume only the scroll on the Y axis.
                    available.copy(x = 0f)
                } else {
                    Offset.Zero
                }
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val state = state ?: return Offset.Zero
                if (!canScroll()) return Offset.Zero
                state.contentOffset += consumed.y

                if (available.y < 0f || consumed.y < 0f) {
                    // When scrolling up, just update the state's height offset.
                    val oldHeightOffset = state.heightOffset
                    state.heightOffset = state.heightOffset + consumed.y
                    return Offset(0f, state.heightOffset - oldHeightOffset)
                }

                if (consumed.y == 0f && available.y > 0) {
                    // Reset the total content offset to zero when scrolling all the way down. This
                    // will eliminate some float precision inaccuracies.
                    state.contentOffset = 0f
                }

                if (available.y > 0f) {
                    // Adjust the height offset in case the consumed delta Y is less than what was
                    // recorded as available delta Y in the pre-scroll.
                    val oldHeightOffset = state.heightOffset
                    state.heightOffset = state.heightOffset + available.y
                    return Offset(0f, state.heightOffset - oldHeightOffset)
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                val superConsumed = super.onPostFling(consumed, available)
                val state = state ?: return Velocity.Zero
                return superConsumed + settleAppBar(
                    state,
                    available.y,
                    flingAnimationSpec,
                    snapAnimationSpec
                )
            }
        }
}