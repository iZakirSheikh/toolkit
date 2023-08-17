package com.primex.core

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
import android.text.style.UnderlineSpan
import androidx.annotation.ArrayRes
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
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

@ExperimentalTextApi
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
                else -> /*SpanStyle()*/ error("$span not supported")
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
@ExperimentalTextApi
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
 * An extension function on [Resources] that allows to dynamically insert arguments into raw as well
 * as styled strings.
 * This function uses [CharSequence.format] to format the string resource with the given arguments,
 * and then converts it to an [AnnotatedString] if it is a [Spanned] object.
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
 *         <!--  <a href="https://alphabetworkersunion.org/">link</a> (use TalkBack to see link)-->
 *     </string>
 * ```
 * @receiver The [Resources] object to get the string resource from.
 * @param id The resource ID of the string to be formatted.
 * @param args The arguments to be used in the format string, or empty if none.
 * @return A formatted char sequence, either a [String] or an [AnnotatedString], depending on the type of the string resource.
 * @throws Resources.NotFoundException If the given ID does not exist.
 * @throws IllegalArgumentException If the formatted char sequence is neither a [String] nor a [Spanned].
 */
@ExperimentalTextApi
fun Resources.getText2(@StringRes id: Int, vararg args: Any): CharSequence =
    when (val formatted = getText(id).format(*args)) {
        is String -> formatted
        is Spanned -> formatted.toAnnotatedString()
        else -> error("$formatted is some other type of string.")
    }

/**
 * @see getText2
 */
@OptIn(ExperimentalTextApi::class)
fun Resources.getText2(@StringRes id: Int): CharSequence =
    when (val formatted = getText(id)) {
        is String -> formatted
        is Spanned -> formatted.toAnnotatedString()
        else -> error("$formatted is some other type of string.")
    }

/**
 * An extension function on [Resources] that allows to dynamically insert arguments into raw as well
 * as styled strings.
 * This function uses [CharSequence.format] to format the string resource with the given arguments,
 * and then converts it to an [AnnotatedString] if it is a [Spanned] object.
 *
 * @receiver The [Resources] object to get the string resource from.
 * @param id The resource ID of the string to be formatted.
 * @param quantity The number used to select the correct string for the current language's plural rules.
 * @param args The arguments to be used in the format string, or empty if none.
 * @return A formatted CharSequence, either a [String] or an [AnnotatedString], depending on the type of the string resource.
 * @throws Resources.NotFoundException If the given ID does not exist.
 * @throws IllegalStateException If the formatted char sequence is neither a [String] nor a [Spanned].
 */
@ExperimentalTextApi
fun Resources.getQuantityText2(@PluralsRes id: Int, quantity: Int, vararg args: Any): CharSequence =
    when (val text = getQuantityText(id, quantity).format(*args)) {
        is String -> text
        is Spanned -> text.toAnnotatedString()
        else -> error("$text is some other type of string.")
    }

/**
 * @see getQuantityText2
 */
@ExperimentalTextApi
fun Resources.getQuantityText2(@PluralsRes id: Int, quantity: Int): CharSequence =
    when (val text = getQuantityText(id, quantity)) {
        is String -> text
        is Spanned -> text.toAnnotatedString()
        else -> error("$text is some other type of string.")
    }

@Composable
@ReadOnlyComposable
private fun resources(): Resources {
    LocalConfiguration.current
    return LocalContext.current.resources
}

/**
 * Gets a string resource with the given ID and formats it with the given arguments.
 * This function is a composable version of [Resources.getText2] that supports both raw and styled strings.
 * If the string resource is a [Spanned] object, it converts it to an [AnnotatedString] using [Spanned.toAnnotatedString].
 *
 * @param id The resource ID of the string to be formatted.
 * @param args The arguments to be used in the format string, or empty if none.
 * @return A formatted char sequence, either a [String] or an [AnnotatedString], depending on the type of the string resource.
 */
@Composable
@ReadOnlyComposable
@ExperimentalTextApi
fun stringResource2(@StringRes id: Int, vararg args: Any): CharSequence {
    val resources = resources()
    return resources.getText2(id, *args)
}

/**
 * @see stringResource2
 */
@Composable
@ReadOnlyComposable
@ExperimentalTextApi
fun stringResource2(@StringRes id: Int): CharSequence {
    val resources = resources()
    return resources.getText2(id)
}

/**
 * Gets a quantity string resource with the given ID and quantity and formats it with the given arguments.
 * This function is a composable version of [Resources.getQuantityString2] that supports both raw and styled strings.
 * If the string resource is a [Spanned] object, it converts it to an [AnnotatedString] using [Spanned.toAnnotatedString].
 *
 * @param id The resource ID of the string to be formatted.
 * @param quantity The number used to select the correct string for the current language's plural rules.
 * @param args The arguments to be used in the format string, or empty if none.
 * @return A formatted char sequence, either a [String] or an [AnnotatedString], depending on the type of the string resource.
 */
@Composable
@ExperimentalTextApi
fun pluralStringResource2(@PluralsRes id: Int, quantity: Int, vararg args: Any): CharSequence {
    val resources = resources()
    return resources.getQuantityText2(id, quantity, *args)
}

/**
 * @see pluralStringResource2
 */
@Composable
@ExperimentalTextApi
fun pluralStringResource2(@PluralsRes id: Int, quantity: Int): CharSequence {
    val resources = resources()
    return resources.getQuantityText2(id, quantity)
}

/**
 * This might be replaced in future with new that takes vargar and returns styled strings.
 */
@Composable
@ReadOnlyComposable
inline fun stringArrayResource2(@ArrayRes id: Int): Array<String> = stringArrayResource(id = id)

