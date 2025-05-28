/*
 * Copyright 2025 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 25-05-2025.
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

@file:OptIn(
    ExperimentalThemeApi::class, ExperimentalFoundationApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)

package com.prime.toolkit.m3

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.rounded.HomeMax
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.prime.toolkit.core.AdaptiveLargeTopAppBar
import com.prime.toolkit.core.background
import com.prime.toolkit.core.observe
import com.prime.toolkit.core.rememberBackgroundProvider
import com.zs.compose.foundation.fullLineSpan
import com.zs.compose.foundation.plus
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.ExperimentalThemeApi
import com.zs.compose.theme.Icon
import com.zs.compose.theme.IconButton
import com.zs.compose.theme.LocalWindowSize
import com.zs.compose.theme.adaptive.Scaffold
import com.zs.compose.theme.adaptive.content
import com.zs.compose.theme.appbar.AppBarDefaults
import com.zs.compose.theme.menu.DropDownMenuItem
import com.zs.compose.theme.text.Header
import com.zs.compose.theme.text.Label

@Composable
fun M3() {
    val behaviour = AppBarDefaults.exitUntilCollapsedScrollBehavior()
    val navInsets = WindowInsets.content
    val surface = rememberBackgroundProvider()
    val (width, height) = LocalWindowSize.current

    val colors = if (AppTheme.colors.isLight) lightColorScheme(
        primary = AppTheme.colors.accent,
        background = AppTheme.colors.background
    ) else
        darkColorScheme(primary = AppTheme.colors.accent, background = AppTheme.colors.background)
    MaterialExpressiveTheme(motionScheme = MotionScheme.expressive(), colorScheme = colors) {
        Scaffold(
            topBar = {
                AdaptiveLargeTopAppBar(
                    height > width,
                    behavior = behaviour,
                    background = AppTheme.colors.background(surface),
                    title = { Label("M3 Catalog") },
                    navigationIcon = {
                        IconButton(
                            Icons.Outlined.Info,
                            onClick = {  },
                            contentDescription = null
                        )
                    },
                    actions = {
                        IconButton(
                            Icons.Outlined.LocalFireDepartment,
                            contentDescription = null,
                            onClick = {}
                        )

                        var showMore by remember { mutableStateOf(false) }
                        IconButton(
                            content = {
                                Icon(Icons.Outlined.MoreVert, contentDescription = null)
                                DropdownMenu (showMore, onDismissRequest = { showMore = false }) {
                                    DropDownMenuItem(
                                        "Dropdown Item 1",
                                        icon = Icons.Rounded.HomeMax,
                                        onClick = {}
                                    )
                                    DropDownMenuItem(
                                        "Dropdown Item 2",
                                        icon = Icons.Rounded.Tune,
                                        onClick = {}
                                    )
                                    DropDownMenuItem(
                                        "Dropdown Item 3",
                                        icon = Icons.Rounded.Settings,
                                        onClick = {}
                                    )
                                    DropDownMenuItem(
                                        "Dropdown Item 4",
                                        icon = null,
                                        onClick = {}
                                    )
                                }
                            },
                            onClick = {
                                showMore = true
                            }
                        )
                    }
                )
            },
            content = {
                val insets = navInsets.union(WindowInsets.content).union(
                    WindowInsets.systemBars.only(
                        WindowInsetsSides.Vertical
                    )
                )
                val state = rememberLazyGridState()
                LazyVerticalGrid(
                    state = state,
                    columns = GridCells.Adaptive(84.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp) + insets.asPaddingValues(),
                    horizontalArrangement = Arrangement.Absolute.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(behaviour.nestedScrollConnection)
                        .observe(surface)/*.fadingEdge(state,  false, length = 50.dp)*/,
                    content = {
                        // button with shapes
                        item(span = fullLineSpan) {
                            Header("Button with shapes.")
                        }

                        item(span = fullLineSpan) {
                            androidx.compose.material3.Button(
                                shapes = ButtonShapes(
                                    androidx.compose.material3.ButtonDefaults.shape,
                                    androidx.compose.material3.ButtonDefaults.pressedShape
                                ),
                                onClick = {}
                            ) {
                                Text("Shapes Button")
                            }
                        }

                        // button with shapes
                        item(span = fullLineSpan) {
                            Header("Split button")
                        }

                        item(span = fullLineSpan) {
                            SplitButtonLayout(
                                leadingButton = {
                                    SplitButtonDefaults.LeadingButton(
                                        onClick = {},
                                        content = {
                                            Text("Leading Button")
                                        }
                                    )
                                },
                                trailingButton = {
                                    val (checked, onCheckedChange) = remember { mutableStateOf(false) }
                                    SplitButtonDefaults.TrailingButton (
                                        checked = checked,
                                        onCheckedChange = onCheckedChange,
                                        content = {
                                            androidx.compose.material3.Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null)
                                        }
                                    )
                                }
                            )
                        }
                    }
                )
            }
        )
    }
}