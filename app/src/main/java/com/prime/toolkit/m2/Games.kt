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

import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.AppSettingsAlt
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Cyclone
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.prime.toolkit.core.games
import com.primex.core.ExperimentalToolkitApi
import com.primex.core.blend
import com.primex.core.plus
import com.primex.material2.IconButton
import com.primex.material2.Label
import com.primex.material2.appbar.LargeTopAppBar
import com.primex.material2.appbar.TopAppBar
import com.primex.material2.appbar.TopAppBarDefaults
import com.primex.material2.appbar.TopAppBarScrollBehavior

@OptIn(ExperimentalToolkitApi::class)
@Composable
fun AppBarsPreview(
    onBack: () -> Unit,
    padding: PaddingValues
) {
    var behavior by remember {
        mutableIntStateOf(0)
    }
    val topAppBarScrollBehavior = when (behavior) {
        0 -> TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        1 -> TopAppBarDefaults.enterAlwaysScrollBehavior()
        2 -> TopAppBarDefaults.pinnedScrollBehavior()
        else -> error("N/A Behaviour $behavior")
    }

    var showLargeBar by remember { mutableStateOf(true) }
    val context = LocalContext.current
    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                isLarge = showLargeBar,
                label = "Title",
                onBack = onBack,
                actions = {
                    IconButton(
                        imageVector = Icons.Outlined.SwapHoriz,
                        onClick = {
                            val msg = if (showLargeBar) "Normal AppBar" else "LargeApp Bar"
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            showLargeBar = !showLargeBar
                        },
                    )

                    IconButton(
                        imageVector = Icons.Outlined.AccountTree,
                        onClick = {
                            val msg =
                                "Switched to " + if (behavior == 0) "Exit until collapsed behaviour" else if (behavior == 1) "Enter Always behaviour" else "Pinned Behaviour"
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            behavior = when (behavior) {
                                0 -> 1
                                1 -> 2
                                else -> 0
                            }
                        },
                    )
                },
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        /*backgroundColor = MaterialTheme.colors.primary.blend(
            MaterialTheme.colors.background, 0.96f
        )*/
    ) { inner ->
        LazyGameGrid(
            list = games,
            contentPadding = inner + padding + PaddingValues(horizontal = 22.dp, vertical = 16.dp),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@OptIn(ExperimentalToolkitApi::class)
@Composable
private fun TopAppBar(
    isLarge: Boolean,
    label: CharSequence,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    blur: Boolean = false,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    Crossfade(targetState = isLarge, label = "", modifier = modifier) {
        when (it) {
            false -> TopAppBar(
                title = { Label(text = label) },
                scrollBehavior = scrollBehavior,
                actions = actions,
                navigationIcon = {
                    IconButton(imageVector = Icons.Outlined.ArrowBack, onClick = onBack)
                },
                style = TopAppBarDefaults.topAppBarStyle(
                    scrolledContainerColor = MaterialTheme.colors.primary.blend(
                        MaterialTheme.colors.background,
                        0.94f
                    ),
                    scrolledContentColor = MaterialTheme.colors.onBackground
                )
            )

            else -> LargeTopAppBar(
                title = { Label(text = label) },
                scrollBehavior = scrollBehavior,
                actions = actions,
                navigationIcon = {
                    IconButton(imageVector = Icons.Outlined.ArrowBack, onClick = onBack)
                },
                style = TopAppBarDefaults.largeAppBarStyle(
                    /*scrolledContainerColor = MaterialTheme.colors.primary.blend(
                        MaterialTheme.colors.background,
                        0.84f
                    ),
                    scrolledContentColor = MaterialTheme.colors.onBackground*/
                )
            )
        }
    }
}