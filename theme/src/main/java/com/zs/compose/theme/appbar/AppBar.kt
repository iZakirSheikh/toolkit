/*
 * Copyright 2025 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 08-02-2025.
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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zs.compose.foundation.thenIf
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.ContentAlpha
import com.zs.compose.theme.LocalContentColor
import com.zs.compose.theme.None
import com.zs.compose.theme.Surface
import com.zs.compose.theme.text.ProvideTextStyle

private val TopAppBarHeight = 56.dp
private val SideBarMinWidth = 80.dp
private val BottomAppBarMinHeight = SideBarMinWidth

// TODO: this should probably be part of the touch target of the start and end icons, clarify this
private val AppBarHorizontalPadding = 4.dp
private val SideBarVerticalPadding = AppBarHorizontalPadding
/**
 * Padding at the bottom of the [SideBar]'s header. This padding will only be added when the
 * header is not null.
 */
private val SideBarHeaderPadding: Dp = 8.dp

// Start inset for the title when there is no navigation icon provided
private val TitleInsetWithoutIcon = Modifier.width(16.dp - AppBarHorizontalPadding)

// Start inset for the title when there is a navigation icon provided
private val TitleIconModifier = Modifier
    .fillMaxHeight()
    .width(72.dp - AppBarHorizontalPadding)

/// The space between conservative items of Bottom app bar
private val BottomBarItemHorizontalPadding: Dp = 8.dp

/** Contains default values used for [TopAppBar] and [BottomAppBar]. */
object AppBarDefaults {
    // TODO: clarify elevation in surface mapping - spec says 0.dp but it appears to have an
    //  elevation overlay applied in dark theme examples.
    /** Default elevation used for [TopAppBar]. */
    val TopAppBarElevation = 4.dp

    /** Default elevation used for [BottomAppBar]. */
    val BottomAppBarElevation = 2.dp

    /** Default padding used for [TopAppBar] and [BottomAppBar]. */
    val ContentPadding =
        PaddingValues(start = AppBarHorizontalPadding, end = AppBarHorizontalPadding)

    /** Recommended insets to be used and consumed by the top app bars */
    val topAppBarWindowInsets: WindowInsets
        @Composable
        get() =
            WindowInsets.systemBars.only(
                WindowInsetsSides.Horizontal + WindowInsetsSides.Top
            )

    /** Recommended insets to be used and consumed by the bottom app bars */
    val bottomAppBarWindowInsets: WindowInsets
        @Composable
        get() {
            return WindowInsets.systemBars.only(
                WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
            )
        }

    /** Default elevation used for [SideBar]. */
    val SideBarElevation = 8.dp

    /** Recommended window insets for side bar. */
    val sideBarWindowInsets: WindowInsets
        @Composable
        get() =
            WindowInsets.systemBars.only(
                WindowInsetsSides.Vertical + WindowInsetsSides.Start
            )
}

/**
 * [Material Design top app bar](https://m3.material.io/components/top-app-bar/overview)
 *
 * Top app bars display information and actions at the top of a screen.
 *
 * ![Top app bar image](https://developer.android.com/images/reference/androidx/compose/material3/small-top-app-bar.png)
 *
 * This TopAppBar has slots for a title, navigation icon, and actions. Note that the [title] slot is
 * inset from the start according to spec - for custom use cases such as horizontally centering the
 * title, use the other TopAppBar overload for a generic TopAppBar with no restriction on content.
 *
 * @sample androidx.compose.material.samples.SimpleTopAppBar
 * @param title The title to be displayed in the center of the TopAppBar
 * @param windowInsets a window insets that app bar will respect.
 * @param modifier The [Modifier] to be applied to this TopAppBar
 * @param navigationIcon The navigation icon displayed at the start of the TopAppBar. This should
 *   typically be an [IconButton] or [IconToggleButton].
 * @param actions The actions displayed at the end of the TopAppBar. This should typically be
 *   [IconButton]s. The default layout here is a [Row], so icons inside will be placed horizontally.
 * @param backgroundColor The background color for the TopAppBar. Use [Color.Transparent] to have no
 *   color.
 * @param contentColor The preferred content color provided by this TopAppBar to its children.
 *   Defaults to either the matching content color for [backgroundColor], or if [backgroundColor] is
 *   not a color from the theme, this will keep the same value set above this TopAppBar.
 * @param elevation the elevation of this TopAppBar.
 */
@Composable
fun TopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = AppBarDefaults.topAppBarWindowInsets,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    shape: Shape = RectangleShape,
    backgroundColor: Color = AppTheme.colors.background(1.dp),
    contentColor: Color = AppTheme.colors.onBackground,
    elevation: Dp = AppBarDefaults.TopAppBarElevation,
) {
    Surface(
        color = backgroundColor,
        contentColor = contentColor.copy(ContentAlpha.medium),
        elevation = elevation,
        shape = shape,
        modifier = modifier
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .thenIf(windowInsets !== WindowInsets.None) { windowInsetsPadding(windowInsets) }
                .padding(AppBarDefaults.ContentPadding)
                .height(TopAppBarHeight),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            content = {
                if (navigationIcon == null) Spacer(TitleInsetWithoutIcon) else {
                    Row(TitleIconModifier, verticalAlignment = Alignment.CenterVertically) {
                        CompositionLocalProvider(
                            LocalContentColor provides contentColor.copy(ContentAlpha.high),
                            content = navigationIcon
                        )
                    }
                }

                Row(Modifier
                    .fillMaxHeight()
                    .weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    ProvideTextStyle(value = AppTheme.typography.title2) {
                        CompositionLocalProvider(
                            LocalContentColor provides contentColor.copy(ContentAlpha.high),
                            content = title
                        )
                    }
                }

                CompositionLocalProvider(LocalContentColor provides contentColor.copy(ContentAlpha.medium)) {
                    Row(
                        Modifier.fillMaxHeight(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                        content = actions
                    )
                }
            }
        )
    }
}

