package com.primex.core

import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


/**
 * Overlays the content with the gradient provided by the [Brush].
 *
 * @param colors The list of colors to use in the gradient.
 * @param provider The function to provide the [Brush] to use in the gradient, based on the [Size] of the content.
 * @return A modified [Modifier] with the gradient effect applied.
 *
 * Usage example:
 * ```
 * Box(
 *     modifier = Modifier
 *         .fillMaxSize()
 *         .gradient(
 *             colors = listOf(Color.Red, Color.Yellow, Color.Green),
 *             provider = { size ->
 *                 Brush.linearGradient(
 *                     colors = colors,
 *                     start = Offset.Zero,
 *                     end = Offset(size.width, size.height)
 *                 )
 *             }
 *         )
 * ) {
 *     // content here
 * }
 * ```
 *
 * @since 1.0.0
 * @author [Your name]
 */
@Deprecated("Use the official ones")
inline fun Modifier.gradient(
    colors: List<Color>,
    crossinline provider: (Size) -> Brush
): Modifier = composed {
    var size by rememberState(initial = Size.Zero)
    val gradient = remember(colors, size) { provider(size) }
    drawWithContent {
        size = this.size
        drawContent()
        drawRect(brush = gradient)
    }
}

/**
 * Adds a gradient effect to the content with the provided [Brush].
 *
 * @param vertical `true` if the gradient should be applied vertically, `false` if horizontally.
 * @param colors The colors to use in the [Brush]. The first color is the top color if `vertical`
 * is `true`, or the left color if `vertical` is `false`. The last color is the bottom color if
 * `vertical` is `true`, or the right color if `vertical` is `false`.
 *
 * Usage example:
 * ```
 * Box(
 *     modifier = Modifier
 *         .fillMaxSize()
 *         .gradient(
 *             vertical = true,
 *             colors = listOf(Color.Transparent, Color.Red, Color.Black)
 *         )
 * ) {
 *     // content here
 * }
 * ```
 *
 * @since 1.0.0
 * @author Zakir Sheikh
 *
 * @return A modified [Modifier] with the gradient effect applied.
 */

@Deprecated("Use the official ones")
fun Modifier.gradient(
    vertical: Boolean,
    colors: List<Color> = listOf(
        Color.Transparent,
        Color.Black,
    ),
) = gradient(colors) { size ->
    if (vertical)
        Brush.verticalGradient(
            colors = colors,
            startY = 0f,
            endY = size.height
        )
    else
        Brush.horizontalGradient(
            colors = colors,
            startX = 0f,
            endX = size.width
        )
}

/**
 * Adds a radial gradient effect to the content with the provided [Brush].
 *
 * @param radius The radius of the radial gradient.
 * @param colors The colors to use in the [Brush]. The first color is the center color, and the last color is the outer color.
 * @param center The center of the radial gradient. If unspecified, the center is the center of the content.
 * @param tileMode The tile mode of the radial gradient.
 * @return A modified [Modifier] with the radial gradient effect applied.
 *
 * Usage example:
 * ```
 * Box(
 *     modifier = Modifier
 *         .fillMaxSize()
 *         .gradient(
 *             radius = 200f,
 *             colors = listOf(Color.Red, Color.Yellow, Color.Green),
 *             center = Offset(0.5f, 0.5f),
 *             tileMode = TileMode.Repeated
 *         )
 * ) {
 *     // content here
 * }
 * ```
 *
 * @since 1.0.0
 * @see [Brush.radialGradient]
 */

@Deprecated("Use the official ones")
fun Modifier.gradient(
    radius: Float,
    colors: List<Color> = listOf(
        Color.Transparent,
        Color.Black
    ),
    center: Offset = Offset.Unspecified,
    tileMode: TileMode = TileMode.Clamp
) = gradient(colors) {
    Brush.radialGradient(
        colors = colors,
        center = center,
        radius = radius.coerceAtLeast(1f),
        tileMode = tileMode
    )
}

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


@Deprecated(
    "Use the new function name 'rotateTransform' instead.",
    ReplaceWith("rotateTransform(clockwise)")
)
@ExperimentalToolkitApi
inline fun Modifier.rotate(clockwise: Boolean) = rotateTransform(clockwise)

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

@Deprecated("Not good solution.")
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
 * Adds padding to the composable in a shorthand way.
 *
 * @param horizontal The horizontal padding value.
 * @param top The top padding value.
 * @param bottom The bottom padding value.
 * @return A modified [Modifier] instance with added padding.
 *
 * Usage example:
 * ```
 * Box(
 *     modifier = Modifier.padding(horizontal = 16.dp, top = 8.dp, bottom = 8.dp)
 * ) {
 *     Text(text = "Hello world")
 * }
 * ```
 *
 * @since 1.0.0
 */
@Stable
@Deprecated("Don't use it as it cases confusion.", level = DeprecationLevel.HIDDEN)
fun Modifier.padding(horizontal: Dp, top: Dp = 0.dp, bottom: Dp = 0.dp) =
    this.then(
        Modifier.padding(
            start = horizontal,
            end = horizontal,
            top = top,
            bottom = bottom
        )
    )