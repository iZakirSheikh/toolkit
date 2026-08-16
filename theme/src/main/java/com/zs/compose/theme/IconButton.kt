/*
 * Copyright 2025 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 28-01-2025.
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

@file:OptIn(ExperimentalThemeApi::class)

package com.zs.compose.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

// Default radius of an unbounded ripple in an IconButton
private val RippleRadius get() = 24.dp

/**
 * Icon buttons help people take minor actions and move through screens.
 *
 * @param onClick called when this icon button is clicked
 * @param modifier the [Modifier] to be applied to this icon button
 * @param enabled controls the enabled state of this icon button. When `false`, this component will
 * not respond to user input, and it will appear visually disabled and disabled to accessibility services.
 * @param interactionSource the [MutableInteractionSource] representing the stream of [Interaction]s
 * for this icon button. You can create and pass in your own `remember`ed instance to observe
 * Interactions and customize the appearance / behavior of this icon button in different states.
 * @param content the content of this icon button, typically an [Icon]
 */
@Composable
fun IconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        interactionSource = interactionSource,
        content = content,
        color = Color.Unspecified,
        shape = CircleShape,
        indication = ripple(bounded = false, radius = RippleRadius),
        contentColor = when {
            enabled -> LocalContentColor.current
            else -> LocalContentColor.current.copy(
                ContentAlpha.disabled
            )
        }
    )
}


/**
 * @see IconButton
 */
@Composable
inline fun IconButton(
    icon: ImageVector,
    contentDescription: String?,
    noinline onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = Color.Unspecified,
    interactionSource: MutableInteractionSource? = null
) = IconButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    interactionSource = interactionSource, content = {
    Icon(icon, contentDescription, tint = tint.takeOrElse { LocalContentColor.current })
}
)

/**
 * A toggle button that displays an icon and can be toggled between two states (checked and unchecked).
 *
 * @param checked whether this toggle button is currently checked
 * @param onCheckedChange callback to be invoked when the toggle button is clicked
 * @param modifier the [Modifier] to be applied to this toggle button
 * @param enabled controls the enabled state of this toggle button. When `false`, this component will
 * not respond to user input, and it will appear visually disabled and alpha-reduced.
 * @param interactionSource the [MutableInteractionSource] representing the stream of [Interaction]s
 * for this toggle button. You can create and pass in your own `remember`ed instance to observe
 * Interactions and customize the appearance / behavior of this toggle button in different states.
 * @param content the content to be drawn inside the toggle button, typically an [Icon]
 */
@Composable
fun IconToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit
) {
    Surface(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        interactionSource = interactionSource,
        color = Color.Unspecified,
        shape = CircleShape,
        indication = ripple(bounded = false, radius = RippleRadius),
        contentColor = when {
            enabled -> LocalContentColor.current
            else -> LocalContentColor.current.copy(
                ContentAlpha.disabled
            )
        },
        content = content
    )
}

/**
 * @see IconToggleButton
 */
@Composable
inline fun IconToggleButton(
    checked: Boolean,
    icon: ImageVector,
    contentDescription: String?,
    noinline onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
) = IconToggleButton(
    checked = checked,
    onCheckedChange = onCheckedChange,
    modifier = modifier,
    enabled = enabled,
    interactionSource = interactionSource,
    content = { Icon(icon, contentDescription, tint = tint) }
)

/**
 * Tonal icon buttons are a medium-emphasis alternative to standard icon buttons,
 * using a surface color with a low-opacity background.
 *
 * @param onClick called when this icon button is clicked
 * @param modifier the [Modifier] to be applied to this icon button
 * @param enabled controls the enabled state of this icon button. When `false`, this component will
 * not respond to user input, and it will appear visually disabled and disabled to accessibility services.
 * @param color the color to be used for the background and content. The background will be a
 * transparent version of this color, while the content will use the full opacity.
 */
@Composable
fun TonalIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = Color.Unspecified,
    shape: Shape = CircleShape,
    border: BorderStroke? = null,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        color = color.takeOrElse { LocalContentColor.current }.copy(ContentAlpha.indication),
        shape = shape,
        border = border,
        interactionSource = interactionSource,
        contentColor = color.copy(if (enabled) ContentAlpha.high else ContentAlpha.disabled),
        content = content
    )
}

/**
 * @see TonalIconButton
 */
@NonRestartableComposable
@Composable
fun TonalIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null,
    color: Color = Color.Unspecified,
    shape: Shape = CircleShape,
    border: BorderStroke? = null,
    interactionSource: MutableInteractionSource? = null,
) = TonalIconButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    color = color,
    shape = shape,
    border = border,
    interactionSource = interactionSource,
    content = {Icon(icon, contentDescription)}
)

