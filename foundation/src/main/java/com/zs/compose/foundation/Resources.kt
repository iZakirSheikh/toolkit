/*
 * Copyright 2025 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 26-01-2025.
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

package com.zs.compose.foundation

import android.content.res.Resources
import android.graphics.Typeface
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.SubscriptSpan
import android.text.style.SuperscriptSpan
import android.text.style.TypefaceSpan
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import androidx.annotation.ArrayRes
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.UrlAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.text.HtmlCompat

private const val TAG = "Resources"

private inline val TypefaceSpan.toSpanStyle: SpanStyle
    get() = SpanStyle(
        fontFamily = when (family) {
            FontFamily.SansSerif.name -> FontFamily.SansSerif
            FontFamily.Serif.name -> FontFamily.Serif
            FontFamily.Monospace.name -> FontFamily.Monospace
            FontFamily.Cursive.name -> FontFamily.Cursive
            else -> FontFamily.Default
        }
    )

private inline val StyleSpan.toSpanStyle: SpanStyle
    get() {
        return when (style) {
            Typeface.NORMAL -> SpanStyle()
            Typeface.BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
            Typeface.ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)
            Typeface.BOLD_ITALIC -> SpanStyle(
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic
            )

            else -> error("$style not supported")
        }
    }

/**
 * Converts a [Spanned] object into an [AnnotatedString] object.
 *
 * This function supports only a handful of span types and applies
 * the corresponding [SpanStyle] to each span in the input.
 *
 * The supported span types are: [StyleSpan], [TypefaceSpan], [AbsoluteSizeSpan], [RelativeSizeSpan],
 * [StrikethroughSpan], [UnderlineSpan], [SuperscriptSpan], [SubscriptSpan], [ForegroundColorSpan], [BackgroundColorSpan]
 *
 * @receiver The [Spanned] object to be converted.
 * @return An [AnnotatedString] object with the same text and style
 * as the receiver.
 * @throws IllegalStateException If the receiver contains an unsupported span type.
 *
 * Sources:
 * - [IssueTracker](https://kotlinlang.org/docs/kotlin-doc.html)
 * - [Issue Tracker](https://kotlinlang.org/docs/coding-conventions.html)
 * - [IssueTracker](https://stackoverflow.com/questions/53413060/how-to-add-formatting-such-as-bold-italic-underscore-etc-to-kotlin-documenat)
 * - [Article](https://medium.com/nerd-for-tech/styling-dynamic-strings-directly-in-xml-7b93cfe380ab)
 *
 * Note: The size in [AbsoluteSizeSpan] is taken as Sp no matter what.
 */

@OptIn(ExperimentalTextApi::class)
@ExperimentalFoundationApi
private fun Spanned.toAnnotatedString() =
    buildAnnotatedString {
        val text = this@toAnnotatedString
        // append the string and
        // then apply the supported annotations.
        append((text.toString()))
        // iterate though each span
        // from the old android and
        // return the annotated version of the
        // corresponding span.
        text.getSpans(0, text.length, Any::class.java).forEach { span ->
            val start = text.getSpanStart(span)
            val end = text.getSpanEnd(span)
            // convert span to corresponding style
            val style = when (span) {
                is StyleSpan -> span.toSpanStyle
                is TypefaceSpan -> span.toSpanStyle
                // whatever size is(dip or pixel) we treat it as sp.
                is AbsoluteSizeSpan -> SpanStyle(fontSize = span.size.sp)
                is RelativeSizeSpan -> SpanStyle(fontSize = span.sizeChange.em)
                is StrikethroughSpan -> SpanStyle(textDecoration = TextDecoration.LineThrough)
                is UnderlineSpan -> SpanStyle(textDecoration = TextDecoration.Underline)
                is SuperscriptSpan -> SpanStyle(baselineShift = BaselineShift.Superscript)
                is SubscriptSpan -> SpanStyle(baselineShift = BaselineShift.Subscript)
                is ForegroundColorSpan -> SpanStyle(color = Color(span.foregroundColor))
                // no idea wh this not works with html
                is BackgroundColorSpan -> SpanStyle(background = Color(span.backgroundColor))
                is URLSpan -> {
                    UrlAnnotation(span.url)
                    SpanStyle(color = Color.SkyBlue, textDecoration = TextDecoration.Underline)
                }

                else -> /*SpanStyle()*/ SpanStyle() // FixMe - unsupported span_ just ignore.
            }
            addStyle(style, start, end)
        }
    }

