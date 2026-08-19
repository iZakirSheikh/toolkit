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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.zs.compose.foundation.decorator
import com.zs.compose.foundation.shapes.CompactDisk
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.ExperimentalThemeApi
import com.zs.compose.theme.adaptive.content

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
    AsyncImage(
        model =  ImageRequest.Builder(context)
            .data("https://static.vecteezy.com/system/resources/thumbnails/010/700/453/small/grunge-dark-gray-color-texture-photo.jpg")
            .build(),
       // onError = { Log.d(TAG, "Game: ${it.result.throwable.message}")},
        contentScale = ContentScale.Crop,
        contentDescription = null,
        modifier = Modifier.size(150.dp)
            .decorator(
                backgroundColor = AppTheme.colors.background,
                shape = CompactDisk,
                roughness = 0.3f,
                border = BorderStroke(1.dp, Color.White),
                elevation = 20.dp
            )
    )
}


@Composable
fun PreviewNode() {
    com.zs.compose.theme.adaptive.Scaffold(topBar = {
        com.zs.compose.theme.appbar.TopAppBar(title = { com.zs.compose.theme.text.Label("Preview") }, )
    }) {
        val insets = WindowInsets.content
        Column(modifier = Modifier.padding(16.dp).windowInsetsPadding(insets)) {
            Preview()
        }
    }
}