/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 06-02-2024.
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

package com.primex.material2.appbar

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.primex.core.ExperimentalToolkitApi
import com.primex.material2.ProvideTextStyle
import com.primex.material2.R

/**
 * Represents the visual styles and dimensions of a top app bar in different states,
 * including colors, text styles, and heights. It animates the container color and
 * title text style based on scroll state, providing a visually dynamic experience.
 *
 * @constructor Creates a `TopAppBarStyle` with specified colors, text styles, and heights.
 * Use [TopAppBarColors] for a factory method using default Material 3 specifications.
 *
 * @param containerColor The background color of the top app bar.
 * @param scrolledContainerColor The background color when content is scrolled behind it.
 * @param contentColor The primary content color for elements like navigation icons, title, and actions.
 * @param scrolledContentColor The content color when content is scrolled behind the top app bar.
 * @param titleTextStyle The text style used for the title.
 * @param scrolledTitleTextStyle The text style used for the title when content is scrolled.
 * @param height The default height of the top app bar.
 * @param maxHeight The maximum height the top app bar can expand to.
 *
 * @see TopAppBarColors
 */
@ExperimentalToolkitApi
@Stable
class TopAppBarStyle internal constructor(
    val containerColor: Color,
    val scrolledContainerColor: Color,
    val contentColor: Color,
    val scrolledContentColor: Color,
    val titleTextStyle: TextStyle,
    val scrolledTitleTextStyle: TextStyle,
    val height: Dp,
    val maxHeight: Dp,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TopAppBarStyle

        if (containerColor != other.containerColor) return false
        if (scrolledContainerColor != other.scrolledContainerColor) return false
        if (contentColor != other.contentColor) return false
        if (scrolledContentColor != other.scrolledContentColor) return false
        if (titleTextStyle != other.titleTextStyle) return false
        if (scrolledTitleTextStyle != other.scrolledTitleTextStyle) return false
        if (height != other.height) return false
        return maxHeight == other.maxHeight
    }

    override fun hashCode(): Int {
        var result = containerColor.hashCode()
        result = 31 * result + scrolledContainerColor.hashCode()
        result = 31 * result + contentColor.hashCode()
        result = 31 * result + scrolledContentColor.hashCode()
        result = 31 * result + titleTextStyle.hashCode()
        result = 31 * result + scrolledTitleTextStyle.hashCode()
        result = 31 * result + height.hashCode()
        result = 31 * result + maxHeight.hashCode()
        return result
    }

    override fun toString(): String {
        return "TopAppBarStyle(" +
                "containerColor=$containerColor, " +
                "scrolledContainerColor=$scrolledContainerColor, " +
                "contentColor=$contentColor, " +
                "scrolledContentColor=$scrolledContentColor, " +
                "titleTextStyle=$titleTextStyle, " +
                "scrolledTitleTextStyle=$scrolledTitleTextStyle, " +
                "height=$height, " +
                "maxHeight=$maxHeight)"
    }

    fun copy(
        containerColor: Color = this.containerColor,
        scrolledContainerColor: Color = this.scrolledContainerColor,
        contentColor: Color = this.containerColor,
        scrolledContentColor: Color = this.scrolledContentColor,
        titleTextStyle: TextStyle = this.titleTextStyle,
        scrolledTitleTextStyle: TextStyle = this.scrolledTitleTextStyle,
    ): TopAppBarStyle = TopAppBarStyle(
        containerColor,
        scrolledContainerColor,
        contentColor,
        scrolledContentColor,
        titleTextStyle,
        scrolledTitleTextStyle,
        height,
        maxHeight
    )

    /**
     * Returns the container color interpolated based on the provided scroll fraction.
     *
     * @param fraction A value between 0.0 and 1.0, indicating the scroll state:
     *     - 0.0: Fully collapsed
     *     - 1.0: Fully expanded or overlapped
     *
     * @return The interpolated container color, smoothly transitioning between
     *     `containerColor` and `scrolledContainerColor` using a fast-out-linear-in easing.
     */
    @Stable
    internal fun containerColor(fraction: Float): Color {
        return androidx.compose.ui.graphics.lerp(
            containerColor,
            scrolledContainerColor,
            FastOutLinearInEasing.transform(fraction)
        )
    }

    /**
     * @see containerColor
     */
    @Stable
    internal fun contentColor(fraction: Float): Color {
        return androidx.compose.ui.graphics.lerp(
            contentColor,
            scrolledContentColor,
            FastOutLinearInEasing.transform(fraction)
        )
    }

    /**
     * @see containerColor
     */
    @Stable
    internal fun titleTextStyle(fraction: Float): TextStyle {
        return androidx.compose.ui.text.lerp(
            titleTextStyle,
            scrolledTitleTextStyle,
            FastOutLinearInEasing.transform(fraction)
        )
    }
}