/**
 * [Material Design bottom app bar](https://m3.material.io/components/bottom-app-bar/overview)
 *
 * A bottom app bar displays navigation and key actions at the bottom of small screens.
 *
 * [Bottom app bar image](https://developer.android.com/images/reference/androidx/compose/material3/bottom-app-bar.png)
 *
 * A bottom app bar displays navigation and key actions at the bottom of screens.
 *
 * ![App bars: bottom image](https://lh3.googleusercontent.com/ue2BtghXOybkv5flhCofY63pvV-HaTJ-RfQngqIoWBLo7tnO1IIgIduRtfx_S9LAUJuDJLCREVrbU4ZBGZMtzyo7el9R5t9xAlsaP0pkuGM=s0)
 *
 *
 * The [LocalContentColor] inside a BottomAppBar is [ContentAlpha.medium] - this is the default for
 * trailing and overflow icons. It is recommended that any leading icons at the start of the
 * BottomAppBar, such as a menu icon, use [ContentAlpha.high] instead. This is demonstrated in the
 * sample below.
 *
 * Also see [BottomNavigation].
 *
 * @sample androidx.compose.material.samples.SimpleBottomAppBar
 * @param windowInsets a window insets that app bar will respect.
 * @param modifier The [Modifier] to be applied to this BottomAppBar
 * @param backgroundColor The background color for the BottomAppBar. Use [Color.Transparent] to have
 *   no color.
 * @param contentColor The preferred content color provided by this BottomAppBar to its children.
 *   Defaults to either the matching content color for [backgroundColor], or if [backgroundColor] is
 *   not a color from the theme, this will keep the same value set above this BottomAppBar.
 * @param shape the shape of the bottom [AppBar]
 * @param elevation the elevation of this BottomAppBar.
 * @param contentPadding the padding applied to the content of this BottomAppBar
 * @param content the content of this BottomAppBar. The default layout here is a [Row], so content
 *   inside will be placed horizontally.
 */
@Composable
fun BottomAppBar(
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = AppBarDefaults.bottomAppBarWindowInsets,
    backgroundColor: Color = AppTheme.colors.background(1.dp),
    contentColor: Color = AppTheme.colors.onBackground,
    shape: Shape = RectangleShape,
    elevation: Dp = AppBarDefaults.BottomAppBarElevation,
    contentPadding: PaddingValues = AppBarDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
){
    Surface(
        color = backgroundColor,
        contentColor = contentColor.copy(ContentAlpha.medium),
        elevation = elevation,
        shape = shape,
        modifier = modifier
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .thenIf(windowInsets !== WindowInsets.None) { windowInsetsPadding(windowInsets) }
                .padding(contentPadding)
                .defaultMinSize(minHeight = BottomAppBarMinHeight),
            horizontalArrangement = Arrangement.spacedBy(BottomBarItemHorizontalPadding, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }

}

/**
 * <a href="https://material.io/components/navigation-rail" class="external"
 * target="_blank">Material Design navigation rail</a>.
 *
 * A Navigation Rail is a side navigation component that allows movement between primary
 * destinations in an app. A navigation rail should be used to display three to seven app
 * destinations and, optionally, a [FloatingActionButton] or a logo inside [header]. Each
 * destination is typically represented by an icon and an optional text label.
 *
 * ![Navigation rail
 * image](https://developer.android.com/images/reference/androidx/compose/material/navigation-rail.png)
 *
 * This particular overload provides ability to specify [WindowInsets]. Recommended value can be
 * found in [NavigationRailDefaults.windowInsets].
 *
 * NavigationRail should contain multiple [NavigationRailItem]s, each representing a singular
 * destination.
 *
 * A simple example looks like:
 *
 * @sample androidx.compose.material.samples.NavigationRailSample
 *
 * See [NavigationRailItem] for configuration specific to each item, and not the overall
 * NavigationRail component.
 *
 * For more information, see [Navigation Rail](https://material.io/components/navigation-rail/)
 *
 * @param windowInsets a window insets that navigation rail will respect
 * @param modifier optional [Modifier] for this NavigationRail
 * @param backgroundColor The background color for this NavigationRail
 * @param contentColor The preferred content color provided by this NavigationRail to its children.
 *   Defaults to either the matching content color for [backgroundColor], or if [backgroundColor] is
 *   not a color from the theme, this will keep the same value set above this NavigationRail.
 * @param elevation elevation for this NavigationRail
 * @param header an optional header that may hold a [FloatingActionButton] or a logo
 * @param content destinations inside this NavigationRail, this should contain multiple
 *   [NavigationRailItem]s
 */
@Composable
fun SideBar(
    windowInsets: WindowInsets = AppBarDefaults.sideBarWindowInsets,
    modifier: Modifier = Modifier,
    backgroundColor: Color = AppTheme.colors.background(1.dp),
    contentColor: Color = AppTheme.colors.onBackground,
    elevation: Dp = AppBarDefaults.SideBarElevation,
    header: @Composable (ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        color = backgroundColor,
        contentColor = contentColor,
        elevation = elevation
    ) {
        Column(
            Modifier
                .fillMaxHeight()
                .thenIf(windowInsets !== WindowInsets.None) { windowInsetsPadding(windowInsets) }
                .defaultMinSize(minWidth = SideBarMinWidth)
                .padding(vertical = SideBarVerticalPadding)
                .selectableGroup(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SideBarVerticalPadding)
        ) {
            if (header != null) {
                header()
                Spacer(Modifier.height(SideBarHeaderPadding))
            }
            content()
        }
    }
}

