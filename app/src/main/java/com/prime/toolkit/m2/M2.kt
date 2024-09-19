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

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.BottomAppBar
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.darkColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdUnits
import androidx.compose.material.icons.outlined.BlurOn
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.primex.core.ExperimentalToolkitApi
import com.primex.core.ImageBrush
import com.primex.core.blend
import com.primex.core.blur.legacyBackgroundBlur
import com.primex.core.foreground
import com.primex.core.visualEffect
import com.primex.material2.Label
import com.primex.material2.appbar.TopAppBarDefaults

/**
 * The Entry point for M2 Sample.
 */
@OptIn(ExperimentalToolkitApi::class)
@Composable
fun M2(dark: Boolean, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var screen by remember { mutableIntStateOf(0) }
    MaterialTheme(
        if (dark) darkColors() else lightColors(),
        content = {
            Scaffold(
                content = { padding ->
                    Crossfade(targetState = screen, label = "", modifier = Modifier.fillMaxSize()) {
                        when (it) {
                            0 -> AppBarsPreview(onBack = onBack, padding = padding)
                        }
                    }
                },
                bottomBar = {
                    BottomAppBar(
                        backgroundColor = Color.Transparent,
                        contentColor = MaterialTheme.colors.onBackground,
                        windowInsets = WindowInsets.navigationBars,
                        elevation = 0.dp,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        modifier = Modifier
                            .background( MaterialTheme.colors.primary.blend(
                                MaterialTheme.colors.background, 0.91f
                            ).copy(0.7f))
                            //.legacyBackgroundBlur(25f, 0.3f)
                            .visualEffect(ImageBrush.NoiseBrush, 0.3f),
                        // The actions of Bottom app abr.
                        content = {
                            NavigationItem(
                                selected = screen == 0,
                                onClick = { screen = 0 },
                                label = "App Bars",
                                icon = Icons.Outlined.AdUnits
                            )

                            NavigationItem(
                                selected = screen == 1,
                                onClick = { screen = 1 },
                                label = "Blur",
                                icon = Icons.Outlined.BlurOn
                            )

                            NavigationItem(
                                selected = screen == 2,
                                onClick = { screen = 2 },
                                label = "Screen 3",
                                icon = Icons.Outlined.QrCodeScanner
                            )

                            NavigationItem(
                                selected = screen == 3,
                                onClick = { screen = 3 },
                                label = "Screen 4",
                                icon = Icons.Outlined.Scale
                            )
                        }
                    )
                },
            )
        }
    )
}

@Composable
private inline fun RowScope.NavigationItem(
    selected: Boolean,
    noinline onClick: () -> Unit,
    label: CharSequence,
    icon: ImageVector,
) {
    BottomNavigationItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(imageVector = icon, contentDescription = label.toString()) },
        label = {
            Label(text = label)
        }
    )
}