/**
 * Formats the [CharSequence] with the given arguments using [String.format].
 * If the char sequence is a [Spanned] object, it preserves the span styles by converting it to HTML first.
 * If the char sequence is neither a [String] nor a [Spanned], it throws an exception.
 *
 * Note- It returns same string if the [args] are empty.
 *
 * @param args The arguments to be used in the format string, or empty if none.
 * @return A formatted char sequence with the same type as the receiver.
 * @throws IllegalStateException If the receiver is an unsupported type of char sequence.
 */
@ExperimentalFoundationApi
private fun CharSequence.format(vararg args: Any): CharSequence {
    // if args is empty early return
    if (args.isEmpty()) return this
    return when (val text = this) {
        is String -> String.format(text, *args)
        is Spanned -> {
            // convert to html
            val html = HtmlCompat.toHtml(text, HtmlCompat.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL)
            // add args
            val formatted = String.format(html, *args)
            // return CharSequence.
            HtmlCompat.fromHtml(formatted, HtmlCompat.FROM_HTML_MODE_COMPACT)
        }

        else -> error("$this type of CharSequence is  not supported")
    }
}

/**
 * An extension function that returns a formatted string with the given arguments and preserves the style information.
 * This function is useful for creating dynamic texts with formatting from string resources.
 * @param id The resource id of the string to format.
 * @param args The arguments to replace the format specifiers in the string.
 * @return A spanned or a string that contains the formatted text with the style information.
 */
@ExperimentalFoundationApi
fun Resources.getText(@StringRes id: Int, vararg args: Any): CharSequence =
    getText(id).format(*args)

/**
 * An extension function that returns a formatted string for the given quantity and arguments and preserves the style information.
 * This function is useful for creating dynamic texts with formatting from plural resources.
 * @param id The resource id of the plural to format.
 * @param quantity The number used to select the correct string for the current language's plural rules.
 * @param args The arguments to replace the format specifiers in the plural string.
 * @return A spanned or a string that contains the formatted text with the style information.
 */
@OptIn(ExperimentalTextApi::class)
@ExperimentalFoundationApi
fun Resources.getQuantityText(@PluralsRes id: Int, quantity: Int, vararg args: Any): CharSequence =
    getQuantityText(id, quantity).format(*args)

/**
 * An extension function on [Resources] that returns [AnnotatedString] or [String] provided if the resource is [String] or [Spanned].
 *
 * The following HTML tags are currently supported by this function, others are ignored:
 *
 * ```
 * <string name="styled_with_args">
 *         <b>bold</b>
 *         <i>italic</i>
 *         <big>big</big>
 *         <small>small</small>
 *         <font face="monospace">monospace</font>
 *         <font face="serif">serif</font>
 *         <font face="sans_serif">sans_serif</font>
 *         <font face="cursive">cursive</font>
 *         <font color="#00ff00">green</font>
 *         <tt>tt</tt>
 *         <strike>strike</strike>
 *         <u>underline</u>
 *         <sup>sup</sup>
 *         <sub>sub</sub>
 *         <span style="color: #ff0000; background-color: #00ff00; text-decoration: line-through">span</span>
 *         <p dir="rtl">rtl</p>
 *         <p dir="ltr">ltr</p>
 *         <div>div</div>
 *         <br/>
 *         This is a text with a <a href="https://www.bing.com">URL</a> span.
 *     </string>
 * ```
 * @receiver The [Resources] object to get the string resource from.
 * @param id The resource ID of the string to be formatted.
 * @return A formatted [CharSequence], either a [String] or an [AnnotatedString], depending on the type of the string resource.
 * @throws Resources.NotFoundException If the given ID does not exist
 */
@ExperimentalFoundationApi
fun Resources.getText2(@StringRes id: Int): CharSequence =
    when (val formatted = getText(id)) {
        is String -> formatted
        is Spanned -> formatted.toAnnotatedString()
        else -> error("$formatted is some other type of string.")
    }

/**
 * @see getText2
 * @receiver The [Resources] object to get the string resource from.
 * @param id The resource ID of the string to be formatted.
 * @param args The arguments to be used in the format string, or empty if none.
 * @return A formatted [CharSequence], either a [String] or an [AnnotatedString], depending on the type of the string resource.
 * @throws Resources.NotFoundException If the given ID does not exist.
 * @throws IllegalArgumentException If the formatted char sequence is neither a [String] nor a [Spanned].
 */
