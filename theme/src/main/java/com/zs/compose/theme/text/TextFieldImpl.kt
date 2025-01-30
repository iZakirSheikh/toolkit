/*
 * Copyright 2025 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 30-01-2025.
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

package com.zs.compose.theme.text

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposableOpenTarget
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.LayoutIdParentData
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.lerp
import androidx.compose.ui.unit.dp
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.ExperimentalThemeApi
import com.zs.compose.theme.LocalContentAlpha
import com.zs.compose.theme.LocalContentColor

internal enum class TextFieldType {
    Filled,
    Outlined
}

/** Implementation of the [TextField] and [OutlinedTextField] */
@OptIn(ExperimentalThemeApi::class)
@Composable
internal fun CommonDecorationBox(
    type: TextFieldType,
    value: String,
    innerTextField: @Composable () -> Unit,
    visualTransformation: VisualTransformation,
    label: @Composable (() -> Unit)?,
    placeholder: @Composable (() -> Unit)?,
    leadingIcon: @Composable (() -> Unit)?,
    trailingIcon: @Composable (() -> Unit)?,
    singleLine: Boolean,
    enabled: Boolean,
    isError: Boolean,
    interactionSource: InteractionSource,
    contentPadding: PaddingValues,
    shape: Shape,
    colors: TextFieldColors,
    border: @Composable (() -> Unit)?,
) {
    val transformedText =
        remember(value, visualTransformation) {
            visualTransformation.filter(AnnotatedString(value))
        }
            .text
            .text

    val isFocused = interactionSource.collectIsFocusedAsState().value
    val inputState =
        when {
            isFocused -> InputPhase.Focused
            transformedText.isEmpty() -> InputPhase.UnfocusedEmpty
            else -> InputPhase.UnfocusedNotEmpty
        }

    val labelColor: @Composable (InputPhase) -> Color = {
        colors
            .labelColor(
                enabled,
                // if label is used as a placeholder (aka not as a small header
                // at the top), we don't use an error color
                if (it == InputPhase.UnfocusedEmpty) false else isError,
                interactionSource
            )
            .value
    }

    val typography = AppTheme.typography
    val subtitle1 = typography.title2
    val caption = typography.label3
    val shouldOverrideTextStyleColor =
        (subtitle1.color == Color.Unspecified && caption.color != Color.Unspecified) ||
                (subtitle1.color != Color.Unspecified && caption.color == Color.Unspecified)

    TextFieldTransitionScope.Transition(
        inputState = inputState,
        focusedTextStyleColor =
        with(AppTheme.typography.label3.color) {
            if (shouldOverrideTextStyleColor) this.takeOrElse { labelColor(inputState) }
            else this
        },
        unfocusedTextStyleColor =
        with(AppTheme.typography.title2.color) {
            if (shouldOverrideTextStyleColor) this.takeOrElse { labelColor(inputState) }
            else this
        },
        contentColor = labelColor,
        showLabel = label != null
    ) { labelProgress, labelTextStyleColor, labelContentColor, placeholderAlphaProgress ->
        val decoratedLabel: @Composable (() -> Unit)? =
            label?.let {
                @Composable {
                    val labelTextStyle =
                        lerp(
                            AppTheme.typography.title2,
                            AppTheme.typography.label3,
                            labelProgress
                        )
                            .let {
                                if (shouldOverrideTextStyleColor)
                                    it.copy(color = labelTextStyleColor)
                                else it
                            }
                    Decoration(labelContentColor, labelTextStyle, null, it)
                }
            }

        // Transparent components interfere with Talkback (b/261061240), so if the placeholder has
        // alpha == 0, we set the component to null instead.
        val decoratedPlaceholder: @Composable ((Modifier) -> Unit)? =
            if (placeholder != null && transformedText.isEmpty() && placeholderAlphaProgress > 0f) {
                @Composable { modifier ->
                    Box(modifier.alpha(placeholderAlphaProgress)) {
                        Decoration(
                            contentColor = colors.placeholderColor(enabled).value,
                            typography = AppTheme.typography.title2,
                            content = placeholder
                        )
                    }
                }
            } else null

        val leadingIconColor = colors.leadingIconColor(enabled, isError, interactionSource).value
        val decoratedLeading: @Composable (() -> Unit)? =
            leadingIcon?.let {
                @Composable { Decoration(contentColor = leadingIconColor, content = it) }
            }

        val trailingIconColor = colors.trailingIconColor(enabled, isError, interactionSource).value
        val decoratedTrailing: @Composable (() -> Unit)? =
            trailingIcon?.let {
                @Composable { Decoration(contentColor = trailingIconColor, content = it) }
            }

        val backgroundModifier = Modifier.background(colors.backgroundColor(enabled).value, shape)

        when (type) {
            TextFieldType.Filled -> {
                TextFieldLayout(
                    modifier = backgroundModifier,
                    textField = innerTextField,
                    placeholder = decoratedPlaceholder,
                    label = decoratedLabel,
                    leading = decoratedLeading,
                    trailing = decoratedTrailing,
                    singleLine = singleLine,
                    animationProgress = labelProgress,
                    paddingValues = contentPadding
                )
            }
            TextFieldType.Outlined -> {
                // Outlined cutout
                val labelSize = remember { mutableStateOf(Size.Zero) }
                val drawBorder: @Composable () -> Unit = {
                    Box(
                        Modifier.layoutId(BorderId).outlineCutout(labelSize.value, contentPadding),
                        propagateMinConstraints = true
                    ) {
                        border?.invoke()
                    }
                }

                OutlinedTextFieldLayout(
                    modifier = backgroundModifier,
                    textField = innerTextField,
                    placeholder = decoratedPlaceholder,
                    label = decoratedLabel,
                    leading = decoratedLeading,
                    trailing = decoratedTrailing,
                    singleLine = singleLine,
                    onLabelMeasured = {
                        val labelWidth = it.width * labelProgress
                        val labelHeight = it.height * labelProgress
                        if (
                            labelSize.value.width != labelWidth ||
                            labelSize.value.height != labelHeight
                        ) {
                            labelSize.value = Size(labelWidth, labelHeight)
                        }
                    },
                    animationProgress = labelProgress,
                    border = drawBorder,
                    paddingValues = contentPadding
                )
            }
        }
    }
}

