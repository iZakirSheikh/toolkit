/*
 * Copyright 2025 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 31-01-2025.
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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.takeOrElse
import com.zs.compose.theme.text.ProvideTextStyle

private const val TAG = "ListItem"

private val LIST_TILE_START_PADDING = 16.dp
private val LIST_TILE_VERTICAL_PADDING = 8.dp
private val LIST_TILE_END_PADDING = 16.dp
private val LIST_ITEM_THREE_LINE_VERTICAL_PADDING = 12.dp

// Icon related defaults.
private val LEADING_CONTENT_END_PADDING = 16.dp

// Trailing related defaults.
private val TRAILING_CONTENT_START_PADDING = 16.dp

private val LIST_ITEM_ONE_LINE_CONTAINER_HEIGHT = 56.0.dp
private val LIST_ITEM_TWO_LINE_CONTAINER_HEIGHT = 72.0.dp
private val LIST_ITEM_THREE_LINE_CONTAINER_HEIGHT = 88.0.dp

/**
 * Lists are continuous, vertical indexes of text or images.
 *
 * [Lists image](https://developer.android.com/images/reference/androidx/compose/material3/lists.png)
 *
 * This component can be used to achieve the list item templates existing in the spec. One-line list
 * items have a singular line of headline content. Two-line list items additionally have either
 * supporting or overline content. Three-line list items have either both supporting and overline
 * content, or extended (two-line) supporting text.
 *
 * This composable provides a streamlined alternative to the standard [ListItem], offering greater
 * flexibility in styling and content arrangement.
 * It allows you to define the headline, subtitle, overline, leading and trailing icons, and a footer section.
 *
 * **Note:**
 * - The `headline` composable is required. Passing an empty value may lead to unexpected behavior.
 * - Pass `null` for optional compatibles (`subtitle`, `overline`, `leading`, `trailing`, `footer`)
 * if they are not needed.
 * - Each composable slot should ideally contain only one direct child.
 *
 * @param heading The primary text content of the list item. **Required.**
 * @param modifier The modifier to apply to the list item.
 * @param contentColor The content color used for text and icons on the list item.
 * @param padding The content padding of the list item. Uses default padding if not specified.
 * @param spacing The spacing between the leading/trailing icons and the text content. Uses default spacing if not specified.
 * @param subheading Optional secondary text content displayed below the headline.
 * @param overline Optional text content displayed above the headline.
 * @param leading Optional composable for displaying an icon or other content at the start of the list item.
 * @param trailing Optional composable for displaying an icon or other content at the end of the list item.
 * @param footer Optional composable for displaying content at the bottom of the list item, between the leading and trailing sections.
 * @param centerAlign Whether to vertically center-align the leading/trailing icons with the text content. Defaults to `false`.
 *
 * @see androidx.compose.material.ListItem
 */
@Composable
@ExperimentalThemeApi
fun BaseListItem(
    heading: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    contentColor: Color = LocalContentColor.current,
    padding: PaddingValues? = null,
    spacing: Dp = Dp.Unspecified,
    subheading: (@Composable () -> Unit)? = null,
    overline: (@Composable () -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
    centerAlign: Boolean = false
) {
    // Provide color and style to the composable.
    // Also, control the enabling and disabling of this composable.
    val content = @Composable {
        val typography = AppTheme.typography
        // Style the compatibles
        CompositionLocalProvider(
            LocalContentColor provides contentColor,
        ) {
            // Headline
            ProvideTextStyle(typography.title2) {
                heading() // 0
            }

            //Overline
            // Leading/Trailing icons; I guess doesn't need TextStyle I guess.
            if (leading != null || trailing != null)
                ProvideTextStyle(typography.label3) {
                    leading?.invoke() // 1
                    trailing?.invoke() // 2
                    if (overline != null)
                        CompositionLocalProvider(
                            LocalContentColor provides contentColor.copy(ContentAlpha.medium),
                            overline // 3
                        )
                }
            // Support Text
            if (subheading != null)
                ProvideTextStyle(value = typography.body2) {
                    CompositionLocalProvider(
                        LocalContentColor provides contentColor.copy(ContentAlpha.medium),
                        content = subheading // 4
                    )
                }

            // Bottom
            footer?.invoke() // 5
        }
    }
    // Find how many lines are there.
    val lines = when {
        subheading == null && overline == null -> 1
        subheading == null || overline == null -> 2
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
            .fillMaxWidth()
            .heightIn(minHeight)
            .padding(padding ?: outerPaddingValues)
    ) { measurables, constraints ->
        val width = constraints.maxWidth
        val leadingPaddingPx = spacing.takeOrElse { LEADING_CONTENT_END_PADDING }.roundToPx()
        val trailingPaddingPx = spacing.takeOrElse { TRAILING_CONTENT_START_PADDING }.roundToPx()
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
            if (subheading != null) measurables[++index].measure(textConstraints) else null
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
            if (centerAlign) false else height > LIST_ITEM_ONE_LINE_CONTAINER_HEIGHT.toPx() * 1.2f
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

/** @see BaseListItem */
@OptIn(ExperimentalThemeApi::class)
@Composable
@NonRestartableComposable
fun ListItem(
    heading: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    contentColor: Color = LocalContentColor.current,
    padding: PaddingValues? = null,
    spacing: Dp = Dp.Unspecified,
    subheading: (@Composable () -> Unit)? = null,
    overline: (@Composable () -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) = BaseListItem(
    heading = heading,
    modifier = modifier,
    contentColor = contentColor,
    padding = padding,
    spacing = spacing,
    subheading = subheading,
    overline = overline,
    leading = leading,
    trailing = trailing
)