@ExperimentalFoundationApi
fun Resources.getText2(@StringRes id: Int, vararg args: Any): CharSequence =
    when (val formatted = getText(id).format(*args)) {
        is String -> formatted
        is Spanned -> formatted.toAnnotatedString()
        else -> error("$formatted is some other type of string.")
    }

/**
 * An extension function on [Resources] that returns styled [AnnotatedString] or simple string based on if the original is [Spanned] or simple string.
 *
 * @receiver The [Resources] object to get the string resource from.
 * @param id The resource ID of the string to be formatted.
 * @param quantity The number used to select the correct string for the current language's plural rules.
 * @return A formatted [CharSequence], either a [String] or an [AnnotatedString], depending on the type of the string resource.
 * @throws Resources.NotFoundException If the given ID does not exist.
 * @throws IllegalStateException If the formatted char sequence is neither a [String] nor a [Spanned].
 */
@ExperimentalFoundationApi
fun Resources.getQuantityText2(@PluralsRes id: Int, quantity: Int): CharSequence =
    when (val text = getQuantityText(id, quantity)) {
        is String -> text
        is Spanned -> text.toAnnotatedString()
        else -> error("$text is some other type of string.")
    }

/**
 * @param args The arguments to be used in the format string, or empty if none.
 * @see getQuantityText2
 */
@ExperimentalFoundationApi
fun Resources.getQuantityText2(@PluralsRes id: Int, quantity: Int, vararg args: Any): CharSequence =
    when (val text = getQuantityText(id, quantity).format(*args)) {
        is String -> text
        is Spanned -> text.toAnnotatedString()
        else -> error("$text is some other type of string.")
    }

@ExperimentalFoundationApi
fun Resources.getTextArray2(@ArrayRes id: Int): Array<CharSequence> {
    val array = getTextArray(id)
    for (i in array.indices) {
        array[i] = when (val text = array[i]) {
            is String -> text
            is Spanned -> text.toAnnotatedString()
            else -> error("$text is some other type of string.")
        }
    }
    return array
}

@Composable
@ReadOnlyComposable
private fun resources(): Resources {
    LocalConfiguration.current
    return LocalContext.current.resources
}

/**
 * @see getText2
 * @param id The resource ID of the string to be formatted.
 * @return A formatted char sequence, either a [String] or an [AnnotatedString], depending on the type of the string resource.
 */
@Composable
@ExperimentalFoundationApi
@NonRestartableComposable
fun textResource(@StringRes id: Int): CharSequence {
    val resources = resources()
    return when (val formatted = resources.getText(id)) {
        is String -> formatted
        is Spanned -> remember(key1 = id, calculation = formatted::toAnnotatedString)
        else -> error("$formatted is some other type of string.")
    }
}


/**
 * Gets a string resource with the given ID and formats it with the given arguments.
 * @param args The arguments to be used in the format string, or empty if none.
 * @see textResource
 * @see getText2
 */
@Composable
@NonRestartableComposable
@ExperimentalFoundationApi
fun textResource(@StringRes id: Int, vararg args: Any): CharSequence {
    val resources = resources()
    return when (val formatted = resources.getText(id).format(*args)) {
        is String -> formatted
        is Spanned -> remember(key1 = id, key2 = args, calculation = formatted::toAnnotatedString)
        else -> error("$formatted is some other type of string.")
    }
}

@Composable
@NonRestartableComposable
@ExperimentalFoundationApi
fun pluralTextResource(@PluralsRes id: Int, quantity: Int, vararg args: Any): CharSequence {
    val resources = resources()
    return when (val text = resources.getQuantityText(id, quantity).format(*args)) {
        is String -> text
        is Spanned -> remember(
            key1 = id,
            key2 = quantity,
            key3 = args,
            calculation = text::toAnnotatedString
        )

        else -> error("$text is some other type of string.")
    }
}

@Composable
@NonRestartableComposable
@ExperimentalFoundationApi
fun pluralTextResource(@PluralsRes id: Int, quantity: Int): CharSequence {
    val resources = resources()
    return when (val text = resources.getQuantityText(id, quantity)) {
        is String -> text
        is Spanned -> remember(key1 = id, key2 = quantity, calculation = text::toAnnotatedString)
        else -> error("$text is some other type of string.")
    }
}

@Composable
@NonRestartableComposable
@ExperimentalFoundationApi
fun textArrayResource(@ArrayRes id: Int): Array<CharSequence> {
    val resources = resources()
    return remember(id) {
        resources.getTextArray2(id)
    }
}