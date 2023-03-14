package com.primex.material2

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.*
import androidx.compose.ui.unit.dp

private const val INDEX_LEADING = 0
private const val INDEX_OVERLINE = 1
private const val INDEX_TEXT = 2
private const val INDEX_SECONDARY = 3
private const val INDEX_TRAILING = 4
private const val INDEX_OPTIONAL_BOTTOM = 5

private val ListItemHorizontalPadding = 16.dp
private val ListItemVerticalPadding = 8.dp


private val ListItemTextContentHorizontalPadding = ListItemVerticalPadding

private val LeadingTrailingMaxWidth = 80.dp


private const val ContentAlphaIndication = 0.2f


@Composable
fun ListTile(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    centreVertically: Boolean = false,
    leading: @Composable (() -> Unit)? = null,
    secondaryText: @Composable (() -> Unit)? = null,
    overlineText: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    bottom: @Composable (() -> Unit)? = null,
    text: @Composable () -> Unit
) {

    val bg by animateColorAsState(
        targetValue = if (selected) LocalContentColor.current.copy(
            ContentAlphaIndication
        ) else Color.Transparent
    )

    val content =
        @UiComposable
        @Composable {
            val contentAlpha = if (enabled) ContentAlpha.high else ContentAlpha.disabled
            CompositionLocalProvider(LocalContentAlpha provides contentAlpha) {
                val typography = MaterialTheme.typography

                // place icn or the Spacer
                // TODO: find may be replacement for Spacer.
                val emptyModifier = Modifier
                leading?.invoke() ?: Spacer(modifier = emptyModifier)

                ProvideTextStyle(style = typography.overline) {
                    overlineText?.invoke() ?: Spacer(modifier = emptyModifier)
                }

                ProvideTextStyle(style = typography.body1) {
                    text()
                }

                val alpha = if (enabled) ContentAlpha.medium else ContentAlpha.disabled
                ProvideTextStyle(style = typography.body2, alpha = alpha) {
                    secondaryText?.invoke() ?: Spacer(modifier = emptyModifier)
                }

                trailing?.invoke() ?: Spacer(modifier = emptyModifier)

                bottom?.invoke() ?: Spacer(modifier = emptyModifier)
            }
        }

    val modifier =
        Modifier
            .background(color = bg)
            .fillMaxWidth()
            .wrapContentHeight()
            .then(modifier)
            .padding(horizontal = ListItemHorizontalPadding, vertical = ListItemVerticalPadding)

    val measurePolicy = listTileMeasurePolicy(centreVertically = centreVertically)

    Layout(
        measurePolicy = measurePolicy,
        modifier = modifier,
        content = content
    )
}

@Composable
@PublishedApi
internal fun listTileMeasurePolicy(centreVertically: Boolean) =
    remember(centreVertically) {
        MeasurePolicy { measurables, constraints ->

            val leadingTrailingConstraints = constraints.copy(
                maxWidth = LeadingTrailingMaxWidth.roundToPx(),
                minWidth = 0,
            )

            val leadingPlaceable = measurables[INDEX_LEADING].measure(leadingTrailingConstraints)
            val trailingPlaceable = measurables[INDEX_TRAILING].measure(leadingTrailingConstraints)

            // the text body content
            val textContentPadding = ListItemTextContentHorizontalPadding.roundToPx()
            val textConstraints = constraints.copy(
                maxWidth = constraints.maxWidth - (leadingPlaceable.width + trailingPlaceable.width + 2 * textContentPadding),
                minWidth = 0
            )

            val overlinePlaceable = measurables[INDEX_OVERLINE].measure(textConstraints)
            val textPlaceable = measurables[INDEX_TEXT].measure(textConstraints)
            val secondaryPlaceable = measurables[INDEX_SECONDARY].measure(textConstraints)

            val bottomPlaceable = measurables[INDEX_OPTIONAL_BOTTOM].measure(constraints)

            val heightTextContent =
                overlinePlaceable.height + textPlaceable.height + secondaryPlaceable.height
            val heightLeadingTrailing = maxOf(leadingPlaceable.height, trailingPlaceable.height)
            val heightBottomPlaceable = bottomPlaceable.height

            val heightMainContent = maxOf(heightTextContent, heightLeadingTrailing)

            val height = heightMainContent + heightBottomPlaceable
            val width = constraints.maxWidth

            val centreY = heightMainContent / 2

            layout(width, height) {
                var xPosition = 0
                var yPosition = if (centreVertically) centreY - leadingPlaceable.height / 2 else 0

                // place icon at centreY
                leadingPlaceable.placeRelative(xPosition, yPosition)

                //place text
                xPosition = leadingPlaceable.width + textContentPadding
                yPosition = if (centreVertically) centreY - heightTextContent / 2 else 0
                overlinePlaceable.placeRelative(xPosition, yPosition)

                yPosition += overlinePlaceable.height
                textPlaceable.placeRelative(xPosition, yPosition)

                yPosition += textPlaceable.height
                secondaryPlaceable.placeRelative(xPosition, yPosition)

                xPosition = constraints.maxWidth - trailingPlaceable.width
                yPosition = if (centreVertically) centreY - trailingPlaceable.height / 2 else 0
                trailingPlaceable.placeRelative(xPosition, yPosition)

                xPosition = 0
                yPosition = height - heightBottomPlaceable
                bottomPlaceable.placeRelative(xPosition, yPosition)
            }
        }
    }