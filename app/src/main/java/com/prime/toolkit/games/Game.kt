/*
 * Copyright 2025 sheik
 *
 * Created by sheik on 08-05-2025.
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

package com.prime.toolkit.games

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.allowHardware
import com.zs.compose.foundation.ImageBrush
import com.zs.compose.foundation.visualEffect
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.Surface
import com.zs.compose.theme.text.Label

private const val TAG = "Game"

@Composable
fun Game(
    value: Game,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val (name, url) = value
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ){
        Surface(
            shape = RoundedCornerShape(8),
            elevation = 0.dp,
            color = AppTheme.colors.background(1.dp),
            modifier = Modifier
                .scale(0.90f)
                .padding(2.dp)
                .aspectRatio(1 / 1.6f),
            content = {
                AsyncImage(
                    model =  ImageRequest.Builder(context)
                        .data(url)
                        .build(),
                    onError = { Log.d(TAG, "Game: ${it.result.throwable.message}")},
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                    modifier = Modifier
                )
            }
        )

        Label(
            text = name,
            style = AppTheme.typography.label3,
            maxLines = 2,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, end = 6.dp, top = 8.dp)
        )
    }
}