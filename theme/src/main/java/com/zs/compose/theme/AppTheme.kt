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

package com.zs.compose.theme

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import com.zs.compose.theme.text.ProvideTextStyle

// source: https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/MaterialTheme.kt;bpv=0
// commit date: 2024-09-26 23:43

/**
 * Contains functions to access the current theme values provided at the call site's position in the
 * hierarchy.
 */
object AppTheme {
    /**
     * Retrieves the current [Colors] at the call site's position in the hierarchy.
     *
     * @sample androidx.compose.material3.samples.ThemeColorSample
     */
    val colors: Colors
        @Composable @ReadOnlyComposable get() = LocalColors.current

    /**
     * Retrieves the current [Typography] at the call site's position in the hierarchy.
     *
     * @sample androidx.compose.material3.samples.ThemeTextStyleSample
     */
    val typography: Typography
        @Composable @ReadOnlyComposable get() = LocalTypography.current

    /**
     * Retrieves the current [Shapes] at the call site's position in the hierarchy.
     *
     * @sample androidx.compose.material3.samples.ThemeShapeSample
     */
    val shapes: Shapes
        @Composable @ReadOnlyComposable get() = LocalShapes.current


    @Composable
    operator fun invoke(
        colors: Colors = AppTheme.colors,
        shapes: Shapes = AppTheme.shapes,
        typography: Typography = AppTheme.typography,
        content: @Composable () -> Unit
    ) {
        val rippleIndication = ripple()
        val selectionColors = rememberTextSelectionColors(colors)
        CompositionLocalProvider(
            LocalColors provides colors,
            LocalIndication provides rippleIndication,
            LocalShapes provides shapes,
            LocalTextSelectionColors provides selectionColors,
            LocalTypography provides typography,
        ) {
            ProvideTextStyle(value = typography.body1, content = content)
        }
    }
}

@Composable
/*@VisibleForTesting*/
internal fun rememberTextSelectionColors(colorScheme: Colors): TextSelectionColors {
    val primaryColor = colorScheme.accent
    return remember(primaryColor) {
        TextSelectionColors(
            handleColor = primaryColor,
            backgroundColor = primaryColor.copy(alpha = TextSelectionBackgroundOpacity),
        )
    }
}

/*@VisibleForTesting*/
internal const val TextSelectionBackgroundOpacity = 0.4f

