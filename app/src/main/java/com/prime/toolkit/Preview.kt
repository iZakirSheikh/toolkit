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
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.zs.compose.foundation.backdrop.backdrop
import com.zs.compose.foundation.backdrop.haze.BlurProfile
import com.zs.compose.foundation.backdrop.haze.hazeEffect
import com.zs.compose.foundation.backdrop.rememberBackdropLayer
import com.zs.compose.theme.ExperimentalThemeApi
import com.zs.compose.theme.adaptive.content

private const val TAG = "Preview"

@OptIn(ExperimentalFoundationApi::class)
@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun Preview() {
    val backdrop = rememberBackdropLayer()
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
//        Image(
//            painter = painterResource(R.drawable.wallpaper_light),
//            modifier = Modifier
//                .fillMaxSize()
//                .backdrop(backdrop),
//            contentScale = ContentScale.Crop,
//            contentDescription = null,
//        )

        val context = LocalContext.current
        AsyncImage(
            model =  ImageRequest.Builder(context)
                .data("https://cdn.thegamesdb.net/images/original/boxart/front/53-1.jpg")
                .build(),
            onError = { Log.d(TAG, "Game: ${it.result.throwable.message}")},
            contentScale = ContentScale.Crop,
            contentDescription = null,
            modifier = Modifier.fillMaxSize().backdrop(backdrop)
        )

        // Current animated offset
        var offset by remember { mutableStateOf(Offset.Zero) }

        val infiniteTransition = rememberInfiniteTransition(label = "infinite_transition")

        val animatedValue by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 10_000,
                    easing = LinearEasing // Change to FastOutSlowInEasing for a smoother effect
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "zero_to_one_animation"
        )

        Spacer(
            Modifier
                .requiredSize(100.dp)
                .graphicsLayer {
                    translationX = offset.x
                    translationY = offset.y
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offset += dragAmount
                    }
                }
                .hazeEffect(
                    backdrop,
                    Color.Transparent,
                    blurProfile = BlurProfile(0.25f, 100f * animatedValue),
                    vibrancy = 1.6f,
                    edgeHighlight = BorderStroke(
                        2.dp,
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.6f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.1f)
                            ),
                        )
                    ),
                    shape = RoundedCornerShape(12.dp),
                    noiseAmount = 0.25f
                )
        )
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