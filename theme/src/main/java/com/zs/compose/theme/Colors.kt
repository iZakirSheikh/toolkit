/*
 * Copyright 2025 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 27-01-2025.
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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zs.compose.foundation.Amber
import com.zs.compose.foundation.BlueLilac
import com.zs.compose.foundation.MetroGreen
import com.zs.compose.foundation.RedViolet
import com.zs.compose.foundation.Rose
import com.zs.compose.foundation.SignalWhite
import com.zs.compose.foundation.UmbraGrey
import kotlin.math.ln

// source: https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/ColorScheme.kt;bpv=0
// commit date: 2025-01-08 05:57

/**
 * Applies tonal elevation to a color based on an accent color, background color, and elevation value.
 *
 * This function calculates the alpha value for the accent color based on the elevation and
 * composites it over the background color to create a tonal elevation effect.
 *
 * @param accent the accent color to apply tonal elevation to.
 * @param background the background color to composite the accent color over.
 * @param elevation the elevation value to use in the calculation.
 * @return the color with tonal elevation applied.
 */
internal fun applyTonalElevation(accent: Color, background: Color, elevation: Dp) =
    accent.copy(alpha = ((4.5f * ln(elevation.value + 1)) + 2f) / 100f).compositeOver(background)

/**
 * Represents a set of colors used in a UI theme.
 *
 * This class defines the core color scheme for an application, including colors for accent, error, background,
 * and their corresponding "on" colors (e.g., the color of text that should be used on top of the accent color).
 * It also provides utility functions to calculate derived colors, such as shadow colors and elevated background colors.
 *
 * @property accent The primary color, used to indicate active or important elements.
 * @property onAccent The color used for content (text, icons, etc.) displayed on top of the [accent] color.
 * @property error The color used to represent errors or negative states.
 * @property onError The color used for content displayed on top of the [error] color.
 * @property background The base background color for the application.
 * @property onBackground The color used for content displayed on top of the [background] color.
 */
@Immutable
class Colors(
    val accent: Color,
    val onAccent: Color,
    val error: Color,
    val onError: Color,
    val background: Color,
    val onBackground: Color,
) {

    val isLight: Boolean get() = background.luminance() > 0.5f

    val lightShadowColor
        inline get() = if (isLight) Color.White else Color.White.copy(0.025f)
    val darkShadowColor
        inline get() = if (isLight) Color(0xFFAEAEC0).copy(0.7f)
        else Color.Black.copy(0.6f)

    /**
     * Calculates a background color with an overlay based on the provided elevation.
     *
     * @param elevation The elevation value to calculate the overlay alpha for.
     * @return A [Color] representing the background with an overlay.*/
    fun background(elevation: Dp) =
        applyTonalElevation(accent, background, if (isLight) elevation else elevation * 0.5f)

    /** Returns a copy of this ColorScheme, optionally overriding some of the values. */
    fun copy(
        accent: Color = this.accent,
        onAccent: Color = this.onAccent,
        error: Color = this.error,
        onError: Color = this.onError,
        background: Color = this.background,
        onBackground: Color = this.onBackground,
    ) = Colors(accent, onAccent, error, onError, background, onBackground)

    override fun toString(): String {
        return "Colors(accent=$accent, onAccent=$onAccent, error=$error, onError=$onError, " +
                "background=$background, onBackground=$onBackground, isLight=$isLight)"
    }

    override fun hashCode(): Int {
        var result = accent.hashCode()
        result = 31 * result + onAccent.hashCode()
        result = 31 * result + error.hashCode()
        result = 31 * result + onError.hashCode()
        result = 31 * result + background.hashCode()
        result = 31 * result + onBackground.hashCode()
        result = 31 * result + isLight.hashCode()
        result = 31 * result + lightShadowColor.hashCode()
        result = 31 * result + darkShadowColor.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Colors

        if (accent != other.accent) return false
        if (onAccent != other.onAccent) return false
        if (error != other.error) return false
        if (onError != other.onError) return false
        if (background != other.background) return false
        if (onBackground != other.onBackground) return false
        if (isLight != other.isLight) return false
        if (lightShadowColor != other.lightShadowColor) return false
        if (darkShadowColor != other.darkShadowColor) return false

        return true
    }
}

/**
 * Creates a complete color definition for the [AppTheme] using the default light theme values.
 * @see darkColors
 */
fun lightColors(
    accent: Color = Color(0xFF6750A4),
    onAccent: Color = Color.SignalWhite,
    error: Color = Color.RedViolet,
    onError: Color = Color.SignalWhite,
    background: Color = applyTonalElevation(accent, Color.White, 0.8.dp),
    onBackground: Color = Color.UmbraGrey,
) = Colors(accent, onAccent, error, onError, background, onBackground)

/**
 * @see lightColors
 */
fun darkColors(
    accent: Color = Color.Amber,
    onAccent: Color = Color.SignalWhite,
    error: Color = Color.Rose,
    onError: Color = Color.SignalWhite,
    background: Color = Color(0xFF0E0E0F),
    onBackground: Color = Color.SignalWhite,
) = Colors(accent, onAccent, error, onError, background, onBackground)

/**
 * CompositionLocal used to pass [ColorScheme] down the tree.
 *
 * Setting the value here is typically done as part of [MaterialTheme]. To retrieve the current
 * value of this CompositionLocal, use [MaterialTheme.colorScheme].
 */
internal val LocalColors = staticCompositionLocalOf { lightColors() }

/**
 * CompositionLocal containing the preferred content color for a given position in the hierarchy.
 * This typically represents the `on` color for a color in [Colors]. For example, if the
 * background color is [Colors.background], this color is typically set to
 * [Colors.onBackground].
 *
 * This color should be used for any typography / iconography, to ensure that the color of these
 * adjusts when the background color changes. For example, on a dark background, text should be
 * light, and on a light background, text should be dark.
 *
 * Defaults to [Color.Black] if no color has been explicitly set.
 */
val LocalContentColor = compositionLocalOf { Color.Black }

/**
 * The AppTheme color system contains pairs of colors that are typically used for the background and
 * content color inside a component. For example, a [Button] typically uses `accent` for its
 * background, and `onAccent` for the color of its content (usually text or iconography).
 *
 * This function tries to match the provided [backgroundColor] to a 'background' color in this
 * [Colors], and then will return the corresponding color used for content. For example, when
 * [backgroundColor] is [Colors.accent], this will return [Colors.onAccent].
 *
 * If [backgroundColor] does not match a background color in the theme, this will return
 * [Color.Unspecified].
 *
 * @return the matching content color for [backgroundColor]. If [backgroundColor] is not present in
 *   the theme's [Colors], then returns [Color.Unspecified].
 * @see contentColorFor
 */
@Stable
fun Colors.contentColorFor(backgroundColor: Color): Color =
    when (backgroundColor) {
        accent -> onAccent
        background -> onBackground
        error -> onError
        else -> Color.Unspecified
    }


/**
 * @see com.zs.compose.theme.Colors.contentColorFor
 */
@Composable
@ReadOnlyComposable
fun contentColorFor(backgroundColor: Color) =
    AppTheme.colors.contentColorFor(backgroundColor).takeOrElse { LocalContentColor.current }