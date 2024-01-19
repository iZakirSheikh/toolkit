package com.primex.core

import android.content.res.Resources
import android.text.Spanned
import android.text.style.*
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString

/*
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

*/
/**
 * Helper function for converting [Spanned] to [AnnotatedString]
 *//*
private fun Spanned.annotate(density: Density) =
    buildAnnotatedString {
        with(density) {
            val text = this@annotate

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

                val style = when (span) {
                    is StyleSpan -> span.toSpanStyle
                    is TypefaceSpan -> span.toSpanStyle
                    is AbsoluteSizeSpan -> SpanStyle(fontSize = if (span.dip) span.size.dp.toSp() else span.size.toSp())
                    is RelativeSizeSpan -> SpanStyle(fontSize = span.sizeChange.em)
                    is StrikethroughSpan -> SpanStyle(textDecoration = TextDecoration.LineThrough)
                    is UnderlineSpan -> SpanStyle(textDecoration = TextDecoration.Underline)
                    is SuperscriptSpan -> SpanStyle(baselineShift = BaselineShift.Superscript)
                    is SubscriptSpan -> SpanStyle(baselineShift = BaselineShift.Subscript)
                    is ForegroundColorSpan -> SpanStyle(color = Color(span.foregroundColor))
                    // no idea wh this not works with html
                    is BackgroundColorSpan -> SpanStyle(background = Color(span.backgroundColor))
                    else -> *//*SpanStyle()*//* SpanStyle()
                }
                addStyle(style, start, end)
            }
        }
    }*/


/**
 * A composable function that returns the [Resources]. It will be recomposed when [Configuration]
 * gets updated.
 */
@Composable
@ReadOnlyComposable
private fun resources(): Resources {
    LocalConfiguration.current
    return LocalContext.current.resources
}

/**
 * Returns an [AnnotatedString] object representing the HTML content of the string resource with
 * the given resource ID.
 *
 * This function takes a resource ID pointing to a string resource containing HTML content. It
 * returns an [AnnotatedString] object that can be used to display the HTML content in a UI element
 * such as a TextView.
 *
 * @param id The resource ID of the string containing HTML content.
 * @return An [AnnotatedString] object representing the HTML content of the string resource.
 */
@ExperimentalToolkitApi
@Composable
@ReadOnlyComposable
@Deprecated("Use new 'textResource' instead.", ReplaceWith("textResource(id)"))
fun stringHtmlResource(@StringRes id: Int): AnnotatedString {
    val resources = resources()
    val text = resources.getText(id)
    return if (text !is Spanned) AnnotatedString(text.toString()) else text.toAnnotatedString()
}