/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 11-01-2024.
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

@file:OptIn(ExperimentalMaterial3Api::class)

package com.prime.toolkit.preview

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.primex.core.ExperimentalToolkitApi
import com.primex.core.ImageBrush
import com.primex.core.blur.legacyBackgroundBlur
import com.primex.core.noise
import com.primex.core.rememberVectorPainter
import com.primex.core.visualEffect
import com.primex.material2.Dialog
import com.primex.material2.DropDownMenuItem
import com.primex.material2.menu.DropDownMenu2
import com.primex.material3.IconButton
import kotlinx.coroutines.delay

private const val TAG = "Blur"

private val BASE_URL = "https://picsum.photos/id"

/**
 * Constructs a image url
 */
fun Image(id: Int, width: Int = 256, height: Int = 256) =
    "$BASE_URL/$id/$width/$height"

@Composable
@NonRestartableComposable
fun Item(url: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Surface(shape = RoundedCornerShape(5), modifier = modifier) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .allowHardware(false)
                .data(url)
                .build(),
            contentScale = ContentScale.Crop,
            contentDescription = null,
            placeholder = painterResource(id = android.R.drawable.checkbox_on_background)

        )
    }
}

val lsit = List(30) { id ->
    Image(id, 256, 256)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Grid(modifier: Modifier = Modifier) {
    var data by remember {
        mutableStateOf<List<String>>(emptyList())
    }
    LaunchedEffect(key1 = Unit) {
        while (true) {
            delay(5_000)
            data = lsit.shuffled()
        }
    }
    val context = LocalContext.current
    LazyVerticalGrid(
        columns = GridCells.Adaptive(110.dp),
        horizontalArrangement = Arrangement.Absolute.spacedBy(4.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(data, key = { it }) {
            Item(
                url = it,
                modifier = Modifier
                    .animateItemPlacement()
                    .height(110.dp)
                    .aspectRatio(0.56f)
            )
        }
    }
}

@OptIn(ExperimentalToolkitApi::class, ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
@Preview
fun BlurPreview() {
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .legacyBackgroundBlur(
                        25f,
                        0.4f
                    )

                    .background(Color.White.copy(0.7f))
                    .noise(0.05f)
                    .fillMaxWidth()
                    .height(56.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                com.primex.material3.Label("Blur Preview")

            }
        },
        bottomBar = {

            var show by remember {
                mutableStateOf(false)
            }

            Dialog(
                expanded = show,
                onDismissRequest = { show = false },
                backgroundColor = Color.Transparent,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .legacyBackgroundBlur(25f, 0.4f)
                    .background(Color.White.copy(0.7f), )
                    .visualEffect(ImageBrush.NoiseBrush, 0.5f)
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier,
                ) {

                    TopAppBar(title = { com.primex.material3.Label(text = "Dialog") })

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        Slider(value = 0.5f, onValueChange = {})
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            BottomAppBar(actions = {
                IconButton(
                    icon = Icons.Rounded.AddCircle,
                    contentDescription = null,
                    onClick = { show = true }
                )
                var expanded by remember {
                    mutableStateOf(false)
                }
                androidx.compose.material.IconButton(onClick = { expanded = !expanded }) {
                    Icon(imageVector = Icons.Outlined.MoreVert, contentDescription = null)
                    DropDownMenu2(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        // border = BorderStroke(1.dp, Color.Gray),
                        shape = RoundedCornerShape(5)
                    ) {
                        DropDownMenuItem(
                            title = "Hello",
                            onClick = { /*TODO*/ },
                            subtitle = "HI this is Zakir",
                            icon = rememberVectorPainter(image = Icons.Outlined.Person)
                        )

                        DropDownMenuItem(
                            title = "Hello",
                            onClick = { /*TODO*/ },
                            subtitle = "HI this is Zakir",
                            icon = rememberVectorPainter(image = Icons.Outlined.Person)
                        )

                        DropDownMenuItem(
                            title = "Hello",
                            onClick = { /*TODO*/ },
                            subtitle = "HI this is Zakir",
                            icon = rememberVectorPainter(image = Icons.Outlined.Person)
                        )

                        DropDownMenuItem(
                            title = "Hello",
                            onClick = { /*TODO*/ },
                            subtitle = "HI this is Zakir",
                            icon = rememberVectorPainter(image = Icons.Outlined.Person)
                        )
                    }
                }
            }
            )
        }
    ) {
        Grid(
            /*  Modifier.padding(it)*/
        )
    }
}


