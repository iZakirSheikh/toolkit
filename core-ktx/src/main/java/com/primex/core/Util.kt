package com.primex.core

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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
inline fun <T> rememberState(initial: T): MutableState<T> = remember {
    mutableStateOf(initial)
}


/**
 * @see findActivity
 */
val Context.activity: Activity get() = findActivity()

/**
 * A utility function that recursively searches for and returns the [Activity] instance associated with the current [Context].
 *
 * @return The [Activity] instance associated with the current [Context].
 * @throws IllegalStateException if the current [Context] is not an [Activity] context.
 */
private tailrec fun Context.findActivity(): Activity =
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
 * Return a copy of [Color] from [hue], [saturation], and [lightness] (HSL representation).
 *
 * @param hue The color value in the range (0..360), where 0 is red, 120 is green, and
 * 240 is blue; default value is null; which makes is unaltered.
 * @param saturation The amount of [hue] represented in the color in the range (0..1),
 * where 0 has no color and 1 is fully saturated; default value is null; which makes is unaltered.
 * @param lightness A range of (0..1) where 0 is black, 0.5 is fully colored, and 1 is
 * white; default value is null; which makes is unaltered.
 */
fun Color.hsl(
    hue: Float? = null,
    saturation: Float? = null,
    lightness: Float? = null,
    alpha: Float? = null
): Color {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(toArgb(), hsl)

    // use value or default.
    return Color.hsl(
        hue = hue ?: hsl[0],
        saturation = saturation ?: hsl[1],
        lightness = lightness ?: hsl[2],
        alpha = alpha ?: this.alpha,
    )
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
@Deprecated("Not recommanded to use.")
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
