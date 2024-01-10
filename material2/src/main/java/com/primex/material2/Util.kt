package com.primex.material2

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit

/**
 * Provides the [style] [TextStyle] to a group of composable functions nested inside it.
 *
 * @param style the [TextStyle] to be provided
 * @param alpha the alpha value to be used for the text; defaults to [LocalContentAlpha.current]
 * @param color the [Color] value to be used for the text; defaults to [LocalContentColor.current]
 * @param textSelectionColors the [TextSelectionColors] value to be used for the text selection color; defaults to [LocalTextSelectionColors.current]
 * @param content the group of composable functions that will receive the provided [style]
 */
@Composable
inline fun ProvideTextStyle(
    style: TextStyle,
    alpha: Float = LocalContentAlpha.current,
    color: Color = LocalContentColor.current,
    textSelectionColors: TextSelectionColors = LocalTextSelectionColors.current,
    noinline content: @Composable () -> Unit,
) {
    val mergedStyle = LocalTextStyle.current.merge(style)
    CompositionLocalProvider(
        LocalContentColor provides color,
        LocalContentAlpha provides alpha,
        LocalTextSelectionColors provides textSelectionColors,
        LocalTextStyle provides mergedStyle,
        content = content
    )
}

/**
 * A composable function that provides a custom [TextStyle] to its children.
 *
 * @param style the custom [TextStyle] to apply
 * @param alpha the alpha value to apply to the text
 * @param color the color to apply to the text
 * @param textSelectionColors the [TextSelectionColors] to apply
 * @param fontSize the font size to apply to the text
 * @param fontStyle the font style to apply to the text
 * @param fontWeight the font weight to apply to the text
 * @param fontFamily the font family to apply to the text
 * @param letterSpacing the letter spacing to apply to the text
 * @param textDecoration the text decoration to apply to the text
 * @param textAlign the text alignment to apply to the text
 * @param lineHeight the line height to apply to the text
 * @param content the content to apply the custom [TextStyle] to
 */
@Composable
inline fun ProvideTextStyle(
    style: TextStyle = LocalTextStyle.current,
    alpha: Float = LocalContentAlpha.current,
    color: Color = LocalContentColor.current,
    textSelectionColors: TextSelectionColors = LocalTextSelectionColors.current,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    noinline content: @Composable () -> Unit,
) {
    // NOTE(text-perf-review): It might be worthwhile writing a bespoke merge implementation that
    // will avoid reallocating if all of the options here are the defaults
    val mergedStyle =
        style.merge(
            TextStyle(
                color = color.copy(alpha),
                fontSize = fontSize,
                fontWeight = fontWeight,
                textAlign = TextAlign.Unspecified,
                lineHeight = lineHeight,
                fontFamily = fontFamily,
                textDecoration = textDecoration,
                fontStyle = fontStyle,
                letterSpacing = letterSpacing
            )
        )

    CompositionLocalProvider(
        LocalTextSelectionColors provides textSelectionColors,
        LocalTextStyle provides mergedStyle,
        content = content
    )
}


/**
 * The recommended divider Alpha
 */
val ContentAlpha.Divider
    get() = com.primex.material2.Divider
private const val Divider = 0.12f


/**
 * The recommended LocalIndication Alpha
 */
val ContentAlpha.Indication
    get() = com.primex.material2.Indication
private const val Indication = 0.1f
