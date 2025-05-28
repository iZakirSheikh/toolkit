/*
 * Copyright 2025 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 28-05-2025.
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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import androidx.compose.ui.util.fastFirst
import androidx.compose.ui.util.fastMaxOfOrNull
import androidx.compose.ui.util.fastSumBy
import com.zs.compose.theme.internal.ActivePressedButtonShape
import com.zs.compose.theme.internal.AnimatedShapeState
import com.zs.compose.theme.internal.rememberAnimatedShape
import com.zs.compose.theme.internal.shapeByInteraction
import com.zs.compose.theme.text.ProvideTextStyle
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

private val DefaultSplitButtonSpacing = 2.0.dp

private const val LeadingButtonLayoutId = "LeadingButton"
private const val TrailingButtonLayoutId = "TrailingButton"
private val DefaultContentPadding = PaddingValues(horizontal = 13.dp)

private val MinSizeModifier = Modifier.defaultMinSize(minWidth = 40.dp, minHeight = 40.dp)

/**
 * The shapes that will be used in buttons. Button will morph between these shapes depending on the
 * [checked] state of the button, assuming all of the shapes are [CornerBasedShape]s.
 *
 * @property Pair.first is the active shape.
 * @property Pair.second is the checked shape.
 */
typealias ActiveCheckedButtonState = ActivePressedButtonShape

/**
 * Note: here second is used as checked state not as pressed state.
 */
@OptIn(ExperimentalThemeApi::class)
@Composable
private fun ActiveCheckedButtonState.shapeByState(checked: Boolean): Shape {
    // copied frm
    // https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/internal/AnimatedShape.kt;bpv=0
    // If both shapes are not RoundedCornerShape, return the default shape directly.
    if (first !is RoundedCornerShape && second !is RoundedCornerShape) return first
    val shape = if (checked) second else first
    // Get the default animation specifications for Float values from the motion scheme.
    val specs = AppTheme.motionScheme.defaultEffectsSpec<Float>()
    val state =
        remember(specs) { AnimatedShapeState(shape = shape as RoundedCornerShape, spec = specs) }

    val channel = remember { Channel<RoundedCornerShape>(Channel.CONFLATED) }

    SideEffect { channel.trySend(shape as RoundedCornerShape) }
    LaunchedEffect(state, channel) {
        for (target in channel) {
            val newTarget = channel.tryReceive().getOrNull() ?: target
            launch { state.animateToShape(newTarget) }
        }
    }

    return rememberAnimatedShape(state)
}

/**
 * A [SplitButtonLayout] let user define a button group consisting of 2 buttons. The leading [Button]
 * performs a primary action, and the trailing [SplitTrailingButton] performs a secondary action
 * that is contextually related to the primary action.
 *
 * Choose the best split button for an action based on the amount of emphasis it needs. The more
 * important an action is, the higher emphasis its button should be.
 *
 * Use [SplitButtonDefaults.LeadingButton] and [SplitButtonDefaults.TrailingButton] to construct a
 * `FilledSplitButton`. Filled split button is the high-emphasis version of split button. It should
 * be used for emphasizing important or final actions.
 *
 * Use [SplitButtonDefaults.TonalLeadingButton] and [SplitButtonDefaults.TonalTrailingButton] to
 * construct a `tonal SplitButton`. Tonal split button is the medium-emphasis version of split
 * buttons. It's a middle ground between `filled SplitButton` and `outlined SplitButton`
 *
 * Use [SplitButtonDefaults.ElevatedLeadingButton] and [SplitButtonDefaults.ElevatedTrailingButton]
 * to construct a `elevated SplitButton`. Elevated split buttons are essentially `tonal
 * SplitButton`s with a shadow. To prevent shadow creep, only use them when absolutely necessary,
 * such as when the button requires visual separation from patterned container.
 *
 * Use [SplitButtonDefaults.OutlinedLeadingButton] and [SplitButtonDefaults.OutlinedTrailingButton]
 * to construct a `outlined SplitButton`. Outlined split buttons are medium-emphasis buttons. They
 * contain actions that are important, but are not the primary action in an app. Outlined buttons
 * pair well with `filled SplitButton`s to indicate an alternative, secondary action.
 *
 * @param leadingButton the leading button. You can specify your own composable or construct a
 *   [SplitButtonDefaults.LeadingButton]
 * @param trailingButton the trailing button.You can specify your own composable or construct a
 *   [SplitButtonDefaults.TrailingButton]
 * @param modifier the [Modifier] to be applied to this split button.
 * @param spacing The spacing between the [leadingButton] and [trailingButton]
 */
