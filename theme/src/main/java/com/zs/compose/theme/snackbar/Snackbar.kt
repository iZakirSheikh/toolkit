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

@file:OptIn(ExperimentalThemeApi::class, ExperimentalFoundationApi::class)

package com.zs.compose.theme.snackbar

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zs.compose.foundation.Background
import com.zs.compose.foundation.ImageBrush
import com.zs.compose.foundation.SignalWhite
import com.zs.compose.foundation.background
import com.zs.compose.foundation.composableIf
import com.zs.compose.foundation.fadingEdge
import com.zs.compose.foundation.thenIf
import com.zs.compose.foundation.visualEffect
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.Button
import com.zs.compose.theme.ButtonDefaults
import com.zs.compose.theme.Colors
import com.zs.compose.theme.DismissValue
import com.zs.compose.theme.ExperimentalThemeApi
import com.zs.compose.theme.Icon
import com.zs.compose.theme.OutlinedButton
import com.zs.compose.theme.SwipeToDismiss
import com.zs.compose.theme.TextButton
import com.zs.compose.theme.internal.FractionalThreshold
import com.zs.compose.theme.rememberDismissState
import com.zs.compose.theme.text.Label
import com.zs.compose.theme.BaseListItem as Layout

private inline val Colors.snackBackgroundColor
    get() = if (isLight) Color(0xFF0E0E0F) else background(1.dp)

private val Colors.snackbarBorder
    get() = BorderStroke(
        1.dp,
        Brush.linearGradient(
            listOf(
                Color.Gray.copy(if (!isLight) 0.24f else 0.48f),
                Color.Transparent,
                Color.Transparent,
                Color.Gray.copy(if (!isLight) 0.24f else 0.48f),
            )
        )
    )

private val SnackBarSize =
    Modifier.sizeIn(360.dp, 56.dp, 450.dp, 250.dp)
private val SnackbarHorizontalMargin = 18.dp

private val SnackButtonScale = Modifier.scale(0.85f)
private val SPACING = 4.dp
private val DismissThreshHold = FractionalThreshold(0.75f)
private val SnackNoiseModifier =
    Modifier.visualEffect(ImageBrush.NoiseBrush, 0.08f, overlay = false, blendMode = BlendMode.Exclusion)

private val SnackMessageMaxHeight = 190.dp
private val SnackbarShape =  RoundedCornerShape(10)
private val ExpandedContentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)

/**
 * Represents a Snackbar component for [SnackbarHost]
 */

@Composable
@ExperimentalThemeApi
fun Snackbar(
    value: SnackbarData,
    modifier: Modifier = Modifier,
    background: Background = Background.Unspecified,
    contentColor: Color = Color.SignalWhite,
    shape: Shape = SnackbarShape,
    border: BorderStroke? = AppTheme.colors.snackbarBorder,
    actionColor: Color = value.accent.takeOrElse { AppTheme.colors.accent },
) {
    // A message is critical if duration is indefinite and action is not null.
    // critical messages are not dismissible by the swipe and must expand to include cancel button.
    val critical = value.duration == SnackbarDuration.Indefinite && value.action != null
    // initially snackbar is not expanded; it is expandable if message is critical or text is long.
    var expanded: Boolean by remember { mutableStateOf(false) }
    // TODO - Add this in future.
    //  Handle back press to dismiss expanded Toast or the entire Toast
    // BackHandler(isExpanded) { isExpanded = !isExpanded }
    val dismissState = rememberDismissState(
        confirmStateChange = {
            // Dismiss only if not expanded or critical and expanded
            if (critical || expanded || it == DismissValue.DismissedToEnd) return@rememberDismissState false
            // Execute action if confirmed
            value.dismiss()
            true
        }
    )
    val colors = AppTheme.colors
    val background = background.takeOrElse {
        thenIf(!expanded) {
                drawWithContent {
                    drawContent()
                    drawRect(color = actionColor, size = size.copy(width = 3.dp.toPx()))
                }
            }
            .background(colors.snackBackgroundColor)
            .then(SnackNoiseModifier)
    }

    //
    // SwipeToDismiss composable for handling swipe gesture
    SwipeToDismiss(
        dismissState,
        background = { },
        modifier = Modifier.animateContentSize(),
        dismissThresholds = { DismissThreshHold },
        dismissContent = {
            Layout(
                contentColor = contentColor,
                spacing = SPACING,
                modifier = modifier
                    .then(SnackBarSize)
                    .padding(horizontal = SnackbarHorizontalMargin)
                    .shadow(6.dp, shape, clip = true)
                    // Toggle expanded state on click
                    .clickable(
                        indication = null,
                        interactionSource = null,
                        enabled = critical || value.message.length > 100 || value.action == null,
                        onClick = { expanded = !expanded }
                    )
                    .thenIf(border != null) { border(border!!, shape) }
                    .background(background),
                leading = composableIf(value.icon != null) {
                    Icon(
                        imageVector = value.icon!!,
                        contentDescription = null,
                        tint = actionColor,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                },
                trailing = composableIf(value.action != null && !expanded) {
                    OutlinedButton(
                        text = value.action!!,
                        onClick = value::action,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = actionColor,
                            backgroundColor = Color.Transparent,
                        ),
                        shape = CircleShape,
                        modifier = SnackButtonScale,
                        border = BorderStroke(
                            ButtonDefaults.OutlinedBorderSize, contentColor.copy(
                                ButtonDefaults.OutlinedBorderOpacity
                            )
                        )
                    )
                },
                // Message
                heading = {
                    Label(
                        text = value.message,
                        color = contentColor,
                        style = AppTheme.typography.body2,
                        maxLines = if (!expanded) 3 else Int.MAX_VALUE,  // Limit lines when not expanded
                        modifier = Modifier
                            .heightIn(max = SnackMessageMaxHeight)     // Max height constraint
                            .thenIf(expanded) {
                                val state = rememberScrollState()
                                fadingEdge(state, false, 10.dp)
                                    .verticalScroll(state)
                                    .padding(bottom = 4.dp)
                            }
                    )
                },
                // Footer with action buttons when expanded
                footer = composableIf(expanded) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth(),
                        content = {
                            // Cancel button
                            TextButton(
                                stringResource(android.R.string.cancel).uppercase(),
                                value::dismiss,
                                modifier = SnackButtonScale,
                                colors = ButtonDefaults.textButtonColors(contentColor = contentColor),
                            )

                            // Action button if available
                            val action = value.action
                            if (action != null)
                                Button(
                                    text = action,
                                    onClick = value::action,
                                    colors = ButtonDefaults.buttonColors(
                                        contentColor = actionColor,
                                        backgroundColor = actionColor.copy(0.15f)
                                    ),
                                    modifier = SnackButtonScale,
                                    elevation = null
                                )
                        }
                    )
                }
            )
        }
    )
}