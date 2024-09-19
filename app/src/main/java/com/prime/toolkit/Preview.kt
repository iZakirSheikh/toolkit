/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by 2024 on 19-09-2024.
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

@file:OptIn(ExperimentalToolkitApi::class)

package com.prime.toolkit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.TopAppBar
import androidx.compose.material.darkColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.prime.toolkit.core.games
import com.primex.core.ExperimentalToolkitApi
import com.primex.core.MetroGreen
import com.primex.core.SignalWhite
import com.primex.core.plus
import com.primex.core.shapes.SquircleShape
import com.primex.material2.IconButton
import com.primex.material2.Label
import com.primex.material2.ListTile


@Composable
fun ListTilePreview(modifier: Modifier = Modifier) {
        ListTile(
            headline = {
                Label("Headline")
            },
            overline = {
                Label("Overline")
            },
            subtitle = {
                Label("This is the supporting text line")
            },
            spacing = 0.dp,
            shape = RoundedCornerShape(20),
            color = Color.MetroGreen,
            onColor = Color.SignalWhite,
            modifier = Modifier.clickable(/*indication = ripple(color = Color.White, bounded = true), interactionSource = null*/) {

            },
            leading = {
                Icon(Icons.Outlined.Language, contentDescription = null)
            },
            trailing = {
                IconButton(Icons.Outlined.MoreVert, onClick = {})
            }
        )
}

@Preview(showBackground = true, widthDp = 360, backgroundColor = 0x00000000)
@Composable
fun PreviewM2() {
    MaterialTheme(darkColors()) {
        Scaffold(
          topBar = {
              TopAppBar(title = { Label("") })
          }
        ) {
            Surface(modifier = Modifier.padding(it + PaddingValues(60.dp)), shape =  SquircleShape(0.6f)) {
                AsyncImage(
                    model = games[0].second,
                    contentDescription = null,
                    modifier = Modifier
                        //.padding(30.dp)
                        .widthIn(max = 200.dp).aspectRatio(1.0f),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
