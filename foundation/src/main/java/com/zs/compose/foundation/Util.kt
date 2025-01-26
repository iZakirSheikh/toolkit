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

import android.util.Log
import androidx.compose.runtime.Composable
import java.io.Closeable

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
fun composableIf(condition: Boolean, content: @Composable () -> Unit) =
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

