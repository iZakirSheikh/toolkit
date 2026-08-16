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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material3.Label
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.zs.compose.foundation.Orange
import com.zs.compose.foundation.SignalWhite
import com.zs.compose.foundation.background
import com.zs.compose.foundation.decorator
import com.zs.compose.foundation.shapes.CompactDisk
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.ExperimentalThemeApi
import com.zs.compose.theme.adaptive.content
import com.zs.compose.theme.minimumInteractiveComponentSize
import com.zs.compose.theme.snackbar.Snackbar
import com.zs.compose.theme.snackbar.SnackbarData
import com.zs.compose.theme.snackbar.SnackbarDuration
import com.zs.compose.theme.snackbar.SnackbarHostState

@OptIn(ExperimentalFoundationApi::class)
@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun Preview() {
    Row() {
        Column() {
            Box(
                Modifier
                    //.padding(2.dp)
                    .background(
                        shape = CompactDisk,
                        color = Color.Green
                    )
                    .border(
                        border = BorderStroke(5.dp, Color.Yellow.copy(0.5f)),
                        CompactDisk
                    )
                    .graphicsLayer(){
                        shape = CompactDisk
                        shadowElevation= 5.dp.toPx()
                        clip = true
                    }
                    .defaultMinSize(100.dp, minHeight = 100.dp)
            ){
                com.zs.compose.theme.text.Label("Content", color = Color.Blue)
            }

            Spacer(Modifier.padding(4.dp))
            com.zs.compose.theme.text.Label("Modifiers")
        }
        Spacer(Modifier.padding(10.dp))
        Column() {
            Box (
                Modifier
                    //.padding(2.dp)
                    .decorator(
                        shape = CompactDisk,
                        // border = BorderStroke(5.dp, Color.Yellow),
                        backgroundColor = Color.Green,
                       // foregroundColor = Color.White.copy(0.1f),
                        borderColor = Color.Yellow.copy(0.5f),
                        borderWidth = 5.dp,
                        elevation = 5.dp
                    )

                    .defaultMinSize(100.dp, minHeight = 100.dp)
            ){
                com.zs.compose.theme.text.Label("Content", color = Color.Blue)
            }
            Spacer(Modifier.padding(4.dp))

            com.zs.compose.theme.text.Label("Decorator")

        }
    }
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