/** Set content color, typography and emphasis for [content] composable */
@Composable
@ComposableOpenTarget(index = 0)
internal fun Decoration(
    contentColor: Color,
    typography: TextStyle? = null,
    contentAlpha: Float? = null,
    content: @Composable @ComposableOpenTarget(index = 0) () -> Unit
) {
    val colorAndEmphasis: @Composable () -> Unit =
        @Composable {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                if (contentAlpha != null) {
                    CompositionLocalProvider(
                        LocalContentAlpha provides contentAlpha,
                        content = content
                    )
                } else {
                    CompositionLocalProvider(
                        LocalContentAlpha provides contentColor.alpha,
                        content = content
                    )
                }
            }
        }
    if (typography != null) ProvideTextStyle(typography, colorAndEmphasis) else colorAndEmphasis()
}

// Developers need to handle invalid input manually. But since we don't provide an error message
// slot API, we can set the default error message in case developers forget about it.
internal fun Modifier.defaultErrorSemantics(
    isError: Boolean,
    defaultErrorMessage: String,
): Modifier = if (isError) semantics { error(defaultErrorMessage) } else this

internal fun widthOrZero(placeable: Placeable?) = placeable?.width ?: 0

internal fun heightOrZero(placeable: Placeable?) = placeable?.height ?: 0

private object TextFieldTransitionScope {
    @Composable
    fun Transition(
        inputState: InputPhase,
        focusedTextStyleColor: Color,
        unfocusedTextStyleColor: Color,
        contentColor: @Composable (InputPhase) -> Color,
        showLabel: Boolean,
        content:
        @Composable
            (
            labelProgress: Float,
            labelTextStyleColor: Color,
            labelContentColor: Color,
            placeholderOpacity: Float
        ) -> Unit
    ) {
        // Transitions from/to InputPhase.Focused are the most critical in the transition below.
        // UnfocusedEmpty <-> UnfocusedNotEmpty are needed when a single state is used to control
        // multiple text fields.
        val transition = updateTransition(inputState, label = "TextFieldInputState")

        val labelProgress by
        transition.animateFloat(
            label = "LabelProgress",
            transitionSpec = { tween(durationMillis = AnimationDuration) }
        ) {
            when (it) {
                InputPhase.Focused -> 1f
                InputPhase.UnfocusedEmpty -> 0f
                InputPhase.UnfocusedNotEmpty -> 1f
            }
        }

        val placeholderOpacity by
        transition.animateFloat(
            label = "PlaceholderOpacity",
            transitionSpec = {
                if (InputPhase.Focused isTransitioningTo InputPhase.UnfocusedEmpty) {
                    tween(
                        durationMillis = PlaceholderAnimationDelayOrDuration,
                        easing = LinearEasing
                    )
                } else if (
                    InputPhase.UnfocusedEmpty isTransitioningTo InputPhase.Focused ||
                    InputPhase.UnfocusedNotEmpty isTransitioningTo InputPhase.UnfocusedEmpty
                ) {
                    tween(
                        durationMillis = PlaceholderAnimationDuration,
                        delayMillis = PlaceholderAnimationDelayOrDuration,
                        easing = LinearEasing
                    )
                } else {
                    spring()
                }
            }
        ) {
            when (it) {
                InputPhase.Focused -> 1f
                InputPhase.UnfocusedEmpty -> if (showLabel) 0f else 1f
                InputPhase.UnfocusedNotEmpty -> 0f
            }
        }

        val labelTextStyleColor by
        transition.animateColor(
            transitionSpec = { tween(durationMillis = AnimationDuration) },
            label = "LabelTextStyleColor"
        ) {
            when (it) {
                InputPhase.Focused -> focusedTextStyleColor
                else -> unfocusedTextStyleColor
            }
        }

        val labelContentColor by
        transition.animateColor(
            transitionSpec = { tween(durationMillis = AnimationDuration) },
            label = "LabelContentColor",
            targetValueByState = contentColor
        )

        content(labelProgress, labelTextStyleColor, labelContentColor, placeholderOpacity)
    }
}

/** An internal state used to animate a label and an indicator. */
private enum class InputPhase {
    // Text field is focused
    Focused,

    // Text field is not focused and input text is empty
    UnfocusedEmpty,

    // Text field is not focused but input text is not empty
    UnfocusedNotEmpty
}

internal val IntrinsicMeasurable.layoutId: Any?
    get() = (parentData as? LayoutIdParentData)?.layoutId

internal const val TextFieldId = "TextField"
internal const val PlaceholderId = "Hint"
internal const val LabelId = "Label"
internal const val LeadingId = "Leading"
internal const val TrailingId = "Trailing"

internal const val AnimationDuration = 150
private const val PlaceholderAnimationDuration = 83
private const val PlaceholderAnimationDelayOrDuration = 67

internal val TextFieldPadding = 16.dp
internal val HorizontalIconPadding = 12.dp