@ExperimentalThemeApi
@Composable
fun SplitButtonLayout(
    leadingButton: @Composable () -> Unit,
    trailingButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    spacing: Dp = DefaultSplitButtonSpacing,
) {
    Layout(
        {
            // Override min component size enforcement to avoid create extra padding internally
            // Enforce it on the parent instead
            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                Box(
                    modifier = Modifier.layoutId(LeadingButtonLayoutId),
                    contentAlignment = Alignment.Center,
                    content = { leadingButton() },
                )
                Box(
                    modifier = Modifier.layoutId(TrailingButtonLayoutId),
                    contentAlignment = Alignment.Center,
                    content = { trailingButton() },
                )
            }
        },
        modifier.minimumInteractiveComponentSize(),
        measurePolicy = { measurables, constraints ->
            val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

            val leadingButtonPlaceable =
                measurables
                    .fastFirst { it.layoutId == LeadingButtonLayoutId }
                    .measure(looseConstraints)

            val trailingButtonPlaceable =
                measurables
                    .fastFirst { it.layoutId == TrailingButtonLayoutId }
                    .measure(
                        looseConstraints
                            .offset(
                                horizontal = -(leadingButtonPlaceable.width + spacing.roundToPx())
                            )
                            .copy(
                                minHeight = leadingButtonPlaceable.height,
                                maxHeight = leadingButtonPlaceable.height,
                            )
                    )

            val placeables = listOf(leadingButtonPlaceable, trailingButtonPlaceable)

            val contentWidth = placeables.fastSumBy { it.width } + spacing.roundToPx()
            val contentHeight = placeables.fastMaxOfOrNull { it.height } ?: 0

            val width = constraints.constrainWidth(contentWidth)
            val height = constraints.constrainHeight(contentHeight)

            layout(width, height) {
                leadingButtonPlaceable.placeRelative(0, 0)
                trailingButtonPlaceable.placeRelative(
                    x = leadingButtonPlaceable.width + spacing.roundToPx(),
                    y = 0,
                )
            }
        },
    )
}

/**
 * Constructs a `trailing` button that has the same visual as a [Button].
 *
 * To create a `tonal`, `outlined`, or `elevated` version, the default value of [Button] params
 * can be passed in. For example, [ElevatedButton].
 *
 * The default text style for internal [Text] components will be set to [Typography.label1].
 *
 * @param onClick called when the button is clicked
 * @param modifier the [Modifier] to be applied to this button.
 * @param enabled controls the enabled state of the split button. When `false`, this component
 *   will not respond to user input, and it will appear visually disabled and disabled to
 *   accessibility services.
 * @param shapes the [ActivePressedButtonShape] that the trailing button will morph between depending
 *   on the user's interaction with the button.
 * @param colors [ButtonColors] that will be used to resolve the colors for this button in
 *   different states. See [ButtonDefaults.buttonColors].
 * @param elevation [ButtonElevation] used to resolve the elevation for this button in different
 *   states. This controls the size of the shadow below the button. See
 *   [ButtonElevation.elevation].
 * @param border the border to draw around the container of this button contentPadding the
 *   spacing values to apply internally between the container and the content
 * @param contentPadding the spacing values to apply internally between the container and the
 *   content
 * @param interactionSource an optional hoisted [MutableInteractionSource] for observing and
 *   emitting [Interaction]s for this button. You can use this to change the button's appearance
 *   or preview the button in different states. Note that if `null` is provided, interactions
 *   will still happen internally.
 * @param content the content to be placed in the button
 */
@ExperimentalThemeApi
@Composable
fun TrailingSplitButton(
    onClick: () -> Unit,
    shapes: ActivePressedButtonShape,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = null,
    contentPadding: PaddingValues = DefaultContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val interactionSource = interactionSource ?: remember(::MutableInteractionSource)
    Surface(
        onClick = onClick,
        modifier = modifier.semantics { role = Role.Button },
        enabled = enabled,
        shape = shapes.shapeByInteraction(interactionSource),
        color = colors.backgroundColor(enabled),
        contentColor = colors.contentColor(enabled),
        elevation = elevation?.elevation(enabled, interactionSource)?.value ?: 0.dp,
        border = border,
        interactionSource = interactionSource,
    ) {
        ProvideTextStyle(
            AppTheme.typography.label1,
        ) {
            Box(
                MinSizeModifier then Modifier.padding(contentPadding),
                contentAlignment = Alignment.Center,
                content = content,
            )
        }
    }
}

