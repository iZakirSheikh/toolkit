package com.primex.core

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.util.Log
import android.view.Window
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.ColorUtils
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.Closeable
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.RenderVectorGroup
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.PlatformParagraphStyle
import androidx.compose.ui.text.PlatformSpanStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

/**
 * A composable function that creates a [MutableState] object which can be used to hold and update
 * a value in the composable's memory.
 *
 * @param initial The initial value for the state.
 *
 * @return A [MutableState] object initialized with the [initial] value.
 *
 * @see remember
 * @see mutableStateOf
 */
@Composable
@Deprecated(
    "Use 'remember { mutableStateOf(value)}' instead.'",
    ReplaceWith("remember { mutableStateOf(value = )}", "androidx.compose.runtime.remember", "androidx.compose.runtime.mutableStateOf"),
)
inline fun <T> rememberState(initial: T): MutableState<T> = remember {
    mutableStateOf(initial)
}


/**
 * @see findActivity
 */
@Deprecated(
    "Use 'findActivity' instead.'",
    ReplaceWith("findActivity()"),
)
val Context.activity: Activity get() = findActivity()

/**
 * A utility function that recursively searches for and returns the [Activity] instance associated with the current [Context].
 *
 * @return The [Activity] instance associated with the current [Context].
 * @throws IllegalStateException if the current [Context] is not an [Activity] context.
 */
tailrec fun Context.findActivity(): Activity =
    when (this) {
        is Activity -> this
        is ContextWrapper -> this.baseContext.findActivity()
        else -> throw IllegalStateException(
            "Context is not an Activity context, but a ${javaClass.simpleName} context. "
        )
    }


/**
 * A composable function that requests a specific screen orientation for the current activity.
 *
 * @param orientation The requested screen orientation, as defined by [android.content.pm.ActivityInfo].
 * @throws IllegalStateException if the current [Context] is not an [Activity] context.
 */
@Suppress("NOTHING_TO_INLINE")
@Composable
@ExperimentalToolkitApi
inline fun LockScreenOrientation(orientation: Int) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val activity = context.activity ?: return@DisposableEffect onDispose {}
        val originalOrientation = activity.requestedOrientation
        activity.requestedOrientation = orientation
        onDispose {
            // restore original orientation when view disappears
            activity.requestedOrientation = originalOrientation
        }
    }
}



/**
 * Returns a new [PaddingValues] object that is the result of adding the values of the specified
 * [PaddingValues] object to this object's values.
 *
 * @param value The [PaddingValues] object to add to this object.
 * @return A new [PaddingValues] object that is the result of adding the values of the specified
 * [PaddingValues] object to this object's values.
 */
@Composable
inline operator fun PaddingValues.plus(value: PaddingValues): PaddingValues {
    val direction = LocalLayoutDirection.current
    return PaddingValues(
        start = this.calculateStartPadding(direction) + value.calculateStartPadding(direction),
        top = this.calculateTopPadding() + value.calculateTopPadding(),
        bottom = this.calculateBottomPadding() + value.calculateBottomPadding(),
        end = this.calculateEndPadding(direction) + value.calculateEndPadding(direction)
    )
}


private const val TAG = "Utils"

/**
 * Calls the specified function [block] and returns its result if invocation was successful,
 * catching any [Throwable] exception that was thrown from the [block] function execution and
 * returning null it as a failure.
 *
 * @param tag: The tag to use for logging the exception message.
 * @param block: The function to execute and catch exceptions for.
 *
 * @return The result of the function if success else null.
 */
inline fun <R> runCatching(tag: String, block: () -> R): R? {
    return try {
        block()
    } catch (e: Throwable) {
        Log.e(tag, "runCatching: ${e.message}")
        null
    }
}


/**
 * Executes the given [block] function on this resource and then closes it down correctly whether an exception
 * is thrown or not.
 *
 * @param block a function to process this [Closeable] resource.
 * @param tag if an exception is thrown null is returned and a message is logged with [tag]
 * @return the result of [block] function invoked on this resource. null if exception.
 */
inline fun <T : Closeable?, R> T.use(tag: String, block: (T) -> R): R? {
    return try {
        use { block(it) }
    } catch (e: Throwable) {
        Log.i(tag, "use: ${e.message}")
        null
    }
}


/**
 * An alternative to [synchronized] using [Mutex]
 */
@Deprecated("Not required. use the extension methods on lock.")
suspend inline fun <T> synchronised(lock: Mutex, action: () -> T): T {
    return lock.withLock(action = action)
}


/**
 * A function meant to accompany composable without triggering whole composable recomposition
 */
@SuppressLint("ComposableNaming")
@Composable
@ReadOnlyComposable
@Deprecated("Not recommended to use.", level = DeprecationLevel.HIDDEN)
fun calculate(calculation: () -> Unit) {
    calculation.invoke()
}

/**
 * Casts the given [anything] object to type [T] using the `as` operator.
 *
 * @param anything The object to be cast.
 * @return The casted object of type [T].
 *
 * @throws ClassCastException if the given object cannot be cast to type [T].
 *
 * @see kotlin.collections.MutableList.cast
 * @see kotlin.collections.MutableList.castOrNull
 */
inline fun <reified T> castTo(anything: Any): T {
    return anything as T
}


/**
 * Provides/finds a [Activity] that is wrapped inside the [LocalContext]
 */

val ProvidableCompositionLocal<Context>.activity
    @ReadOnlyComposable
    @Composable
    get() = current.findActivity()

/**
 * Returns a Resources instance for the application's package.
 */
val ProvidableCompositionLocal<Context>.resources: Resources
    @ReadOnlyComposable
    @Composable
    inline get() = current.resources

