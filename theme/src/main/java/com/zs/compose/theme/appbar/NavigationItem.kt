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

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.ContentAlpha
import com.zs.compose.theme.ExperimentalThemeApi
import com.zs.compose.theme.LocalContentColor
import com.zs.compose.theme.Surface
import com.zs.compose.theme.ripple
import com.zs.compose.theme.text.ProvideTextStyle

private val BottomBarItemMinSize = 80.dp


private val BottomBarIndicatorIconPadding =
    PaddingValues(horizontal = 16.dp, vertical = 2.dp)

private val IndicatorLabelArrangement =
    Arrangement.spacedBy(4.dp, Alignment.CenterVertically)

private val BottomBarIndicatorAnimSpec =
    spring<Color>(Spring.DampingRatioMediumBouncy, Spring.StiffnessVeryLow)
private val SideBarIndicatorAnimSpec =
    spring<IntSize>(Spring.DampingRatioMediumBouncy)

private val SideBarItemShape = RoundedCornerShape(20)
private val SideBarItemPadding = PaddingValues(10.dp)

/**
 * Represents the colors of the various elements of a navigation item.
 *
 * @param selectedIconColor the color to use for the icon when the item is selected.
 * @param selectedTextColor the color to use for the text label when the item is selected.
 * @param selectedIndicatorColor the color to use for the indicator when the item is selected.
 * @param unselectedIconColor the color to use for the icon when the item is unselected.
 * @param unselectedTextColor the color to use for the text label when the item is unselected.
 * @param disabledIconColor the color to use for the icon when the item is disabled.
 * @param disabledTextColor the color to use for the text label when the item is disabled.
 * @constructor create an instance with arbitrary colors.
 */
@Immutable
class NavigationItemColors internal constructor(
    val selectedIconColor: Color,
    val selectedTextColor: Color,
    val indicatorColor: Color,
    val unselectedIconColor: Color,
    val unselectedTextColor: Color,
    val disabledIconColor: Color,
    val disabledTextColor: Color,
) {
    /**
     * Returns a copy of this NavigationBarItemColors, optionally overriding some of the values.
     * This uses the Color.Unspecified to mean “use the value from the source”
     */
    fun copy(
        selectedIconColor: Color = this.selectedIconColor,
        selectedTextColor: Color = this.selectedTextColor,
        indicatorColor: Color = this.indicatorColor,
        unselectedIconColor: Color = this.unselectedIconColor,
        unselectedTextColor: Color = this.unselectedTextColor,
        disabledIconColor: Color = this.disabledIconColor,
        disabledTextColor: Color = this.disabledTextColor,
    ) = NavigationItemColors(
        selectedIconColor,
        selectedTextColor,
        indicatorColor,
        unselectedIconColor,
        unselectedTextColor,
        disabledIconColor,
        disabledTextColor,
    )

    /**
     * Represents the icon color for this item, depending on whether it is [selected].
     *
     * @param selected whether the item is selected
     * @param enabled whether the item is enabled
     */
    @Stable
    internal fun iconColor(selected: Boolean, enabled: Boolean): Color =
        when {
            !enabled -> disabledIconColor
            selected -> selectedIconColor
            else -> unselectedIconColor
        }

    /**
     * Represents the text color for this item, depending on whether it is [selected].
     *
     * @param selected whether the item is selected
     * @param enabled whether the item is enabled
     */
    @Stable
    internal fun textColor(selected: Boolean, enabled: Boolean): Color =
        when {
            !enabled -> disabledTextColor
            selected -> selectedTextColor
            else -> unselectedTextColor
        }


    /** Represents the color of the indicator used for selected items. */
    internal fun indicatorColor(selected: Boolean, enabled: Boolean): Color =
        if (selected) indicatorColor else Color.Transparent

    override fun hashCode(): Int {
        var result = selectedIconColor.hashCode()
        result = 31 * result + selectedTextColor.hashCode()
        result = 31 * result + indicatorColor.hashCode()
        result = 31 * result + unselectedIconColor.hashCode()
        result = 31 * result + unselectedTextColor.hashCode()
        result = 31 * result + disabledIconColor.hashCode()
        result = 31 * result + disabledTextColor.hashCode()
        result = 31 * result + indicatorColor.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NavigationItemColors

        if (selectedIconColor != other.selectedIconColor) return false
        if (selectedTextColor != other.selectedTextColor) return false
        if (indicatorColor != other.indicatorColor) return false
        if (unselectedIconColor != other.unselectedIconColor) return false
        if (unselectedTextColor != other.unselectedTextColor) return false
        if (disabledIconColor != other.disabledIconColor) return false
        if (disabledTextColor != other.disabledTextColor) return false
        if (indicatorColor != other.indicatorColor) return false

        return true
    }
}

