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

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.view.Gravity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider

private const val TAG = "Util"

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
inline fun <reified T> castTo(anything: Any): T = anything as T

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
 * Conditionally executes a composable function based on a boolean condition.
 * @return The [content] composable function if the [condition] is `true` else null
 */
inline fun composableIf(condition: Boolean, noinline content: @Composable () -> Unit) =
    if (condition) content else null

/**
 * Indicates whether the application is currently running within a preview environment (e.g., Android Studio's layout preview).
 *
 * Unlike `View.isInEditMode` or `LocalInspectionMode`, this check can be performed from outside of Compose's composition.
 * This is crucial for scenarios where ripple behavior needs to be determined before entering the composition phase.
 *
 * [source](https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/material/material-ripple/src/androidMain/kotlin/androidx/compose/material/ripple/Ripple.android.kt;l=81;bpv=0)
 */
internal val IsRunningInPreview = android.os.Build.DEVICE == "layoutlib"

/**
 * Returns a new [PaddingValues] object that is the result of adding the values of the specified
 * [PaddingValues] object to this object's values.
 *
 * @param value The [PaddingValues] object to add to this object.
 * @return A new [PaddingValues] object that is the result of adding the values of the specified
 * [PaddingValues] object to this object's values.
 */
@Composable
@NonRestartableComposable
operator fun PaddingValues.plus(value: PaddingValues): PaddingValues {
    val direction = LocalLayoutDirection.current
    return PaddingValues(
        start = this.calculateStartPadding(direction) + value.calculateStartPadding(direction),
        top = this.calculateTopPadding() + value.calculateTopPadding(),
        bottom = this.calculateBottomPadding() + value.calculateBottomPadding(),
        end = this.calculateEndPadding(direction) + value.calculateEndPadding(direction)
    )
}

/**
 * A sticky header implementation that respects the top padding of the content.
 * This should be removed when an official solution is provided.
 * Currently, the only issue is that the sticky layout and the next item overlap before moving,
 * while the sticky header should start moving when the next item is about to become sticky.
 *
 * @param state The state of the LazyGrid.
 * @param key The key for the sticky header item.
 * @param contentType The type of content for the sticky header.
 * @param content The composable content for the sticky header.
 */
fun LazyGridScope.stickyHeader(
    state: LazyGridState,
    key: Any? = null,
    contentType: Any? = null,
    content: @Composable LazyGridItemScope.() -> Unit
) {
    stickyHeader(
        key = key,
        contentType = contentType,
        content = {
            // TODO - Added parameter isPinned and fix the issue causing it to overlap.
            Layout(content = { content() }) { measurables, constraints ->
                val placeable = measurables[0].measure(constraints)
                val width = constraints.constrainWidth(placeable.width)
                val height = constraints.constrainHeight(placeable.height)
                layout(width, height) {
                    val posY = coordinates?.positionInParent()?.y?.toInt() ?: 0
                    val paddingTop = state.layoutInfo.beforeContentPadding
                    var top = (paddingTop - posY).coerceIn(0, paddingTop)
                    placeable.placeRelativeWithLayer(0, top)
                }
            }
        }
    )
}

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
        else -> throw error("Context is not an Activity context, but a ${javaClass.simpleName} context. ")
    }

private val _fullLineSpan: (LazyGridItemSpanScope.() -> GridItemSpan) =
    { GridItemSpan(maxLineSpan) }

/**
 * A [GridItemSpan] that spans the entire width of a grid.
 *
 * Use this within a [LazyGridScope] to make an item occupy all columns in a [LazyVerticalGrid] or [LazyHorizontalGrid].
 *
 * This is shorthand for `GridItemSpan.FullLine`, providing a concise way to specify full-width items.
 *
 * Example:
 * ```kotlin
 * LazyVerticalGrid(columns = GridCells.Fixed(3)) {
 *   items(myList) { Text(it.name) } // Regular items
 *   item(span = fullLineSpan) { Divider() } // Full-width item
 *   items(myOtherList) { Button(onClick = {}) { Text(it.title) } } //Regular items
 * }
 * ```
 *
 * @see GridItemSpan.FullLine
 * @see LazyGridScope
 * @see androidx.compose.foundation.lazy.grid.LazyVerticalGrid
 * @see androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
 */
val LazyGridScope.fullLineSpan
    get() = _fullLineSpan


/**
 * A composable function that displays a Dialog if the `expanded` parameter is true.
 *
 * This function wraps the standard Compose [Dialog] to address potential density issues.
 * Specifically, when there is a change in the system's density and the developer has set
 * a custom density in the app, the system density in dialogs may override the custom
 * density, leading to undesirable visual effects. This function uses [CompositionLocalProvider]
 * to ensure the dialog uses the app's current density.
 *
 * @param expanded Controls the visibility of the dialog. If `false`, the dialog is not shown.
 * @param onDismissRequest Callback invoked when the user tries to dismiss the dialog.
 * @param properties [DialogProperties] for configuring the dialog's behavior.
 * @param content The content of the dialog.
 *
 * @see androidx.compose.ui.window.Dialog
 */
@Composable
@NonRestartableComposable
inline fun Dialog(
    expanded: Boolean,
    noinline onDismissRequest: () -> Unit,
    gravity: Int = Gravity.CENTER,
    properties: DialogProperties = DialogProperties(),
    crossinline content: @Composable () -> Unit
) {
    // Do not show when not expanded
    if (!expanded) return
    Dialog(onDismissRequest, properties) {
        // Enhance the dialog with additional features:
        // - Explore possibilities for true full-screen dialogs.
        // - Investigate implementing blur-behind and background blur effects.
        // - Consider adding scrim effects and other visual enhancements.
        val view = LocalView.current
        SideEffect {
            val dialogWindowProvider = view.parent as? DialogWindowProvider ?: return@SideEffect
            val window = dialogWindowProvider.window
            window.setGravity(gravity)
        }
        content()
    }
}

/**
 * A sticky header implementation that respects the top padding of the content.
 * This should be removed when an official solution is provided.
 * Currently, the only issue is that the sticky layout and the next item overlap before moving,
 * while the sticky header should start moving when the next item is about to become sticky.
 *
 * @param state The state of the LazyGrid.
 * @param key The key for the sticky header item.
 * @param contentType The type of content for the sticky header.
 * @param content The composable content for the sticky header.
 */
fun LazyListScope.stickyHeader(
    state: LazyListState,
    key: Any? = null,
    contentType: Any? = null,
    content: @Composable LazyItemScope.() -> Unit
) {
    stickyHeader(
        key = key,
        contentType = contentType,
        content = {
            // TODO - Added parameter isPinned and fix the issue causing it to overlap.
            Layout (content = { content() }) { measurables, constraints ->
                val placeable = measurables[0].measure(constraints)
                val width = constraints.constrainWidth(placeable.width)
                val height = constraints.constrainHeight(placeable.height)
                layout(width, height) {
                    val posY = coordinates?.positionInParent()?.y?.toInt() ?: 0
                    val paddingTop = state.layoutInfo.beforeContentPadding
                    var top = (paddingTop - posY).coerceIn(0, paddingTop)
                    placeable.placeRelative(0, top)
                }
            }
        }
    )
}