/**
 * Returns a Composable function if the condition is true, otherwise returns null.
 *
 * @param condition The boolean condition that determines if the composable function should be returned.
 * @param content The composable function to be returned if the condition is true.
 * @return The composable function if the condition is true, otherwise null.
 */
fun composableOrNull(
    condition: Boolean,
    content: @Composable () -> Unit
) = when (condition) {
    true -> content
    else -> null
}

/**
 *  Applies the specified span style to the [AnnotatedString.Builder] and invokes the specified
 *  [block].
 *  @see SpanStyle
 */
inline fun <R : Any> AnnotatedString.Builder.withSpanStyle(
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    fontStyle: androidx.compose.ui.text.font.FontStyle? = null,
    fontSynthesis: FontSynthesis? = null,
    fontFamily: androidx.compose.ui.text.font.FontFamily? = null,
    fontFeatureSettings: String? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    baselineShift: BaselineShift? = null,
    textGeometricTransform: TextGeometricTransform? = null,
    localeList: LocaleList? = null,
    background: Color = Color.Unspecified,
    textDecoration: TextDecoration? = null,
    shadow: Shadow? = null,
    platformStyle: PlatformSpanStyle? = null,
    drawStyle: DrawStyle? = null,
    block: AnnotatedString.Builder.() -> R
): R = withStyle(
    SpanStyle(
        color,
        fontSize,
        fontWeight,
        fontStyle,
        fontSynthesis,
        fontFamily,
        fontFeatureSettings,
        letterSpacing,
        baselineShift,
        textGeometricTransform,
        localeList,
        background,
        textDecoration,
        shadow,
        platformStyle,
        drawStyle
    ), block
)

/**
 *  Applies the specified span style to the [AnnotatedString.Builder] and invokes the specified
 *  [block].
 *  @see SpanStyle
 */
inline fun <R : Any> AnnotatedString.Builder.withSpanStyle(
    brush: Brush?,
    alpha: Float = Float.NaN,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    fontStyle: androidx.compose.ui.text.font.FontStyle? = null,
    fontSynthesis: FontSynthesis? = null,
    fontFamily: androidx.compose.ui.text.font.FontFamily? = null,
    fontFeatureSettings: String? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    baselineShift: BaselineShift? = null,
    textGeometricTransform: TextGeometricTransform? = null,
    localeList: LocaleList? = null,
    background: Color = Color.Unspecified,
    textDecoration: TextDecoration? = null,
    shadow: Shadow? = null,
    platformStyle: PlatformSpanStyle? = null,
    drawStyle: DrawStyle? = null,
    block: AnnotatedString.Builder.() -> R
): R = withStyle(
    SpanStyle(
        brush,
        alpha,
        fontSize,
        fontWeight,
        fontStyle,
        fontSynthesis,
        fontFamily,
        fontFeatureSettings,
        letterSpacing,
        baselineShift,
        textGeometricTransform,
        localeList,
        background,
        textDecoration,
        shadow,
        platformStyle,
        drawStyle
    ), block
)


/**
 *  Applies the specified Paragraph style to the [AnnotatedString.Builder] and invokes the specified
 *  [block].
 *  @see ParagraphStyle
 */
inline fun <R : Any> AnnotatedString.Builder.withParagraphStyle(
    textAlign: TextAlign = TextAlign.Unspecified,
    textDirection: TextDirection = TextDirection.Unspecified,
    lineHeight: TextUnit = TextUnit.Unspecified,
    textIndent: TextIndent? = null,
    platformStyle: PlatformParagraphStyle? = null,
    lineHeightStyle: LineHeightStyle? = null,
    lineBreak: LineBreak = LineBreak.Unspecified,
    hyphens: Hyphens = Hyphens.Unspecified,
    textMotion: TextMotion? = null,
    crossinline block: AnnotatedString.Builder.() -> R
): R = withStyle(
    ParagraphStyle(
        textAlign,
        textDirection,
        lineHeight,
        textIndent,
        platformStyle,
        lineHeightStyle,
        lineBreak,
        hyphens,
        textMotion
    ), block
)


/**
 * Applies the given text style to the receiver [AnnotatedString.Builder] and executes the provided [block].
 *
 * @param style The text style to apply to the receiver [AnnotatedString.Builder].
 * @param block The block of code to execute within the context of the modified [AnnotatedString.Builder].
 * @return The result of executing the [block].
 */
inline fun <R : Any> AnnotatedString.Builder.withStyle(
    style: TextStyle,
    crossinline block: AnnotatedString.Builder.() -> R
): R = withStyle(style.toParagraphStyle()) {
    withStyle(style.toSpanStyle(), block)
}


/**
 * Creates and remembers a vector painter while providing options to style it.
 *
 * This function creates a vector painter and stores it for future reference. It allows you to apply various styles
 * to the painter, such as color, stroke width, etc.
 */
@Composable
inline fun rememberVectorPainter(
    image: ImageVector,
    defaultWidth: Dp = image.defaultWidth,
    defaultHeight: Dp = image.defaultHeight,
    viewportWidth: Float = image.viewportWidth,
    viewportHeight: Float = image.viewportHeight,
    name: String = image.name,
    tintColor: Color = image.tintColor,
    tintBlendMode: BlendMode = image.tintBlendMode,
    autoMirror: Boolean = image.autoMirror,
) = rememberVectorPainter(
    defaultWidth = defaultWidth,
    defaultHeight = defaultHeight,
    viewportWidth = viewportWidth,
    viewportHeight = viewportHeight,
    name = name,
    tintColor = tintColor,
    tintBlendMode = tintBlendMode,
    autoMirror = autoMirror,
    content = { _, _ -> RenderVectorGroup(group = image.root) }
)