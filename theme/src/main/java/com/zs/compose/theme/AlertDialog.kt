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

package com.zs.compose.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.zs.compose.theme.text.LocalTextStyle
import com.zs.compose.theme.text.ProvideTextStyle

private val DialogTopBarArrangement = Arrangement.spacedBy(8.dp)
private val ContentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp)
private val FooterPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
private val FooterArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
private val TopbarArrangement = Arrangement.spacedBy(8.dp)

/**
 * [Custom Design basic dialog](https://m3.material.io/components/dialogs/overview)
 *
 * Dialogs provide important prompts in a user flow. They can require an action, communicate
 * information, or help users accomplish a task.
 *
 * [Basic dialog image](https://developer.android.com/images/reference/androidx/compose/material3/basic-dialog.png)
 * Displays an alert dialog that can be customized with an icon, title, actions, footer, and content.
 *
 * @param expanded Determines whether the dialog is currently visible. If false, the dialog is not displayed.
 * @param onDismissRequest Callback invoked when the user tries to dismiss the dialog, typically by clicking outside or pressing the back button.
 * @param icon An optional composable function to display an icon in the dialog's top bar.
 * @param title An optional composable function to display a title in the dialog's top bar.
 * @param actions An optional composable function to display action buttons in the dialog's top bar.
 * @param footer An optional composable function to display content at the bottom of the dialog. Typically used for positive/negative actions like "Ok" or "Cancel".
 * @param modifier The `Modifier` to apply to the dialog's surface.
 * @param shape The `Shape` of the dialog's surface.
 * @param backgroundColor The background color of the dialog.
 * @param contentColor The content color of the dialog.
 * @param properties The `DialogProperties` for the dialog.
 * @param content The composable function that displays the main content of the dialog.
 */
@Composable
fun AlertDialog(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null,
    footer: @Composable (RowScope.() -> Unit)? = null,
    modifier: Modifier = Modifier,
    shape: Shape = AppTheme.shapes.xSmall,
    backgroundColor: Color = AppTheme.colors.background(1.dp),
    contentColor: Color = AppTheme.colors.onBackground.copy(ContentAlpha.medium),
    properties: DialogProperties = DialogProperties(),
    content: @Composable () -> Unit
) {
    // If the dialog is not expanded, don't display it.
    if (!expanded) return

    // Use the standard Dialog composable.
    Dialog(onDismissRequest = onDismissRequest, properties = properties) {
        // Use a Surface to define the dialog's appearance.
        Surface(
            modifier = modifier.widthIn(280.dp, 560.dp),
            shape = shape,
            color = backgroundColor,
            contentColor = contentColor,
            content = {
                // Use a Column to arrange the dialog's content vertically.
                Column(
                    modifier = Modifier.height(IntrinsicSize.Min), // Make the Column wrap its content vertically.
                    content = {
                        // TopBar: Contains the icon, title, and actions.
                        if (title != null || icon != null || actions != null) { // Only show the TopBar if at least one of its elements is provided.
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(AppTheme.colors.background(4.dp)), // Apply a background color to the TopBar.
                                verticalAlignment = Alignment.CenterVertically, // Center the content vertically.
                                horizontalArrangement = TopbarArrangement, // Use a custom arrangement (defined elsewhere) for the TopBar.
                                content = {
                                    CompositionLocalProvider(
                                        LocalTextStyle provides AppTheme.typography.title2,
                                        LocalContentColor provides AppTheme.colors.onBackground.copy(
                                            ContentAlpha.high),
                                    ) {
                                        icon?.invoke() // Display the icon if provided.
                                        if (title != null) { // Display the title if provided.
                                            title()
                                            Spacer(Modifier.weight(1f)) // Push the actions to the end of the Row.
                                            actions?.invoke() // Display the actions if provided.
                                        }
                                    }
                                }
                            )
                        }

                        // Main content: The main content of the dialog.
                        Box(
                            Modifier
                                .weight(1f) // Allow the content to expand vertically.
                                .fillMaxWidth() // Fill the available width.
                                .padding(ContentPadding), // Apply padding to the content.
                            content = {
                                ProvideTextStyle(AppTheme.typography.body2, content)
                            } // Display the provided content.
                        )

                        // Footer: Contains buttons or other secondary actions.
                        if (footer != null) { // Only show the Footer if it's provided.
                            Row(
                                horizontalArrangement = FooterArrangement, // Use a custom arrangement (defined elsewhere) for the Footer.
                                content = footer, // Display the provided footer content.
                                modifier = Modifier
                                    .padding(FooterPadding) // Apply padding to the Footer.
                                    .fillMaxWidth() // Fill the available width.
                            )
                        }
                    }
                )
            }
        )
    }
}