/**
 * A central object providing default values and convenient functions for configuring
 * [TopAppBar] & [LargeTopBar] components.
 */
object TopAppBarDefaults {
    /**
     * The default horizontal padding applied to the content of the top app bar.
     */
    val TopAppBarHorizontalPadding = 4.dp

    /**
     * The horizontal inset used for the title when the top app bar is medium or large,
     * or when sizing a spacer for a missing navigation icon.
     */
    val ExpandedTitleHorizontalPadding = 16.dp - TopAppBarHorizontalPadding

    /**
     * The bottom padding applied to the title when the top app bar is expanded.
     */
    val ExpandedTitleBottomPadding = 28.dp

    /**
     * The horizontal space used when the top app bar is missing a leading icon.
     * Equivalent to `ExpandedTitleHorizontalPadding`.
     */
    val TopBarMissingLeadingIconSpace = ExpandedTitleHorizontalPadding

    /**
     * @see APP_BAR_LAYOUT_ID_BACKGROUND
     */
    val LayoutIdBackground = APP_BAR_LAYOUT_ID_BACKGROUND
    val LayoutIdNavIcon = APP_BAR_LAYOUT_ID_NAVIGATION_ICON
    val LayoutIdTitle = APP_BAR_LAYOUT_ID_TITLE
    val LayoutIdCollapsable_title = APP_BAR_LAYOUT_ID_COLLAPSABLE_TITLE
    val LayoutIdAction = APP_BAR_LAYOUT_ID_ACTIONS

    /**
     * The default height of a large top app bar.
     */
    val LargeTopBarHeight = 156.dp

    /**
     * The default height of a non-large top app bar.
     */
    val TopBarHeight = 56.dp

    /**
     * Creates a [TopAppBarStyle] suitable for large top app bars, which feature
     * scrollable content and expandable height.
     *
     * @see TopAppBarStyle
     */
    @OptIn(ExperimentalToolkitApi::class)
    @Composable
    fun largeAppBarStyle(
        containerColor: Color = MaterialTheme.colors.background,
        scrolledContainerColor: Color = MaterialTheme.colors.primary,
        contentColor: Color = MaterialTheme.colors.onBackground,
        scrolledContentColor: Color = MaterialTheme.colors.onPrimary,
        titleTextStyle: TextStyle = MaterialTheme.typography.body1,
        scrolledTitleTextStyle: TextStyle = MaterialTheme.typography.h4,
        height: Dp = TopBarHeight,
        maxHeight: Dp = LargeTopBarHeight,
    ) = TopAppBarStyle(
        containerColor = containerColor,
        scrolledContainerColor = scrolledContainerColor,
        contentColor = contentColor,
        scrolledContentColor = scrolledContentColor,
        titleTextStyle = titleTextStyle,
        scrolledTitleTextStyle = scrolledTitleTextStyle,
        height = height,
        maxHeight = maxHeight
    )

    /**
     * Creates a [TopAppBarStyle] suitable for smaller, non-expandable top app bars.
     * @see largeAppBarStyle
     */
    @ExperimentalToolkitApi
    @Composable
    fun topAppBarStyle(
        containerColor: Color = MaterialTheme.colors.background,
        scrolledContainerColor: Color = MaterialTheme.colors.primary,
        contentColor: Color = MaterialTheme.colors.onBackground,
        scrolledContentColor: Color = MaterialTheme.colors.onPrimary,
        titleTextStyle: TextStyle = MaterialTheme.typography.body1,
        height: Dp = TopBarHeight,
    ) = TopAppBarStyle(
        containerColor = containerColor,
        scrolledContainerColor = scrolledContainerColor,
        contentColor = contentColor,
        scrolledContentColor = scrolledContentColor,
        titleTextStyle = titleTextStyle,
        scrolledTitleTextStyle = titleTextStyle,
        height = height,
        maxHeight = height
    )

