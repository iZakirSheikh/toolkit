/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 25-01-2024.
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

@file:Suppress("TransitionPropertiesLabel")

package com.primex.material2.menu

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenu
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.takeOrElse
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

private const val TAG = "DropDownMenu2"

/*=================== Menu Defaults ======================*/
private val MenuElevation = 8.dp

internal val DropdownMenuVerticalPadding = 8.dp

// Menu open/close animation.
internal const val InTransitionDuration = 120
internal const val OutTransitionDuration = 75

@Composable
internal inline fun Content(
    expandedStates: MutableTransitionState<Boolean>,
    transformOriginState: MutableState<TransformOrigin>,
    modifier: Modifier = Modifier,
    elevation: Dp = Dp.Unspecified,
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = MaterialTheme.colors.onSurface,
    shape: Shape = MaterialTheme.shapes.small,
    border: BorderStroke? = null,
    noinline content: @Composable () -> Unit
) {
    // Menu open/close animation.
    val transition = rememberTransition(expandedStates, "DropDownMenu")

    val scale by transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) {
                // Dismissed to expanded
                tween(
                    durationMillis = InTransitionDuration,
                    easing = LinearOutSlowInEasing
                )
            } else {
                // Expanded to dismissed.
                tween(
                    durationMillis = 1,
                    delayMillis = OutTransitionDuration - 1
                )
            }
        }
    ) {
        if (it) {
            // Menu is expanded.
            1f
        } else {
            // Menu is dismissed.
            0.8f
        }
    }

    val alpha by transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) {
                // Dismissed to expanded
                tween(durationMillis = 30)
            } else {
                // Expanded to dismissed.
                tween(durationMillis = OutTransitionDuration)
            }
        }
    ) {
        if (it) {
            // Menu is expanded.
            1f
        } else {
            // Menu is dismissed.
            0f
        }
    }
    Surface(
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.alpha = alpha
            transformOrigin = transformOriginState.value
        } then modifier,
        elevation = elevation.takeOrElse { MenuElevation },
        contentColor = contentColor,
        content = content,
        color = backgroundColor,
        border = border,
        shape = shape,
    )
}


/**
 * A composable function that creates a customizable [DropdownMenu] with various appearance and behavior options.
 * The popup window can serve as a dropdown menu, tooltip, dialog, or any other overlay component.
 * It automatically animates when shown or dismissed and can be closed by clicking outside or pressing the back button.
 *
 * @param expanded A boolean state controlling the visibility of the popup window. When true, the window is visible; when false, it's hidden.
 * @param onDismissRequest A callback invoked when the user attempts to dismiss the popup. This can occur when clicking outside, pressing the back button, or manually calling [PopupScope.dismiss].
 * @param offset A [DpOffset] specifying the popup's position relative to its parent. Default is (0.dp, 0.dp), aligning the top-left corner with its parent.
 * @param elevation A [Dp] specifying the popup's elevation, affecting its shadow. Default is [Dp.Unspecified], using [PopupDefaults.elevation].
 * @param backgroundColor A [Color] setting the popup's background color. Default is [MaterialTheme.colors.surface], adapting to the current theme.
 * @param contentColor A [Color] setting the content color of the popup window.
 * @param shape A [Shape] defining the popup's shape. Default is [MaterialTheme.shapes.small].
 * @param border A [BorderStroke] specifying the border appearance of the popup.
 * @param properties Additional [PopupProperties] to customize the popup's behavior, with focusability enabled by default.
 * @param content The content of the popup, defined as a composable lambda.
 *
 * Example usage:
 * ```kotlin
 * DropDownMenu(
 *     expanded = isPopupVisible,
 *     onDismissRequest = { isPopupVisible = false },
 *     modifier: Modifier = Modifier,
 *     offset = DpOffset(8.dp, 16.dp),
 *     elevation = 4.dp,
 *     backgroundColor = Color.White,
 *     contentColor = Color.Black,
 *     shape = RoundedCornerShape(8.dp),
 *     border = BorderStroke(1.dp, Color.Gray),
 *     properties = PopupProperties(focusable = true),
 * ) {
 *     // Content of the popup
 *     Text("Hello, Popup!")
 * }
 * ```
 * @see DropdownMenu
 */
@Composable
fun DropDownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    elevation: Dp = Dp.Unspecified,
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = MaterialTheme.colors.onSurface,
    shape: Shape = MaterialTheme.shapes.small,
    border: BorderStroke? = null,
    properties: PopupProperties = PopupProperties(focusable = true),
    content: @Composable () -> Unit
) {
    val expandedStates = remember { MutableTransitionState(false) }
    expandedStates.targetState = expanded
    if (expandedStates.currentState || expandedStates.targetState) {
        val transformOriginState = remember { mutableStateOf(TransformOrigin.Center) }
        val density = LocalDensity.current
        val popupPositionProvider = DropdownMenuPositionProvider(
            offset,
            density
        ) { parentBounds, menuBounds ->
            transformOriginState.value = calculateTransformOrigin(parentBounds, menuBounds)
        }

        Popup(
            onDismissRequest = onDismissRequest,
            popupPositionProvider = popupPositionProvider,
            properties = properties
        ) {
            Content(
                expandedStates = expandedStates,
                transformOriginState = transformOriginState,
                elevation = elevation,
                shape = shape,
                content = content,
                contentColor = contentColor,
                backgroundColor = backgroundColor,
                border = border,
                modifier = modifier
            )
        }
    }
}


/**
 * @see DropDownMenu
 */
@Composable
@NonRestartableComposable
fun DropDownMenu2(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    elevation: Dp = Dp.Unspecified,
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = MaterialTheme.colors.onSurface,
    shape: Shape = MaterialTheme.shapes.small,
    border: BorderStroke? = null,
    properties: PopupProperties = PopupProperties(focusable = true),
    content: @Composable ColumnScope.() -> Unit
) {
    DropDownMenu(
        expanded,
        onDismissRequest,
        modifier,
        offset,
        elevation,
        backgroundColor,
        contentColor,
        shape,
        border,
        properties
    ) {
        Column(
            modifier = modifier
                .padding(vertical = DropdownMenuVerticalPadding)
                .width(IntrinsicSize.Max)
                .verticalScroll(scrollState),
            content = content
        )
    }
}