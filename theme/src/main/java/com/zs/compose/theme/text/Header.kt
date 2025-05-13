/*
 * Copyright 2025 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 31-01-2025.
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

package com.zs.compose.theme.text

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.zs.compose.foundation.thenIf
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.ExperimentalThemeApi
import com.zs.compose.theme.LocalContentColor
import com.zs.compose.theme.drawHorizontalDivider

/**
 * Creates a header with an optional action.
 *
 * @param text The text to display in the header. max 2 lines one for title and other subtitle
 * @param modifier The [Modifier] to be applied to the header.
 * @param style The [TextStyle] to be applied to the header text.
 * @param contentPadding The padding to be applied around the header content.
 * @param action An optional composable function to display an action within the header.*/
@OptIn(ExperimentalThemeApi::class)
@Composable
@NonRestartableComposable
fun Header(
    text: CharSequence,
    leading: @Composable (() -> Unit) = {},
    modifier: Modifier = Modifier,
    style: TextStyle = AppTheme.typography.headline3,
    contentPadding: PaddingValues = PaddingValues.Zero,
    color: Color = LocalContentColor.current,
    drawDivider: Boolean = false,
    action: @Composable () -> Unit
) = Row(
    modifier = modifier
        .thenIf(contentPadding !== PaddingValues.Zero) { padding(contentPadding) }
        .fillMaxWidth()
        .thenIf(drawDivider) {
            drawHorizontalDivider(color = color)
                .padding(bottom = 8.dp)
        },
    // horizontalArrangement = Arrangement.spacedBy(ContentPadding.medium),
    verticalAlignment = Alignment.CenterVertically,
    content = {
        CompositionLocalProvider(LocalContentColor provides color) {
            // leading
            leading()
            // Title
            Label(
                style = style,
                text = text,
                maxLines = 2,
                modifier = Modifier.weight(1f)
            )

            // action.
            action()
        }
    }
)

/**
 * @see Header
 */
@OptIn(ExperimentalThemeApi::class)
@Composable
@NonRestartableComposable
fun Header(
    text: CharSequence,
    modifier: Modifier = Modifier,
    style: TextStyle = AppTheme.typography.headline3,
    color: Color = LocalContentColor.current,
    drawDivider: Boolean = false,
    contentPadding: PaddingValues = PaddingValues.Zero,
) = Label(
    text = text,
    modifier = modifier
        .thenIf(contentPadding !== PaddingValues.Zero) { padding(contentPadding) }
        .fillMaxWidth()
        .thenIf(drawDivider) {
            drawHorizontalDivider(color = color)
                .padding(bottom = 8.dp)
        },
    style = style,
    maxLines = 2,
    color = color
)

private val TONAL_HEADER_MARGIN = PaddingValues(0.dp, 16.dp, 0.dp, 8.dp)
private val TONAL_HEADER_SHAPE = RoundedCornerShape(3, 50, 50, 50)
private val TONAL_CHAR_HEADER_SHAPE = RoundedCornerShape(50, 25, 25, 25)

/**
 * A composable function that displays a header with a tonal background.
 *
 * This function creates a header that visually stands out with a tonal background and border.
 * It adapts its styling based on the length of the provided text.
 *
 * - For single-character text, it uses a larger, circular style.
 * - For text with multiple characters, it uses a smaller, rectangular style and supports multiple lines.
 *
 * @param text The text to display in the header.
 * @param modifier Modifier to apply to the header.
 *
 * Example Usage:
 * ```
 * TonalHeader(text = "A") // Single character header
 * TonalHeader(text = "Multiple words") // Multi-character header
 * TonalHeader(
 *    text = "Longer text that might span multiple lines",
 *    modifier = Modifier.padding(16.dp)
 * )
 * ```
 */
@Composable
fun TonalHeader(
    text: CharSequence,
    modifier: Modifier = Modifier,
) {
    when {
        text.length == 1 -> Label(
            text = text,
            style = AppTheme.typography.headline1,
            modifier = modifier
                .padding(TONAL_HEADER_MARGIN)
                .border(0.5.dp, AppTheme.colors.background(30.dp), TONAL_CHAR_HEADER_SHAPE)
                .background(AppTheme.colors.background(1.dp), TONAL_CHAR_HEADER_SHAPE)
                .padding(horizontal = 22.dp, vertical = 8.dp),
        )

        else -> Label(
            text = text,
            maxLines = 2,
            style = AppTheme.typography.title3,
            modifier = modifier
                .padding(TONAL_HEADER_MARGIN)
                .widthIn(max = 220.dp)
                .border(0.5.dp, AppTheme.colors.background(30.dp), TONAL_HEADER_SHAPE)
                .background(AppTheme.colors.background(1.dp), TONAL_HEADER_SHAPE)
                .padding(horizontal = 16.dp, vertical = 4.dp)
        )
    }
}