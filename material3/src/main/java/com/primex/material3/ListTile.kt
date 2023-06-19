package com.primex.material3

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private const val TAG = "ListTile"

private val LIST_TILE_START_PADDING = 16.dp
private val LIST_TILE_VERTICAL_PADDING = 8.dp
private val LIST_TILE_END_PADDING = 24.dp
private val LIST_ITEM_THREE_LINE_VERTICAL_PADDING = 12.dp

// Icon related defaults.
private val LEADING_CONTENT_END_PADDING = 16.dp

// Trailing related defaults.
private val TRAILING_CONTENT_START_PADDING = 16.dp

private val LIST_ITEM_ONE_LINE_CONTAINER_HEIGHT = 56.0.dp
private val LIST_ITEM_TWO_LINE_CONTAINER_HEIGHT = 72.0.dp
private val LIST_ITEM_THREE_LINE_CONTAINER_HEIGHT = 88.0.dp

private const val ListItemDisabledLabelTextOpacity = 0.3f
private const val ListItemDisabledIconOpacity = 0.38f

/**
 * Advanced Fast/Light version of [ListItem].
 *
 * **Known Issues:**
 *  - The [modifier] pass is not properly clipped with the provided [shape]
 *
 * **Note:**
 *  - The [headline] composable is a must; passing an empty value might result in serious crashes.
 *  - Please pass null if the other composables are not available.
 *  - Additionally, each composable should contain only one child.
 *
 * @param color The color of the background of the composable. Pass different values for different states like "selected," etc.
 * @param enabled This doesn't do anything special except changing the color of the text.
 * @param shape The shape of this list item.
 * @param footer The optional composable to be drawn at the bottom of the list item, between the leading and end sections.
 * @param centerAlign Pass true to align the leading/trailing with the text section at centre
 * otherwise allow the layout decide.
 *
 * @see ListItem
 *
 */