    /**
     * Default insets to be used and consumed by the top app bars
     */
    val windowInsets: WindowInsets
        @Composable
        get() = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)

    @ExperimentalToolkitApi
    @Composable
    fun pinnedScrollBehavior(
        canScroll: () -> Boolean = { true }
    ): TopAppBarScrollBehavior = PinnedScrollBehavior(canScroll = canScroll)

    /**
     * Returns a [TopAppBarScrollBehavior]. A top app bar that is set up with this
     * [TopAppBarScrollBehavior] will immediately collapse when the content is pulled up, and will
     * immediately appear when the content is pulled down.
     *
     * @param state the state object to be used to control or observe the top app bar's scroll
     * state. See [rememberTopAppBarState] for a state that is remembered across compositions.
     * @param canScroll a callback used to determine whether scroll events are to be
     * handled by this [EnterAlwaysScrollBehavior]
     * @param snapAnimationSpec an optional [AnimationSpec] that defines how the top app bar snaps
     * to either fully collapsed or fully extended state when a fling or a drag scrolled it into an
     * intermediate position
     * @param flingAnimationSpec an optional [DecayAnimationSpec] that defined how to fling the top
     * app bar when the user flings the app bar itself, or the content below it
     */
    @ExperimentalToolkitApi
    @Composable
    fun enterAlwaysScrollBehavior(
        canScroll: () -> Boolean = { true },
        snapAnimationSpec: AnimationSpec<Float>? = spring(stiffness = Spring.StiffnessMediumLow),
        flingAnimationSpec: DecayAnimationSpec<Float>? = rememberSplineBasedDecay()
    ): TopAppBarScrollBehavior =
        EnterAlwaysScrollBehavior(
            snapAnimationSpec = snapAnimationSpec,
            flingAnimationSpec = flingAnimationSpec,
            canScroll = canScroll
        )

    /**
     * Returns a [TopAppBarScrollBehavior] that adjusts its properties to affect the colors and
     * height of the top app bar.
     *
     * A top app bar that is set up with this [TopAppBarScrollBehavior] will immediately collapse
     * when the nested content is pulled up, and will expand back the collapsed area when the
     * content is  pulled all the way down.
     *
     * @param state the state object to be used to control or observe the top app bar's scroll
     * state. See [rememberTopAppBarState] for a state that is remembered across compositions.
     * @param canScroll a callback used to determine whether scroll events are to be
     * handled by this [ExitUntilCollapsedScrollBehavior]
     * @param snapAnimationSpec an optional [AnimationSpec] that defines how the top app bar snaps
     * to either fully collapsed or fully extended state when a fling or a drag scrolled it into an
     * intermediate position
     * @param flingAnimationSpec an optional [DecayAnimationSpec] that defined how to fling the top
     * app bar when the user flings the app bar itself, or the content below it
     */
    @ExperimentalToolkitApi
    @Composable
    fun exitUntilCollapsedScrollBehavior(
        canScroll: () -> Boolean = { true },
        snapAnimationSpec: AnimationSpec<Float>? = spring(stiffness = Spring.StiffnessMediumLow),
        flingAnimationSpec: DecayAnimationSpec<Float>? = rememberSplineBasedDecay()
    ): TopAppBarScrollBehavior =
        ExitUntilCollapsedScrollBehavior(
            snapAnimationSpec = snapAnimationSpec,
            flingAnimationSpec = flingAnimationSpec,
            canScroll = canScroll
        )
}

/**
 * Creates a large top app bar with default styling and behavior.
 *
 * @param title The content of the title.
 * @param modifier Optional modifiers to apply to the top app bar.
 * @param navigationIcon Optional composable for the navigation icon.
 * @param actions Optional composable for the actions.
 * @param windowInsets The window insets to use.
 * @param style The styling for the top app bar.
 * @param scrollBehavior The scroll behavior for the top app bar.
 */
