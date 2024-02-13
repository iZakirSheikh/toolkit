/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 13-02-2024.
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

package com.prime.toolkit.m2

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.prime.toolkit.core.Game
import com.primex.material2.Label

@Composable
private fun Game(
    value: Game,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val (name, url) = value
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(8),
            elevation = 6.dp,
            modifier = modifier
                .scale(0.90f)
                .padding(2.dp)
                .weight(1f)
                .aspectRatio(1 / 1.5f),
            content = {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                         .allowHardware(false)
                        .data(url)
                        .build(),
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                )
            }
        )
        Label(
            text = name,
            style = MaterialTheme.typography.caption,
            maxLines = 2,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyGameGrid(
    list: List<Game>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(84.dp),
        horizontalArrangement = Arrangement.Absolute.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding
    ) {
        items(list, key = { it }) {
            Game(
                value = it,
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItemPlacement(),
            )
        }
    }
}