@Composable
fun ListTile(
    headline: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surface,
    enabled: Boolean = true,
    shape: Shape = RectangleShape,
    subtitle: (@Composable () -> Unit)? = null,
    overline: (@Composable () -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
    centerAlign: Boolean = false
) {
    // Provide color and style to the composable.
    // Also, control the enabling and disabling of this composable.
    val content = @Composable {

        val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
        val onSurface = MaterialTheme.colorScheme.onSurface

        var onColor = if (enabled) onSurface else onSurface.copy(ListItemDisabledLabelTextOpacity)
        CompositionLocalProvider(LocalContentColor provides onColor) {
            ProvideTextStyle(value = MaterialTheme.typography.bodyLarge) {
                headline() // 0
            }
        }

        onColor = if (enabled) onSurfaceVariant else onSurface.copy(ListItemDisabledIconOpacity)
        CompositionLocalProvider(LocalContentColor provides onColor) {
            leading?.invoke() // 1
            trailing?.invoke() // 2
        }

        onColor = onSurfaceVariant
        CompositionLocalProvider(LocalContentColor provides onColor) {
            ProvideTextStyle(value = MaterialTheme.typography.labelSmall) {
                overline?.invoke() // 3
            }
        }

        CompositionLocalProvider(LocalContentColor provides onColor) {
            ProvideTextStyle(value = MaterialTheme.typography.bodyMedium) {
                subtitle?.invoke() // 4
            }
            footer?.invoke()// 5
        }
    }

    // Find how many lines are there.
    val lines = when {
        subtitle == null && overline == null -> 1
        subtitle == null || overline == null -> 2
        else -> 3
    }
    val isThreeLine = lines == 3
    val minHeight: Dp = when (lines) {
        1 -> LIST_ITEM_ONE_LINE_CONTAINER_HEIGHT
        2 -> LIST_ITEM_TWO_LINE_CONTAINER_HEIGHT
        else -> LIST_ITEM_THREE_LINE_CONTAINER_HEIGHT // 3
    }
    // Calculate the content padding for this item.
    val vPadding =
        if (isThreeLine) LIST_ITEM_THREE_LINE_VERTICAL_PADDING else LIST_TILE_VERTICAL_PADDING
    val outerPaddingValues =
        PaddingValues(LIST_TILE_START_PADDING, vPadding, LIST_TILE_END_PADDING, vPadding)
    // Actual layout
    Layout(
        content = content,
        modifier = modifier
            .background(color, shape)
            .fillMaxWidth()
            .heightIn(minHeight)
            .padding(outerPaddingValues)
    ) { measurables, constraints ->
        val width = constraints.maxWidth
        val leadingPaddingPx = LEADING_CONTENT_END_PADDING.roundToPx()
        val trailingPaddingPx = TRAILING_CONTENT_START_PADDING.roundToPx()
        // Leading Placeable.
        // The leading placeable has same index since headline is never null.
        // Leading placeable will be placed at the start of the screen.
        var index = 0
        // Measure it as desired size.
        // The width that is consumed till now
        val unrestricted = constraints.copy(minWidth = 0, minHeight = 0)
        val leadingPlaceable =
            if (leading != null) measurables[++index].measure(unrestricted) else null
        val trailingPlaceable =
            if (trailing != null) measurables[++index].measure(unrestricted) else null
        // The space remained for text part of this composable.
        val remaining = width -
                (if (leadingPlaceable == null) 0 else leadingPlaceable.width + leadingPaddingPx) -
                (if (trailingPlaceable == null) 0 else trailingPlaceable.width + trailingPaddingPx)
        // fill-space between leading and trailing placeable.
        val textConstraints = constraints.copy(
            minWidth = 0, maxWidth = remaining.coerceIn(0, width), 0
        )
        val overlinePlaceable =
            if (overline != null) measurables[++index].measure(textConstraints) else null
        val subtitlePlaceable =
            if (subtitle != null) measurables[++index].measure(textConstraints) else null
        // measure bottom unrestricted.
        val bottomPlaceable = if (footer != null)
            measurables[++index].measure(unrestricted)
        else
            null
        // must be non null
        val headingPlaceable = measurables[0].measure(textConstraints)
        // Height must be equal to the height of the children.
        val height = maxOf(
            (leadingPlaceable?.height ?: 0),
            (overlinePlaceable?.height ?: 0) + headingPlaceable.height + (subtitlePlaceable?.height
                ?: 0),
            (trailingPlaceable?.height ?: 0),
            constraints.minHeight
        )
        val topAlign =
            if (centerAlign) false else height > LIST_ITEM_ONE_LINE_CONTAINER_HEIGHT.toPx()
        // place each item as per restrictions.
        layout(width, height + (bottomPlaceable?.height ?: 0)) {
            var x: Int = 0
            var y: Int = if (topAlign) 0 else height / 2 - (leadingPlaceable?.height ?: 0) / 2
            leadingPlaceable?.placeRelative(x, y)
            // TODO: Use text placeable width as constraint.
            y = if (topAlign) 0 else height / 2 - (trailingPlaceable?.height ?: 0) / 2
            x = (width - (trailingPlaceable?.width ?: 0)).coerceIn(0, width)
            trailingPlaceable?.placeRelative(x, y)
            // overline
            x = if (leadingPlaceable == null) 0 else (leadingPlaceable.width + leadingPaddingPx)
            y = if (topAlign) 0 else (height / 2 - ((overlinePlaceable?.height
                ?: 0) + headingPlaceable.height + (subtitlePlaceable?.height ?: 0)) / 2)
            overlinePlaceable?.placeRelative(x, y)
            y += overlinePlaceable?.height ?: 0
            headingPlaceable.placeRelative(x, y)
            y += headingPlaceable.height
            subtitlePlaceable?.placeRelative(x, y)
            x = 0
            y = height
            bottomPlaceable?.placeRelative(x, y)
        }
    }
}
