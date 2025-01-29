/*
 * Copyright 2025 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 29-01-2025.
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

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.zs.compose.foundation.thenIf

// Impl from material2
// inspired by fluent2 switch

private val TrackStrokeWidth = 1.dp
private val DefaultSwitchPadding = 2.dp

private val SwitchWidth = 43.dp
private val SwitchHeight = 22.dp

@Stable
interface SwitchColors {

    /**
     * Represents the color used for the switch's thumb, depending on [enabled] and [checked].
     *
     * @param enabled whether the [Switch] is enabled or not
     * @param checked whether the [Switch] is checked or not
     */
    @Composable
    fun thumbColor(enabled: Boolean, checked: Boolean): State<Color>

    /**
     * Represents the color used for the switch's track, depending on [enabled] and [checked].
     *
     * @param enabled whether the [Switch] is enabled or not
     * @param checked whether the [Switch] is checked or not
     */
    @Composable
    fun trackColor(enabled: Boolean, checked: Boolean): State<Color>
}

/** Default [SwitchColors] implementation. */
@Immutable
private class DefaultSwitchColors(
    private val checkedThumbColor: Color,
    private val checkedTrackColor: Color,
    private val uncheckedThumbColor: Color,
    private val uncheckedTrackColor: Color,
    private val disabledCheckedThumbColor: Color,
    private val disabledCheckedTrackColor: Color,
    private val disabledUncheckedThumbColor: Color,
    private val disabledUncheckedTrackColor: Color
) : SwitchColors {
    @Composable
    override fun thumbColor(enabled: Boolean, checked: Boolean): State<Color> {
        return animateColorAsState(
            if (enabled) {
                if (checked) checkedThumbColor else uncheckedThumbColor
            } else {
                if (checked) disabledCheckedThumbColor else disabledUncheckedThumbColor
            },
            AppTheme.motion.slowEffectsSpec()
        )
    }

    @Composable
    override fun trackColor(enabled: Boolean, checked: Boolean): State<Color> {
        return animateColorAsState(
            if (enabled) {
                if (checked) checkedTrackColor else uncheckedTrackColor
            } else {
                if (checked) disabledCheckedTrackColor else disabledUncheckedTrackColor
            },
            AppTheme.motion.slowEffectsSpec()
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as DefaultSwitchColors

        if (checkedThumbColor != other.checkedThumbColor) return false
        if (checkedTrackColor != other.checkedTrackColor) return false
        if (uncheckedThumbColor != other.uncheckedThumbColor) return false
        if (uncheckedTrackColor != other.uncheckedTrackColor) return false
        if (disabledCheckedThumbColor != other.disabledCheckedThumbColor) return false
        if (disabledCheckedTrackColor != other.disabledCheckedTrackColor) return false
        if (disabledUncheckedThumbColor != other.disabledUncheckedThumbColor) return false
        if (disabledUncheckedTrackColor != other.disabledUncheckedTrackColor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = checkedThumbColor.hashCode()
        result = 31 * result + checkedTrackColor.hashCode()
        result = 31 * result + uncheckedThumbColor.hashCode()
        result = 31 * result + uncheckedTrackColor.hashCode()
        result = 31 * result + disabledCheckedThumbColor.hashCode()
        result = 31 * result + disabledCheckedTrackColor.hashCode()
        result = 31 * result + disabledUncheckedThumbColor.hashCode()
        result = 31 * result + disabledUncheckedTrackColor.hashCode()
        return result
    }
}

/** Contains the default values used by [Switch] */
object SwitchDefaults {
    /**
     * Creates a [SwitchColors] that represents the different colors used in a [Switch] in different
     * states.
     *
     * @param checkedThumbColor the color used for the thumb when enabled and checked
     * @param checkedTrackColor the color used for the track when enabled and checked
     * @param checkedTrackAlpha the alpha applied to [checkedTrackColor] and
     *   [disabledCheckedTrackColor]
     * @param uncheckedThumbColor the color used for the thumb when enabled and unchecked
     * @param uncheckedTrackColor the color used for the track when enabled and unchecked
     * @param uncheckedTrackAlpha the alpha applied to [uncheckedTrackColor] and
     *   [disabledUncheckedTrackColor]
     * @param disabledCheckedThumbColor the color used for the thumb when disabled and checked
     * @param disabledCheckedTrackColor the color used for the track when disabled and checked
     * @param disabledUncheckedThumbColor the color used for the thumb when disabled and unchecked
     * @param disabledUncheckedTrackColor the color used for the track when disabled and unchecked
     */
    @Composable
    fun colors(
        checkedThumbColor: Color = AppTheme.colors.onAccent,
        checkedTrackColor: Color = AppTheme.colors.accent,
        uncheckedThumbColor: Color = AppTheme.colors.onBackground.copy(ContentAlpha.medium),
        uncheckedTrackStrokeColor: Color = uncheckedThumbColor,
        disabledCheckedThumbColor: Color = AppTheme.colors.onBackground.copy(ContentAlpha.disabled),
        disabledCheckedTrackColor: Color =
            checkedTrackColor
                .copy(alpha = ContentAlpha.disabled)
                .compositeOver(AppTheme.colors.background),
        disabledUncheckedThumbColor: Color =
            uncheckedThumbColor
                .copy(alpha = ContentAlpha.disabled)
                .compositeOver(AppTheme.colors.background),
        disabledUncheckedTrackStrokeColor: Color =
            uncheckedTrackStrokeColor
                .copy(alpha = ContentAlpha.disabled)
                .compositeOver(AppTheme.colors.background)
    ): SwitchColors = DefaultSwitchColors(
        checkedThumbColor = checkedThumbColor,
        checkedTrackColor = checkedTrackColor,
        uncheckedThumbColor = uncheckedThumbColor,
        uncheckedTrackColor = uncheckedTrackStrokeColor,
        disabledCheckedThumbColor = disabledCheckedThumbColor,
        disabledCheckedTrackColor = disabledCheckedTrackColor,
        disabledUncheckedThumbColor = disabledUncheckedThumbColor,
        disabledUncheckedTrackColor = disabledUncheckedTrackStrokeColor
    )
}

/**
 * [Fluent Design Switch](https://fluent2.microsoft.design/components/web/react/switch/usage)
 *
 * Switches toggle the state of a single item on or off.
 *
 * [Switch image](https://fluent2.microsoft.design/_image?href=https%3A%2F%2Ffluent2websitecdn.azureedge.net%2Fcdn%2Fswitch1.B_urv6Np.webp&f=webp)
 *
 *
 *
 * @param checked whether or not this switch is checked
 * @param onCheckedChange called when this switch is clicked. If `null`, then this switch will not
 *   be interactable, unless something else handles its input events and updates its state.
 * @param modifier the [Modifier] to be applied to this switch
 * @param enabled controls the enabled state of this switch. When `false`, this component will not
 *   respond to user input, and it will appear visually disabled and disabled to accessibility
 *   services.
 * @param colors [SwitchColors] that will be used to resolve the colors used for this switch in
 *   different states. See [SwitchDefaults.colors].
 * @param interactionSource an optional hoisted [MutableInteractionSource] for observing and
 *   emitting [Interaction]s for this switch. You can use this to change the switch's appearance or
 *   preview the switch in different states. Note that if `null` is provided, interactions will
 *   still happen internally.
 */
@Composable
fun Switch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    colors: SwitchColors = SwitchDefaults.colors()
) {
    val trackColor by colors.trackColor(enabled, checked)
    val thumbColor by colors.thumbColor(enabled, checked)
    // instead of official impl we are using toggleable modifier and use animate state to
    // animate the position of switch thumb
    val checkedModifier = when {
        onCheckedChange != null -> Modifier.toggleable(
            value = checked,
            onValueChange = onCheckedChange,
            enabled = enabled,
            role = Role.RadioButton,
            interactionSource = interactionSource,
            indication = null
        )

        else -> Modifier
    }
    // animates the position
    val position by animateFloatAsState(if (checked) 1f else 0f, AppTheme.motion.slowSpatialSpec())
    Canvas(
        modifier
            .thenIf(onCheckedChange != null) { minimumInteractiveComponentSize() }
            .then(checkedModifier)
            .wrapContentSize(Alignment.Center)
            .padding(DefaultSwitchPadding)
            .requiredSize(43.dp, 22.dp)
    ) {
        val stroke = TrackStrokeWidth.toPx()
        when (checked) {
            false -> drawRoundRect(
                color = trackColor,
                cornerRadius = CornerRadius(size.maxDimension),
                style = Stroke(stroke),
                size = size
            )

            else -> drawRoundRect(
                color = trackColor,
                cornerRadius = CornerRadius(size.maxDimension),
                style = Fill,
                size = size
            )
        }
        val radius = size.minDimension / 2.5f
        drawCircle(
            color = thumbColor,
            radius = radius,
            center = Offset(
                x = (position * size.width).coerceIn(
                    radius + stroke,
                    size.width - (radius + stroke)
                ),
                y = center.y
            )
        )
    }
}

