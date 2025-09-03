/*
 * Copyright 2025 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 15-05-2025.
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

@file:OptIn(ExperimentalThemeApi::class, ExperimentalFoundationApi::class)

package com.prime.toolkit.text

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.prime.toolkit.R
import com.prime.toolkit.core.AdaptiveLargeTopAppBar
import com.prime.toolkit.core.background
import com.prime.toolkit.core.observe
import com.prime.toolkit.core.rememberBackgroundProvider
import com.zs.compose.foundation.Background
import com.zs.compose.foundation.fadingEdge
import com.zs.compose.foundation.textResource
import com.zs.compose.theme.AlertDialog
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.Button
import com.zs.compose.theme.ExperimentalThemeApi
import com.zs.compose.theme.IconButton
import com.zs.compose.theme.LocalWindowSize
import com.zs.compose.theme.Surface
import com.zs.compose.theme.adaptive.Scaffold
import com.zs.compose.theme.adaptive.content
import com.zs.compose.theme.appbar.AppBarDefaults
import com.zs.compose.theme.appbar.TopAppBar
import com.zs.compose.theme.text.Header
import com.zs.compose.theme.text.Label
import com.zs.compose.theme.text.Text

@NonRestartableComposable
@Composable
private fun SectionHeader(text: CharSequence, modifier: Modifier = Modifier) {
    Header(
        text,
        style = AppTheme.typography.body3,
        modifier = Modifier.padding(vertical = 12.dp),
        color = AppTheme.colors.accent
    )
}

@Composable
@NonRestartableComposable
private fun Section(
    text: CharSequence,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(20),
        color = AppTheme.colors.background(1.dp)
    ) {
        Text(
            text,
            modifier = modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun Strings() {
    val behaviour = AppBarDefaults.exitUntilCollapsedScrollBehavior()
    val navInsets = WindowInsets.content
    val surface = rememberBackgroundProvider()
    val (width, height) = LocalWindowSize.current


    Scaffold(
        topBar = {
            AdaptiveLargeTopAppBar(
                height > width,
                behavior = behaviour,
                background = AppTheme.colors.background(surface),
                title = {
                    Text(
                        text = textResource(R.string.strings_title),
                        maxLines = 2
                    )
                },
                navigationIcon = {
                    var showDialog by remember { mutableStateOf(false) }
                    AlertDialog(
                        showDialog,
                        onDismissRequest = { showDialog = false },
                        title = { Label("String Tip") },
                        navigationIcon = {
                            IconButton(
                                Icons.Outlined.Info,
                                contentDescription = null,
                                onClick = {}
                            )
                        },
                        content = {
                            Text(textResource(R.string.styled_string))
                        }
                    )
                    IconButton(
                        Icons.Outlined.Info,
                        onClick = { showDialog = !showDialog },
                        contentDescription = null
                    )
                },
                actions = {

                    IconButton(
                        Icons.Outlined.Colorize,
                        contentDescription = null,
                        onClick = {}
                    )

                    IconButton(
                        Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        onClick = {}
                    )

                    IconButton(
                        Icons.Outlined.MoreVert,
                        contentDescription = null,
                        onClick = {}
                    )
                }
            )
        },
        content = {
            val state = rememberLazyListState()
            LazyColumn(
                state = state,
                modifier = Modifier
                    .fadingEdge(state, false, length = 50.dp)
                    .nestedScroll(behaviour.nestedScrollConnection)
                    .observe(surface)
                    .padding(horizontal = 16.dp),
                contentPadding = navInsets.union(WindowInsets.content).union(
                    WindowInsets.systemBars.only(WindowInsetsSides.Vertical)
                ).asPaddingValues(),
                content = {
                    // Sample Text
                    item { SectionHeader("Sample Text") }
                    item { Section(textResource(R.string.sample_annotated_string)) }

                    // Preference
                    item { SectionHeader("Preference") }
                    item { Section(textResource(R.string.sample_preference_text)) }

                    // Snackbar Message
                    item { SectionHeader("Snackbar Message") }
                    item { Section(textResource(R.string.snackbar_msg)) }

                    //Styled String with Args
                    item { SectionHeader("Styled String & Args") }
                    item { Section(textResource(R.string.styled_string, 1)) }

                    // Text and Link
                    item { SectionHeader("Link & Text") }
                    item { Section(textResource(R.string.text_with_url)) }

                    // From Android
                    item { SectionHeader("From Android") }
                    item { Section(textResource(R.string.styled_from_android_source)) }

                    // List
                    item { SectionHeader("List") }
                    item { Section(textResource(R.string.styled_string_list)) }
                }
            )
        }
    )
}