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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.selection.triStateToggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import com.zs.compose.foundation.thenIf

/** Constructs a simple CheckMark */
private fun CheckMark(size: Size) = Path().apply {
    val (width, _) = size
    reset()
    moveTo(width * 0.2f, width * 0.5f)
    lineTo(width * 0.4f, width * 0.7f)
    lineTo(width * 0.8f, width * 0.3f)
}

private val CheckboxDefaultPadding = 2.dp
private val CheckboxSize = 20.dp
private val StrokeWidth = 2.dp
private val RadiusSize = 2.dp

/**
 * Represents the colors used by the three different sections (checkmark, box, and border) of a
 * [Checkbox] or [TriStateCheckbox] in different states.
 *
 * See [CheckboxDefaults.colors] for the default implementation that follows Material
 * specifications.
 */
@Stable
interface CheckboxColors {

    /**
     * Represents the color used by the checkbox, depending on [enabled] and [state].
     *
     * @param state the [ToggleableState] of the checkbox
     * @param enabled whether the checkbox is enabled or not
     */
    @Composable
    fun checkboxColor(enabled: Boolean, state: ToggleableState): State<Color>
}

/** Default [CheckboxColors] implementation. */
@Stable
private class DefaultCheckboxColors(
    private val checked: Color,
    private val unchecked: Color,
    private val indeterminate: Color,
    private val disabled: Color
) : CheckboxColors {
    @Composable
    override fun checkboxColor(enabled: Boolean, state: ToggleableState): State<Color> {
        val color = when {
            !enabled -> disabled
            state == ToggleableState.On -> checked
            state == ToggleableState.Off -> unchecked
            else -> indeterminate
        }
        return animateColorAsState(color, AppTheme.motion.slowEffectsSpec())
    }
}

/** Defaults used in [Checkbox] and [TriStateCheckbox]. */
object CheckboxDefaults {

    /**
     * Creates a [CheckboxColors] that will animate between the provided colors according to the
     * Material specification.
     *
     * @param checkedColor the color that will be used for the border and box when checked
     * @param uncheckedColor color that will be used for the border when unchecked
     * @param disabledColor color that will be used for the box and border when disabled
     */
    @Composable
    fun colors(
        checkedColor: Color = AppTheme.colors.accent,
        uncheckedColor: Color = AppTheme.colors.onBackground.copy(alpha = ContentAlpha.medium),
        indeterminateColor: Color = AppTheme.colors.accent,
        disabledColor: Color = AppTheme.colors.onBackground.copy(alpha = ContentAlpha.disabled),
    ): CheckboxColors {
        return remember(checkedColor, uncheckedColor, disabledColor) {
            DefaultCheckboxColors(
                checked = checkedColor,
                unchecked = uncheckedColor,
                indeterminate = indeterminateColor,
                disabled = disabledColor,
            )
        }
    }
}

@Composable
private fun CheckboxImpl(
    enabled: Boolean,
    value: ToggleableState,
    modifier: Modifier,
    colors: CheckboxColors
) {
    val color by colors.checkboxColor(enabled, value)
    Canvas(
        modifier
            .wrapContentSize(Alignment.Center)
            .requiredSize(CheckboxSize)
    ) {
        val strokeWidthPx = StrokeWidth.toPx()
        // Border (Present in every state.)
        val checkboxSize = size.width
        drawRoundRect(
            color,
            size = Size(checkboxSize, checkboxSize),
            cornerRadius = CornerRadius(RadiusSize.toPx()),
            style = Stroke(strokeWidthPx)
        )

        when (value) {
            ToggleableState.Off -> return@Canvas
            ToggleableState.On -> drawPath(
                CheckMark(size),
                color = color,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
            // Small box; 1/2 of original size.
            ToggleableState.Indeterminate -> drawRect(
                color,
                size = size * 0.5f,
                topLeft = (size * 0.5f).center
            )
        }
    }
}


@Composable
@NonRestartableComposable
fun TriStateCheckbox(
    state: ToggleableState,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    colors: CheckboxColors = CheckboxDefaults.colors()
) {
    val toggleableModifier = when {
        onClick != null -> Modifier.triStateToggleable(
            state = state,
            onClick = onClick,
            enabled = enabled,
            role = Role.Checkbox,
            interactionSource = interactionSource,
            indication = null
        )

        else -> Modifier
    }
    CheckboxImpl(
        enabled = enabled,
        value = state,
        modifier =
        modifier
            .thenIf(onClick != null) { minimumInteractiveComponentSize() }
            .then(toggleableModifier)
            .padding(CheckboxDefaultPadding),
        colors = colors
    )
}

@Composable
@NonRestartableComposable
fun Checkbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    colors: CheckboxColors = CheckboxDefaults.colors()
) {
    val toggleableModifier = when {
        onCheckedChange != null -> Modifier.toggleable(
            value = checked,
            onValueChange = onCheckedChange,
            enabled = enabled,
            role = Role.Checkbox,
            interactionSource = interactionSource,
            indication = null
        )

        else -> Modifier
    }
    CheckboxImpl(
        enabled = enabled,
        value = ToggleableState(checked),
        modifier =
        modifier
            .thenIf(onCheckedChange != null) { minimumInteractiveComponentSize() }
            .then(toggleableModifier)
            .padding(CheckboxDefaultPadding),
        colors = colors
    )
}