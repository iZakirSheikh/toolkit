/*
 * Copyright 2025 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 30-01-2025.
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

@file:OptIn(ExperimentalFoundationApi::class, ExperimentalThemeApi::class)

package com.zs.compose.theme

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.zs.compose.foundation.Background
import com.zs.compose.theme.text.ProvideTextStyle

private val FooterArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
private val FooterPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
private val ContentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp)
private val ContentSpacing = Arrangement.spacedBy(8.dp, )

private val MinWidth = Modifier.widthIn(280.dp, 560.dp)

/**
 * AlertDialog is a composable that displays a dialog with a title, content, and optional actions.
 * It's built on top of `androidx.compose.ui.window.Dialog` and `Surface` to provide a customizable
 * dialog experience.
 *
 * @param onDismissRequest Callback that is invoked when the user requests to dismiss the dialog,
 * such as by tapping outside the dialog or pressing the back button.
 * @param topBar Optional composable lambda for the dialog's title area. Typically a [TopAppBar] composable.
 * @param bottomBar Optional composable lambda for the dialog's actions area.
 * This is a `RowScope`, so you can lay out buttons or other controls horizontally.
 * @param shape The shape of the dialog's container. Defaults to `AppTheme.shapes.xSmall`.
 * @param background The background of the dialog. Defaults to `Background(AppTheme.colors.background(1.dp))`.
 * @param contentColor The preferred color for content inside the dialog.
 * Defaults to `AppTheme.colors.onBackground.copy(ContentAlpha.medium)`.
 * @param properties Properties to customize the behavior of the dialog.
 * See `androidx.compose.ui.window.DialogProperties`.
 * @param content The main content of the dialog. This is a `ColumnScope`, allowing you to arrange
 * elements vertically.
 */
@Composable
fun AlertDialog(
    onDismissRequest: () -> Unit,
    topBar: @Composable (() -> Unit)? = null,
    bottomBar: @Composable (RowScope.() -> Unit)? = {},
    shape: Shape = AppTheme.shapes.xSmall,
    background: Background = Background(AppTheme.colors.background(1.dp)),
    contentColor: Color = AppTheme.colors.onBackground.copy(ContentAlpha.medium),
    properties: DialogProperties = DialogProperties(),
    content: @Composable ColumnScope.() -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest, properties) {
        // Use a Surface to define the dialog's appearance.
        Surface(
            modifier = MinWidth,
            shape = shape,
            background = background,
            contentColor = contentColor,
            content = {
                // Use a Column to arrange the dialog's content vertically.
                Column(
                    // Make the Column wrap its content vertically.
                    modifier = Modifier.height(IntrinsicSize.Min),
                    content = {
                        topBar?.invoke()

                        // Main content: The main content of the dialog.
                        ProvideTextStyle(AppTheme.typography.body2) {
                            Column(
                                Modifier
                                    .weight(1f) // Allow the content to expand vertically.
                                    .fillMaxWidth() // Fill the available width.
                                    .padding(ContentPadding), // Apply padding to the content.
                                content = content,
                                verticalArrangement = ContentSpacing
                            )
                        }

                        // Footer: Contains buttons or other secondary actions.
                        // Only show the Footer if it's provided.
                        if (bottomBar != null) {
                            // Display the provided footer content.
                            // TODO - Maybe instead of fullMaxWidth; make it align end.
                            Row(
                                horizontalArrangement = FooterArrangement,
                                content = bottomBar,
                                modifier = Modifier
                                    .padding(FooterPadding)
                                    .fillMaxWidth()
                            )
                        }
                    }
                )
            }
        )
    }
}

/**
 * @see AlertDialog
 */
@Composable
@NonRestartableComposable
fun AlertDialog(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    topBar: @Composable (() -> Unit)? = null,
    bottomBar: @Composable (RowScope.() -> Unit)? = {},
    shape: Shape = AppTheme.shapes.xSmall,
    background: Background = Background(AppTheme.colors.background(1.dp)),
    contentColor: Color = AppTheme.colors.onBackground.copy(ContentAlpha.medium),
    properties: DialogProperties = DialogProperties(),
    content: @Composable ColumnScope.() -> Unit
) {
    if (!expanded)
        return
    AlertDialog(
        onDismissRequest,
        topBar,
        bottomBar,
        shape,
        background,
        contentColor,
        properties,
        content
    )
}