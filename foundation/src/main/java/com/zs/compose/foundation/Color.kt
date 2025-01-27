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

import androidx.annotation.FloatRange
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

private const val TAG = "Color"


private val CapriBlue = Color(0xFF0D47A1)
private val SkyBlue = Color(0xFF1A73E8)
private val LightBlue = Color(0xFF03A9F4)
private val Orange = Color(0xFFF4491f)
private val Rose = Color(0xFFE92635)
private val OrientRed = Color(0xffae1c27)
private val RedViolet = Color(0xFF991c37)
private val ClaretViolet = Color(0xFF740945)
private val Magenta = Color(0xFFE91E63)
private val SignalBlue = Color(0xFF4A148C)
private val AzureBlue = Color(0xFF006064)
private val MetroGreen = Color(0xFF00A300)
private val MetroGreen2 = Color(0xFF4CAF50)
private val OliveYellow = Color(0xFF8BC34A)
private val Ivory = Color(0xFFCDDC39)
private val TrafficYellow = Color(0xFFffc107)
private val DahliaYellow = Color(0xFFff9800)
private val Amber = Color(0xFFFF6F00)
private val BlackOlive = Color(0xFF383838)
private val SepiaBrown = Color(0xFF38220f)
private val UmbraGrey = Color(0xFF333333)
private val SignalWhite = Color(0xFFF2F2F2)
private val JetBlack = Color(0xFF121114)
private val TrafficBlack = Color(0xFF1D1D1E)

val Color.Companion.UmbraGrey
    get() = com.zs.compose.foundation.UmbraGrey
val Color.Companion.SignalWhite
    get() = com.zs.compose.foundation.SignalWhite
val Color.Companion.Amber
    get() = com.zs.compose.foundation.Amber
val Color.Companion.Orange
    get() = com.zs.compose.foundation.Orange
val Color.Companion.Rose
    get() = com.zs.compose.foundation.Rose
val Color.Companion.RedViolet
    get() = com.zs.compose.foundation.RedViolet
val Color.Companion.ClaretViolet
    get() = com.zs.compose.foundation.ClaretViolet
val Color.Companion.MetroGreen2
    get() = com.zs.compose.foundation.MetroGreen2
val Color.Companion.MetroGreen
    get() = com.zs.compose.foundation.MetroGreen
val Color.Companion.LightBlue
    get() = com.zs.compose.foundation.LightBlue
val Color.Companion.SkyBlue
    get() = com.zs.compose.foundation.SkyBlue
val Color.Companion.BlueLilac
    get() = SignalBlue
val Color.Companion.CapriBlue
    get() = com.zs.compose.foundation.CapriBlue
val Color.Companion.AzureBlue
    get() = com.zs.compose.foundation.AzureBlue
val Color.Companion.TrafficYellow
    get() = com.zs.compose.foundation.TrafficYellow
val Color.Companion.DahliaYellow
    get() = com.zs.compose.foundation.DahliaYellow
val Color.Companion.BlackOlive
    get() = com.zs.compose.foundation.BlackOlive
val Color.Companion.SepiaBrown
    get() = com.zs.compose.foundation.SepiaBrown
val Color.Companion.OrientRed
    get() = com.zs.compose.foundation.OrientRed
val Color.Companion.Ivory
    get() = com.zs.compose.foundation.Ivory
val Color.Companion.OliveYellow
    get() = com.zs.compose.foundation.OliveYellow
val Color.Companion.Magenta
    get() = com.zs.compose.foundation.Magenta
val Color.Companion.JetBlack
    get() = com.zs.compose.foundation.JetBlack
val Color.Companion.TrafficBlack
    get() = com.zs.compose.foundation.TrafficBlack


/**
 * Returns a new [Color] with modified components.
 *
 * Returns a new color based on this color, but with optional changes to hue, saturation, lightness, and alpha.
 * Unspecified components (using [Float.NaN]) will retain their original values.
 *
 * @param hue         The new hue (0.0-360.0), or [Float.NaN] to keep the original.
 * @param saturation  The new saturation (0.0-1.0), or [Float.NaN] to keep the original.
 * @param lightness   The new lightness (0.0-1.0), or [Float.NaN] to keep the original.
 * @param alpha       The new alpha (0.0-1.0), or [Float.NaN] to keep the original.
 * @return A new [Color] with the updated components.
 */
fun Color.copy(
    hue: Float = Float.NaN,
    saturation: Float = Float.NaN,
    lightness: Float = Float.NaN,
    alpha: Float = Float.NaN
): Color {
    // val hsl[0] =
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(toArgb(), hsl)
    return Color.hsl(
        hue = if (!hue.isNaN()) hue else hsl[0],
        saturation = if (!saturation.isNaN()) saturation else hsl[1],
        lightness = if (!lightness.isNaN()) lightness else hsl[2],
        alpha = if (!alpha.isNaN()) alpha else hsl[3]
    )
}

/**
 * Blends this color with another color using a given ratio.
 *
 * This function performs a linear interpolation between two colors, based on the
 * provided `ratio`. A ratio of 0.0 returns this color, a ratio of 1.0 returns
 * the `color` parameter, and values in between return a blend of the two.
 *
 * The blending is done separately for each color channel (red, green, blue, and alpha).
 *
 * @param color The color to blend with this color.
 * @param ratio The ratio of the blend. A value of 0.0 represents this color,
 *              a value of 1.0 represents the `color` parameter.
 *              Values between 0.0 and 1.0 represent a linear interpolation between
 *              the two colors.
 * @return A new [Color] instance representing the blended color.
 * @throws IllegalArgumentException if the ratio is not within the range [0.0, 1.0] (Checked by @FloatRange)
 */
fun Color.blend(color: Color, @FloatRange(from = 0.0, to = 1.0) ratio: Float): Color {
    val inverseRatio = 1 - ratio
    val a = alpha * inverseRatio + color.alpha * ratio
    val r = red * inverseRatio + color.red * ratio
    val g = green * inverseRatio + color.green * ratio
    val b = blue * inverseRatio + color.blue * ratio
    return Color(r, g, b, a, colorSpace = ColorSpaces.Srgb)
}
