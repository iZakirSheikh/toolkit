/*
 * Copyright 2025 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 08-02-2025.
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
 
package com.zs.compose.theme.snackbar

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zs.compose.foundation.ImageBrush
import com.zs.compose.foundation.SignalWhite
import com.zs.compose.foundation.composableIf
import com.zs.compose.foundation.fadingEdge
import com.zs.compose.foundation.thenIf
import com.zs.compose.foundation.visualEffect
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.BaseListItem
import com.zs.compose.theme.ButtonDefaults
import com.zs.compose.theme.Colors
import com.zs.compose.theme.DismissValue
import com.zs.compose.theme.ExperimentalThemeApi
import com.zs.compose.theme.FilledTonalButton
import com.zs.compose.theme.Icon
import com.zs.compose.theme.SwipeToDismiss
import com.zs.compose.theme.TextButton
import com.zs.compose.theme.internal.FractionalThreshold
import com.zs.compose.theme.rememberDismissState
import com.zs.compose.theme.text.Label

private inline val Colors.toastBackgroundColor
    @Composable
    @ReadOnlyComposable
    get() = if (isLight) Color(0xFF0E0E0F) else AppTheme.colors.background(1.dp)

/**
 * Represents a Snackbar component for [SnackbarHost]
 */
@Composable
@ExperimentalThemeApi
internal fun Snackbar(
    value: SnackbarData,
    modifier: Modifier = Modifier,
    backgroundColor: Color = AppTheme.colors.toastBackgroundColor,
    contentColor: Color = Color.SignalWhite,
    actionColor: Color = value.accent.takeOrElse { AppTheme.colors.accent },
) {
    // State to track if Toast is expanded
    val critical = value.duration == SnackbarDuration.Indefinite
    var isExpanded: Boolean by remember { mutableStateOf(critical) }
    // Handle back press to dismiss expanded Toast or the entire Toast
    // BackHandler(isExpanded) { isExpanded = !isExpanded }
    // State for swipe-to-dismiss gesture

    val dismissState = rememberDismissState(
        confirmStateChange = {
            // Dismiss only if not expanded or critical and expanded
            if (critical || isExpanded || it == DismissValue.DismissedToEnd) return@rememberDismissState false
            // Execute action if confirmed
            value.dismiss()
            true
        }
    )
    val colors = AppTheme.colors
    // SwipeToDismiss composable for handling swipe gesture
    SwipeToDismiss(
        dismissState,
        background = { },
        dismissThresholds = { FractionalThreshold(0.75f) },
        modifier = modifier
//            .renderInSharedTransitionScopeOverlay(0.3f)
            .animateContentSize()
            ,
        dismissContent = {
            // Shape of the Toast based on expanded state
            val shape = if (isExpanded) AppTheme.shapes.small else AppTheme.shapes.xSmall
            BaseListItem(
                contentColor = contentColor,
                spacing = 4.dp,
                leading = composableIf(value.icon != null) {
                    // FixMe: It might cause problems.
                    val icon = value.icon!!
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = actionColor,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                },
                // Trailing action button if available and not expanded
                trailing = composableIf(value.action != null && !isExpanded) {
                    TextButton(
                        text = value.action!!,
                        onClick = value::action,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = actionColor,
                            backgroundColor = Color.Transparent,
                        ),
                        shape = CircleShape,
                        modifier = Modifier.height(37.dp),
                    )
                },
                // Toast message
                heading = {
                    Label(
                        text = value.message,
                        color = contentColor,
                        style = AppTheme.typography.body2,
                        // Limit lines when not expanded
                        maxLines = if (!isExpanded) 3 else Int.MAX_VALUE,
                        modifier = Modifier
                            // Max height constraint
                            .heightIn(max = 200.dp)
                            .thenIf(isExpanded) {
                                val state = rememberScrollState()
                                fadingEdge( state, false, 16.dp)
                                    .verticalScroll(state)
                            }
                    )
                },
                // Footer with action buttons when expanded
                footer = composableIf(isExpanded) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.thenIf(value.icon != null) { padding(start = 20.dp) }.fillMaxWidth(),
                        content = {
                            // Action button if available
                            val action = value.action
                            if (action != null)
                                FilledTonalButton(
                                    text = action,
                                    onClick = value::action,
                                    colors = ButtonDefaults.buttonColors(
                                        contentColor = actionColor,
                                        backgroundColor = actionColor.copy(0.2f).compositeOver(
                                            AppTheme.colors.toastBackgroundColor
                                        )
                                    ),
                                    modifier = Modifier.height(37.dp),
                                )
                            // Cancel button
                            TextButton(
                                stringResource(android.R.string.cancel).uppercase(),
                                value::dismiss,
                                modifier = Modifier.height(37.dp),
                                colors = ButtonDefaults.textButtonColors(contentColor = contentColor),
                            )
                        }
                    )
                },
                modifier = Modifier
                    .padding(horizontal = 18.dp)
                    .shadow(6.dp, shape, clip = true)
                    // Toggle expanded state on click
                    .clickable(indication = null, interactionSource = null, enabled = !critical && value.message.length > 100) {
                        isExpanded = !isExpanded
                    }
                    // Apply border and visual effect if dark theme
                    .thenIf(!isExpanded) {
                        drawWithContent {
                            drawContent()
                            drawRect(color = actionColor, size = size.copy(width = 3.dp.toPx()))
                        }
                    }
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            listOf(
                                Color.Gray.copy(if(!colors.isLight) 0.24f else 0.48f),
                                Color.Transparent,
                                Color.Transparent,
                                Color.Gray.copy(if(!colors.isLight) 0.24f else 0.48f),
                            )
                        ),
                        shape
                    )
                    .visualEffect(ImageBrush.NoiseBrush, 0.60f, overlay = true)
                    .background(backgroundColor)
                    //.clip(shape)
                    .sizeIn(360.dp, 56.dp, 400.dp, 340.dp)
            )
        }
    )
}