/**
 * Creates a `trailing` button that has the same visual as a [Button]. When [checked] is updated
 * from `false` to `true`, the buttons corners will morph to `full` by default. Pressed shape
 * and checked shape can be customized via [shapes] param.
 *
 * To create a `tonal`, `outlined`, or `elevated` version, the default value of [Button] params
 * can be passed in. For example, [ElevatedButton].
 *
 * The default text style for internal [Text] components will be set to [Typography.label1].
 *
 * @param checked indicates whether the button is checked. This will trigger the corner morphing
 *   animation to reflect the updated state.
 * @param onCheckedChange called when the button is clicked
 * @param modifier the [Modifier] to be applied to this button.
 * @param enabled controls the enabled state of the split button. When `false`, this component
 *   will not respond to user input, and it will appear visually disabled and disabled to
 *   accessibility services.
 * @param shapes the [SplitButtonShapes] that the trailing button will morph between depending
 *   on the user's interaction with the button.
 * @param colors [ButtonColors] that will be used to resolve the colors for this button in
 *   different states. See [ButtonDefaults.buttonColors].
 * @param elevation [ButtonElevation] used to resolve the elevation for this button in different
 *   states. This controls the size of the shadow below the button. See
 *   [ButtonElevation.shadowElevation].
 * @param border the border to draw around the container of this button contentPadding the
 *   spacing values to apply internally between the container and the content
 * @param contentPadding the spacing values to apply internally between the container and the
 *   content
 * @param interactionSource an optional hoisted [MutableInteractionSource] for observing and
 *   emitting [Interaction]s for this button. You can use this to change the button's appearance
 *   or preview the button in different states. Note that if `null` is provided, interactions
 *   will still happen internally.
 * @param content the content to be placed in the button
 */
@Composable
@ExperimentalThemeApi
fun TrailingSplitButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    shapes: ActiveCheckedButtonState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = null,
    contentPadding: PaddingValues = DefaultContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember(::MutableInteractionSource)
    Surface(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier.semantics { role = Role.Button },
        enabled = enabled,
        shape = shapes.shapeByState(checked),
        color = colors.backgroundColor(enabled),
        contentColor = colors.contentColor(enabled),
        elevation = elevation?.elevation(enabled, interactionSource)?.value ?: 0.dp,
        border = border,
        interactionSource = interactionSource,
    ) {
        ProvideTextStyle(
            AppTheme.typography.label1,
        ) {
            Box(
                MinSizeModifier then Modifier.padding(contentPadding),
                contentAlignment = Alignment.Center,
                content = content,
            )
        }
    }
}

/**
 * @see TrailingSplitButton
 */
@ExperimentalThemeApi
@Composable
@NonRestartableComposable
fun TrailingSplitTonalButton(
    onClick: () -> Unit,
    shapes: ActivePressedButtonShape,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.filledTonalButtonColors(),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = null,
    contentPadding: PaddingValues = DefaultContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable BoxScope.() -> Unit,
) = TrailingSplitButton(
    onClick = onClick,
    shapes = shapes,
    modifier = modifier,
    enabled = enabled,
    colors = colors,
    elevation = elevation,
    border = border,
    contentPadding = contentPadding,
    interactionSource = interactionSource,
    content = content
)

/**
 * @see TrailingSplitButton
 */
@ExperimentalThemeApi
@Composable
@NonRestartableComposable
fun TrailingSplitOutlinedButton(
    onClick: () -> Unit,
    shapes: ActivePressedButtonShape,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = ButtonDefaults.outlinedBorder,
    contentPadding: PaddingValues = DefaultContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable BoxScope.() -> Unit,
) = TrailingSplitButton(
    onClick = onClick,
    shapes = shapes,
    modifier = modifier,
    enabled = enabled,
    colors = colors,
    elevation = elevation,
    border = border,
    contentPadding = contentPadding,
    interactionSource = interactionSource,
    content = content
)

/**
 * @see TrailingSplitButton
 */
@ExperimentalThemeApi
@Composable
@NonRestartableComposable
fun TrailingSplitElevatedButton(
    onClick: () -> Unit,
    shapes: ActivePressedButtonShape,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.elevatedButtonColors(),
    elevation: ButtonElevation? = ButtonDefaults.elevatedButtonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = DefaultContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable BoxScope.() -> Unit,
) = TrailingSplitButton(
    onClick = onClick,
    shapes = shapes,
    modifier = modifier,
    enabled = enabled,
    colors = colors,
    elevation = elevation,
    border = border,
    contentPadding = contentPadding,
    interactionSource = interactionSource,
    content = content
)
