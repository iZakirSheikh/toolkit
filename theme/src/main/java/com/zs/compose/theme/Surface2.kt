/*
 * Copyright 2025 sheik
 *
 * Created by sheik on 03-05-2025.
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

@file:OptIn(ExperimentalFoundationApi::class)

package com.zs.compose.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.isContainer
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zs.compose.foundation.Background
import com.zs.compose.foundation.background


// TODO - Keep an eye on this from material2 or material3
@ExperimentalThemeApi
private fun Modifier.surface(
    shape: Shape, background: Background, border: BorderStroke?, elevation: Dp
) = this
    .shadow(elevation, shape, clip = false)
    .then(if (border != null) Modifier.border(border, shape) else Modifier)
    .clip(shape)
    .background(background)


/**
 * An alternative to [Surface].
 *
 * A surface is a foundational component that renders a background behind its content. It provides a
 * customizable way to set the background, shape, elevation, border, and content color of a UI element.
 *
 * @param modifier Modifier to be applied to the surface.
 * @param shape Defines the surface's shape as well as its shadow. A shadow is only
 *  displayed if the [elevation] is greater than zero.
 * @param background The background of the surface.
 * @param contentColor The preferred content color provided by this Surface to its
 * children. Defaults to the matching content color for [background], or if that is not
 * a color from the theme, this will keep the same content color set above the
 * Surface.
 * @param border Optional border to draw on top of the surface.
 * @param elevation The z-coordinate at which to place this surface. This controls
 * the size of the shadow below the surface.
 * @param content The content inside the surface.
 */
@ExperimentalThemeApi
@Composable
fun Surface(
    background: Background,
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    contentColor: Color = AppTheme.colors.onBackground,
    border: BorderStroke? = null,
    elevation: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalContentColor provides contentColor
    ) {
        Box(
            modifier = modifier
                .surface(
                    shape = shape, background = background, border = border, elevation = elevation
                )
                .semantics(mergeDescendants = false) {
                    @Suppress("DEPRECATION") isContainer = true
                }
                .pointerInput(Unit) {}, propagateMinConstraints = true
        ) {
            content()
        }
    }
}