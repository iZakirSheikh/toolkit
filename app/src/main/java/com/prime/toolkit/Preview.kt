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

@file:OptIn(ExperimentalThemeApi::class)

package com.prime.toolkit

import android.util.Log
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradient
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.zs.compose.foundation.Slot
import com.zs.compose.foundation.UmbraGrey
import com.zs.compose.foundation.decorator.EdgeInsets
import com.zs.compose.foundation.decorator.decorator
import com.zs.compose.foundation.shapes.CompactDisk
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.ExperimentalThemeApi
import com.zs.compose.theme.adaptive.content
import com.zs.compose.theme.text.Label

private const val TAG = "Preview"

@OptIn(ExperimentalFoundationApi::class)
@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun Preview() {
    /*com.zs.compose.theme.Surface(
        modifier = Modifier.size(100.dp),
        shape = CompactDisk,
        color = Color.Green,
        contentColor = Color.White,
        border = BorderStroke(5.dp, Color.Yellow.copy(0.5f)),
    ) {
        com.zs.compose.theme.text.Label("Content", color = Color.Blue)
    }*/
    val context = LocalContext.current
    Column() {
//        Slot (
//            modifier = Modifier.decorator(
//                backgroundColor = AppTheme.colors.background,
//                elevation = 10.dp,
//                shape = AppTheme.shapes.medium,
//
//            )
//        ) {
//            Label("None")
//        }

        val animator  = rememberInfiniteTransition()

        val gap by animator.animateFloat(0f, 10f,
            animationSpec =
                infiniteRepeatable(
                    // Infinitely repeating a 1000ms tween animation using default easing curve.
                    animation = tween(1000),
                    // After each iteration of the animation (i.e. every 1000ms), the animation
                    // will
                    // start again from the [initialValue] defined above.
                    // This is the default [RepeatMode]. See [RepeatMode.Reverse] below for an
                    // alternative.
                    repeatMode = RepeatMode.Reverse,
                ),)

        Log.d(TAG, "Preview: $gap")
        Slot(
            modifier = Modifier
                .decorator(
                    backgroundColor = Color.Green,
                    elevation = 10.dp,
                    outlineColor = Color.UmbraGrey,
                    outlineWidth = 2.dp,
                    outlineGap = gap.dp,
                    shape = CompactDisk,
                    edgeInsets = EdgeInsets(10.dp),
                    roughness = 0.0f,
                )
                .size(100.dp)
        ) {
           // Label("Yses", modifier = Modifier)
            Brush.linearGradient()
        }
    }
}


@Composable
fun PreviewNode() {
    com.zs.compose.theme.adaptive.Scaffold(topBar = {
        com.zs.compose.theme.appbar.TopAppBar(title = { com.zs.compose.theme.text.Label("Preview") })
    }) {
        val insets = WindowInsets.content
        Column(/*modifier = Modifier.padding(16.dp).windowInsetsPadding(insets)*/) {
            Preview()
        }
    }
}