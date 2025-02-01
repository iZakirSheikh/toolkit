/*
 * Copyright 2025 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 28-01-2025.
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

import androidx.annotation.FloatRange
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.luminance

/**
 * Default alpha levels used by AppTheme components.
 */
object ContentAlpha {
    /**
     * A high level of content alpha, used to represent high emphasis text such as input text in a
     * selected [TextField].
     */
    val high: Float
        @Composable
        get() =
            contentAlpha(
                highContrastAlpha = HighContrastContentAlpha.high,
                lowContrastAlpha = LowContrastContentAlpha.high
            )

    /**
     * A medium level of content alpha, used to represent medium emphasis text such as placeholder
     * text in a [TextField].
     */
    val medium: Float
        @Composable
        get() =
            contentAlpha(
                highContrastAlpha = HighContrastContentAlpha.medium,
                lowContrastAlpha = LowContrastContentAlpha.medium
            )

    /**
     * A low level of content alpha used to represent disabled components, such as text in a
     * disabled [Button].
     */
    val disabled: Float
        @Composable
        get() =
            contentAlpha(
                highContrastAlpha = HighContrastContentAlpha.disabled,
                lowContrastAlpha = LowContrastContentAlpha.disabled
            )

    /**
     * This default implementation uses separate alpha levels depending on the luminance of the
     * incoming color, and whether the theme is light or dark. This is to ensure correct contrast
     * and accessibility on all surfaces.
     *
     * See [HighContrastContentAlpha] and [LowContrastContentAlpha] for what the levels are used
     * for, and under what circumstances.
     */
    @Composable
    private fun contentAlpha(
        @FloatRange(from = 0.0, to = 1.0) highContrastAlpha: Float,
        @FloatRange(from = 0.0, to = 1.0) lowContrastAlpha: Float
    ): Float {
        val contentColor = LocalContentColor.current
        val lightTheme = AppTheme.colors.isLight
        return if (lightTheme) {
            if (contentColor.luminance() > 0.5) highContrastAlpha else lowContrastAlpha
        } else {
            if (contentColor.luminance() < 0.5) highContrastAlpha else lowContrastAlpha
        }
    }
}

/**
 * Alpha levels for high luminance content in light theme, or low luminance content in dark theme.
 *
 * This content will typically be placed on colored surfaces, so it is important that the contrast
 * here is higher to meet accessibility standards, and increase legibility.
 *
 * These levels are typically used for text / iconography in primary colored tabs / bottom
 * navigation / etc.
 */
private object HighContrastContentAlpha {
    const val high: Float = 1.00f
    const val medium: Float = 0.74f
    const val disabled: Float = 0.38f
}

/**
 * Alpha levels for low luminance content in light theme, or high luminance content in dark theme.
 *
 * This content will typically be placed on grayscale surfaces, so the contrast here can be lower
 * without sacrificing accessibility and legibility.
 *
 * These levels are typically used for body text on the main surface (white in light theme, grey in
 * dark theme) and text / iconography in surface colored tabs / bottom navigation / etc.
 */
private object LowContrastContentAlpha {
    const val high: Float = 0.87f
    const val medium: Float = 0.60f
    const val disabled: Float = 0.38f
}
