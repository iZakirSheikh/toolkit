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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.zs.compose.foundation.Slot
import com.zs.compose.foundation.decorator.EdgeInsets
import com.zs.compose.foundation.decorator.decorator
import com.zs.compose.foundation.shapes.CompactDisk
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.ExperimentalThemeApi
import com.zs.compose.theme.adaptive.content
import com.zs.compose.theme.text.Label

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

        Slot(
            modifier = Modifier.decorator(
                backgroundColor = AppTheme.colors.background,
                elevation = 10.dp,
                shape = CompactDisk,
                edgeInsets = EdgeInsets(10.dp),
                roughness = 0.8f,
            ).size(100.dp)
        ) {
            Label("Yses", modifier = Modifier)
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