@ExperimentalToolkitApi
@Composable
fun LargeTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable() () -> Unit = {},
    actions: @Composable() RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    style: TopAppBarStyle = TopAppBarDefaults.largeAppBarStyle(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
) = CollapsableTopBarLayout(
    height = style.height,
    maxHeight = style.maxHeight,
    insets = windowInsets,
    modifier = modifier.clipToBounds(),
    scrollBehavior = scrollBehavior
) {
    require(style.height < style.maxHeight){
        "LargeTopAppBar maxHeight (${style.maxHeight}) must be greater than height (${style.height})"
    }
    val appBarContentColor = style.contentColor(1 - fraction)
    val textStyle = style.titleTextStyle(fraction)
    ProvideTextStyle(style = textStyle, color = appBarContentColor) {
        // Background; zIndex determines which is stacked where.
        // so this will be at the bottom.
        val containerColor = style.containerColor(1 - fraction)
        Spacer(
            modifier = Modifier
                .background(containerColor)
                .layoutId(APP_BAR_LAYOUT_ID_BACKGROUND)
                .fillMaxSize()
        )
        // Defines the navIcon and actions first;
        // make sure that title is always last; because if it is not; a new list of
        // measurables will be created; which will make sure it is at the last.
        Box(
            Modifier
                .layoutId(APP_BAR_LAYOUT_ID_NAVIGATION_ICON)
                .padding(start = TopAppBarDefaults.TopAppBarHorizontalPadding),
            content = { navigationIcon() }
        )

        // Actions
        Box(
            Modifier
                .layoutId(APP_BAR_LAYOUT_ID_ACTIONS)
                .padding(end = TopAppBarDefaults.TopAppBarHorizontalPadding),
            content = {
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    content = actions
                )
            }
        )

        Box(
            Modifier
                .layoutId(APP_BAR_LAYOUT_ID_COLLAPSABLE_TITLE)
                .padding(horizontal = TopAppBarDefaults.TopAppBarHorizontalPadding),
            content = { title() }
        )
    }
}

/**
 * Creates a normal [TopAppBar] with default styles and behaviour.
 * @see LargeTopAppBar
 */
@ExperimentalToolkitApi
@Composable
fun TopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable() () -> Unit = {},
    actions: @Composable() RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    style: TopAppBarStyle = TopAppBarDefaults.topAppBarStyle(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
) = CollapsableTopBarLayout(
    height = style.height,
    maxHeight = style.height,
    insets = windowInsets,
    modifier = modifier.clipToBounds(),
    scrollBehavior = scrollBehavior
) {
    // TODO: Add this property to TopAppBarState.
    val overlappedFraction = (this as TopAppBarState).overlappedFraction
    // Obtain the container color from the TopAppBarColors using the `overlapFraction`. This
    // ensures that the colors will adjust whether the app bar behavior is pinned or scrolled.
    // This may potentially animate or interpolate a transition between the container-color and the
    // container's scrolled-color according to the app bar's scroll state.
    val fraction = if (overlappedFraction > 0.01f) 1f else 0f

    ProvideTextStyle(style = style.scrolledTitleTextStyle, color = style.contentColor(fraction)) {
        // Background; zIndex determines which is stacked where.
        // so this will be at the bottom.
        val appBarContainerColor by animateColorAsState(
            targetValue = style.containerColor(fraction),
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "appBarContainerColor"
        )
        Spacer(
            modifier = Modifier
                .background(appBarContainerColor)
                .layoutId(APP_BAR_LAYOUT_ID_BACKGROUND)
                .fillMaxSize()
        )
        // Defines the navIcon and actions first;
        // make sure that title is always last; because if it is not; a new list of
        // measurables will be created; which will make sure it is at the last.
        Box(
            Modifier
                .layoutId(APP_BAR_LAYOUT_ID_NAVIGATION_ICON)
                .padding(start = TopAppBarDefaults.TopAppBarHorizontalPadding),
            content = { navigationIcon() }
        )

        // Actions
        Box(
            Modifier
                .layoutId(APP_BAR_LAYOUT_ID_ACTIONS)
                .padding(end = TopAppBarDefaults.TopAppBarHorizontalPadding),
            content = {
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    content = actions
                )
            }
        )

        Box(
            Modifier
                .layoutId(APP_BAR_LAYOUT_ID_TITLE)
                .padding(horizontal = TopAppBarDefaults.TopAppBarHorizontalPadding),
            content = { title() }
        )
    }
}