/*
 * Copyright 2025 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 11-02-2025.
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

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.toComposeRect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.window.layout.WindowMetricsCalculator

private const val TAG = "WindowSize"

/**
 * Represents the size of a window in terms of reach categories (Compact, Medium, Large, xLarge).
 *
 * @param value The size of the window in DpSize.
 * @property width The standard width [Category].
 * @property height The standard height [Category].
 * @property rawValue The raw value of the window size as [DpSize]
 */
@Immutable
@JvmInline
value class WindowSize internal constructor(val value: DpSize) {

    constructor(width: Dp, height: Dp) : this(DpSize(width, height))

    /**
     * Represents different categories based on screen width or height.
     *
     * @property Small Indicates a compact screen size, typically for smaller devices like phones. Breakpoint: 300- 400 dp
     * @property Medium Indicates a medium screen size, typically for tablets or small laptops. Breakpoint: 401-650 dp
     * @property Large Indicates a large screen size, typically for laptops or desktops. Breakpoint: 650-900 dp
     * @property xLarge Indicates a very large screen size, typically for external monitors. Breakpoint: 900 dp and above
     */
    enum class Category {
        Small,
        Medium,
        Large,
        xLarge
    }

    val width: Category
        get() {
            require(value.width >= 0.dp) { "Width must not be negative" }
            return when {
                value.width <= 500.dp -> Category.Small
                value.width <= 700.dp -> Category.Medium
                value.width <= 900.dp -> Category.Large
                else -> Category.xLarge
            }
        }
    val height: Category
        get() {
            require(value.height >= 0.dp) { "Height must not be negative" }
            return when {
                value.height <= 500.dp -> Category.Small
                value.height <= 700.dp -> Category.Medium
                value.height <= 900.dp -> Category.Large
                else -> Category.xLarge
            }
        }

    operator fun component1() = width
    operator fun component2() = height

    /**
     * Consumes a specified amount of width and/or height from the current [WindowSize] and returns a new [WindowSize] with the adjusted dimensions.
     *
     * @param amount The [DpSize] to consume, representing the amount to subtract from both width and height.
     * @return A new [WindowSize] with the consumed amount subtracted from the original [value].
     */
    fun consume(amount: DpSize): WindowSize = WindowSize(value - amount)

    /**
     * @see consume
     */
    fun consume(width: Dp = 0.dp, height: Dp = 0.dp) = consume(DpSize(width, height))

    val rawValue get() = value
}

/**
 * Calculates the window's [WindowSize] for the provided [activity].
 *
 * A new [WindowSize] will be returned whenever a configuration change causes the width or
 * height of the window to cross a breakpoint, such as when the device is rotated or the window
 * is resized.
 *
 * @sample androidx.compose.material3.windowsizeclass.samples.AndroidWindowSizeClassSample
 * @param activity The [Activity] for which the window size is calculated.
 * @return The [WindowSize] corresponding to the current width and height.
 */
@Composable
@ReadOnlyComposable
fun calculateWindowSizeClass(activity: Activity): WindowSize {
    // Observe view configuration changes and recalculate the size class on each change. We can't
    // use Activity#onConfigurationChanged as this will sometimes fail to be called on different
    // API levels, hence why this function needs to be @Composable so we can observe the
    // ComposeView's configuration changes.
    LocalConfiguration.current
    val density = LocalDensity.current
    val metrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(activity)
    val size = with(density) { metrics.bounds.toComposeRect().size.toDpSize() }
    return WindowSize(size)
}

/**
 * [CompositionLocal] containing the [WindowSize].
 *
 * This [CompositionLocal] is used to access the current [WindowSize] within a composition.
 * If no [WindowSize] is found in the composition hierarchy, an error will be thrown.
 *
 * Usage:
 * ```
 * val windowSize = LocalWindowSize.current
 * // Use the windowSize value within the composition
 * ```
 * @optIn ExperimentalMaterial3WindowSizeClassApi
 */
val LocalWindowSize = compositionLocalOf<WindowSize> {
    error("No Window size defined.")
}