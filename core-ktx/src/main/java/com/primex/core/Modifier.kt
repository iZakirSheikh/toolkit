package com.primex.core

import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Rotates/transforms the composable by 90 degrees in the clockwise or anti-clockwise direction.
 *
 * @param clockwise `true` if the composable should be rotated clockwise, `false` if anti-clockwise.
 * @return A modified [Modifier] with the rotation applied.
 *
 * Usage example:
 * ```
 * Box(
 *     modifier = Modifier
 *         .fillMaxSize()
 *         .rotate(clockwise = true)
 * ) {
 *     // content here
 * }
 * ```
 *
 * @since 1.0.0
 * @author Zakir Sheikh
 */

@ExperimentalToolkitApi
fun Modifier.rotateTransform(
    clockwise: Boolean
): Modifier {
    val transform = Modifier.layout { measurable, constraints ->
        // as rotation is taking place
        // the height becomes so construct new set of construnts from old one.
        val newConstraints = constraints.copy(
            minWidth = constraints.minHeight,
            minHeight = constraints.minWidth,
            maxHeight = constraints.maxWidth,
            maxWidth = constraints.maxHeight
        )

        // measure measurable with new constraints.
        val placeable = measurable.measure(newConstraints)

        layout(placeable.height, placeable.width) {

            //Compute where to place the measurable.
            // TODO needs to rethink these
            val x = -(placeable.width / 2 - placeable.height / 2)
            val y = -(placeable.height / 2 - placeable.width / 2)

            placeable.place(x = x, y = y)
        }
    }

    val rotated = Modifier.rotate(if (clockwise) 90f else -90f)

    // transform and then apply rotation.
    return this
        .then(transform)
        .then(rotated)
}

/**
 * This modifier acquires focus to this widget as soon as the user clicks on it.
 *
 * @param interactionSource An optional [MutableInteractionSource] that will be used to track user interactions.
 * @param indication An optional [Indication] that will be shown when the widget is clicked.
 * @return A modified [Modifier] that acquires focus when the user clicks on it.
 *
 * Usage example:
 * ```
 * Box(
 *     modifier = Modifier
 *         .size(100.dp)
 *         .acquireFocusOnInteraction()
 * ) {
 *     // content here
 * }
 * ```
 *
 * @since 1.0.0
 * @author [Your name]
 */

@Deprecated("Not good solution.", level = DeprecationLevel.HIDDEN)
fun Modifier.acquireFocusOnInteraction(
    interactionSource: MutableInteractionSource? = null,
    indication: Indication? = null
): Modifier = composed {
    val interaction = interactionSource ?: remember {
        MutableInteractionSource()
    }
    val requester = remember {
        FocusRequester()
    }
    val isFocused by interaction.collectIsFocusedAsState()
    Modifier
        .focusRequester(requester)
        .focusable(true, interactionSource = interaction)
        .clickable(
            enabled = !isFocused,
            indication = indication,
            onClick = { requester.requestFocus() },
            interactionSource = remember {
                MutableInteractionSource()
            }
        )
        .then(this)
}


/**
 * Draws a horizontal divider at the bottom of the composable.
 *
 * A divider is a thin line that groups content in lists and layouts.
 *
 * ![Dividers image](https://developer.android.com/images/reference/androidx/compose/material/dividers.png)
 *
 * @param color The color of the divider line.
 * @param thickness The thickness of the divider line. By default, 1 dp is used. Using [Dp.Hairline]
 *                  will produce a single pixel divider regardless of screen density.
 * @param indent The offset of the line from the left and right edges of the composable. No offset is
 *               used by default.
 * @return A modified [Modifier] that draws a horizontal divider at the bottom of the composable.
 *
 * Usage example:
 * ```
 * Column {
 *     // content here
 *     Spacer(modifier = Modifier.height(16.dp))
 *     Text("Section 1", modifier = Modifier.drawHorizontalDivider(Color.Gray))
 *     // more content here
 * }
 * ```
 *
 * @since 1.0.0
 * @see [drawVerticalDivider]
 * @see [Divider]
 * @see [DividerVertical]
 */

@ExperimentalToolkitApi
fun Modifier.drawHorizontalDivider(
    color: Color,
    thickness: Dp = 1.dp,
    indent: PaddingValues = PaddingValues(0.dp)
) = drawWithContent {

    // calculate the respective indents.
    val startIndentPx = indent.calculateStartPadding(layoutDirection).toPx()
    val endIndentPx = indent.calculateEndPadding(layoutDirection = layoutDirection).toPx()
    val topIndentPx = indent.calculateTopPadding().toPx()
    val bottomIndentPx = indent.calculateBottomPadding().toPx()

    // width and height of the composable UI element.
    val (width, height) = size

    // constructs offsets of the divider.
    val start = Offset(
        startIndentPx,

        // top will get added and bottom will get subtracted.
        height + topIndentPx - bottomIndentPx
    )

    val end = Offset(
        width - endIndentPx,
        height + topIndentPx - bottomIndentPx
    )

    val thicknessPx = thickness.toPx()

    drawContent()
    drawLine(
        color.copy(DividerAlpha),
        strokeWidth = thicknessPx,
        start = start,
        end = end
    )
}

private const val DividerAlpha = 0.12f

/**
 * Draws vertical [Divider] at the end of the composable
 * @see drawHorizontalDivider
 */

@ExperimentalToolkitApi
fun Modifier.drawVerticalDivider(
    color: Color,
    thickness: Dp = 1.dp,
    indent: PaddingValues = PaddingValues(0.dp)
) = drawWithContent {

    // calculate the respective indents.
    val startIndentPx = indent.calculateStartPadding(layoutDirection).toPx()
    val endIndentPx = indent.calculateEndPadding(layoutDirection = layoutDirection).toPx()
    val topIndentPx = indent.calculateTopPadding().toPx()
    val bottomIndentPx = indent.calculateBottomPadding().toPx()

    // width and height of the composable UI element.
    val (width, height) = size

    // constructs offsets of the divider.
    val start = Offset(
        width + startIndentPx,

        // top will get added and bottom will get subtracted.
        topIndentPx
    )

    val end = Offset(
        width - endIndentPx,
        height - bottomIndentPx
    )

    val thicknessPx = thickness.toPx()

    drawContent()
    drawLine(
        color.copy(DividerAlpha),
        strokeWidth = thicknessPx,
        start = start,
        end = end
    )
}

/**
 * Conditionally applies another [Modifier] if the given [condition] is true.
 *
 * @param condition The condition to evaluate.
 * @param other The [Modifier] to apply if the condition is true.
 * @return This [Modifier] if the condition is false, otherwise this [Modifier] combined with [other].
 */
inline fun Modifier.thenIf(condition: Boolean, other: Modifier.() -> Modifier) =
    if (condition) this then Modifier.other() else this