/**
 * Represents the defaults used in [NavigationItem].
 */
object NavigationItemDefaults {

    /**
     * Represents the [NavigationItemColors] used in [NavigationItem]
     */
    @Composable
    fun colors(
        selectedIconColor: Color = AppTheme.colors.onAccent,
        selectedTextColor: Color = AppTheme.colors.accent,
        selectedIndicatorColor: Color = AppTheme.colors.accent,
        unselectedIconColor: Color = AppTheme.colors.onBackground,
        unselectedTextColor: Color = unselectedIconColor,
        disabledIconColor: Color = unselectedIconColor.copy(ContentAlpha.medium),
        disabledTextColor: Color = unselectedTextColor.copy(ContentAlpha.medium)
    ) = NavigationItemColors(
        selectedIconColor,
        selectedTextColor,
        selectedIndicatorColor,
        unselectedIconColor,
        unselectedTextColor,
        disabledIconColor,
        disabledTextColor
    )
}

/**
 * Material Design navigation bar item.
 *
 * Navigation bars offer a persistent and convenient way to switch between primary destinations in
 * an app.
 *
 * @param selected whether this item is selected
 * @param onClick called when this item is clicked
 * @param icon icon for this item, typically an [Icon]
 * @param modifier the [Modifier] to be applied to this item
 * @param enabled controls the enabled state of this item. When `false`, this component will not
 *   respond to user input, and it will appear visually disabled and disabled to accessibility
 *   services.
 * @param label optional text label for this item
 * @param colors [NavigationItemColors] that will be used to resolve the colors used for this
 *   item in different states. See [NavigationItemDefaults.colors].
 * @param interactionSource an optional hoisted [MutableInteractionSource] for observing and
 *   emitting [Interaction]s for this item. You can use this to change the item's appearance or
 *   preview the item in different states. Note that if `null` is provided, interactions will still
 *   happen internally.
 */
@Composable
fun BottomNavigationItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    label: @Composable (() -> Unit),
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    colors: NavigationItemColors = NavigationItemDefaults.colors()
) {
    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember(::MutableInteractionSource)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = IndicatorLabelArrangement,
        modifier = modifier
            .selectable(
                selected = selected,
                onClick = onClick,
                enabled = enabled,
                role = Role.Tab,
                interactionSource = interactionSource,
                indication = null
            )
            .defaultMinSize(minHeight = BottomBarItemMinSize)
    ) {
        val iconColor = colors.iconColor(selected, enabled)
        val indicatorColor by animateColorAsState(
            colors.indicatorColor(selected, enabled),
            BottomBarIndicatorAnimSpec
        )
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .indication(interactionSource, ripple(bounded = false, color = iconColor))
                .background(indicatorColor)
                .padding(BottomBarIndicatorIconPadding),
            content = {
                CompositionLocalProvider(LocalContentColor provides iconColor, icon)
            }
        )

        /*Label*/
        val labelColor by animateColorAsState(colors.textColor(selected, enabled))
        CompositionLocalProvider(LocalContentColor provides labelColor) {
            ProvideTextStyle(AppTheme.typography.label3, label)
        }
    }
}

/** @see BottomNavigationItem */
@OptIn(ExperimentalThemeApi::class)
@Composable
fun SideNavigationItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    label: @Composable (() -> Unit),
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    colors: NavigationItemColors = NavigationItemDefaults.colors()
) {
    // This item necessitates the presence of both an icon and a label.
    // When selected, both the icon and label are displayed; otherwise, only the icon is shown.
    // The border is visible only when the item is selected; otherwise, it remains hidden.
    // The background color of this item is set to Color.Transparent.
    // TODO(b/113855296): Animate transition between unselected and selected
    val iconColor = colors.iconColor(selected, enabled)
    val indicatorColor = colors.indicatorColor(enabled = enabled, selected = selected)
    Surface(
        selected = selected,
        onClick = onClick,
        shape = RoundedCornerShape(20),
        modifier = modifier.animateContentSize(
            alignment = Alignment.TopCenter,
            animationSpec = SideBarIndicatorAnimSpec
        ),
        color = indicatorColor,
        contentColor = iconColor,
        interactionSource = interactionSource
    ) {
        Column(
            Modifier.padding(PaddingValues(10.dp)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (!selected) IndicatorLabelArrangement else Arrangement.Center,
        ) {
            icon()
            // Label is only shown when not checked.
            // return from here if not checked.
            if (selected) return@Column
            val labelColor by animateColorAsState(colors.textColor(selected, enabled))
            CompositionLocalProvider(LocalContentColor provides labelColor) {
                ProvideTextStyle(AppTheme.typography.label3, label)
            }
        }
    }
}