// Games

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

@file:OptIn(ExperimentalThemeApi::class, ExperimentalFoundationApi::class)

package com.prime.toolkit.games

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.rounded.HomeMax
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.prime.toolkit.R
import com.prime.toolkit.core.AdaptiveLargeTopAppBar
import com.zs.compose.foundation.Background
import com.zs.compose.foundation.backdrop.backdrop
import com.zs.compose.foundation.backdrop.haze.BlurConfig
import com.zs.compose.foundation.backdrop.haze.legacyHazeEffect
import com.zs.compose.foundation.backdrop.rememberBackdropLayer
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.ExperimentalThemeApi
import com.zs.compose.theme.Icon
import com.zs.compose.theme.IconButton
import com.zs.compose.theme.LocalWindowSize
import com.zs.compose.theme.adaptive.Scaffold
import com.zs.compose.theme.adaptive.content
import com.zs.compose.theme.appbar.AppBarDefaults
import com.zs.compose.theme.menu.DropDownMenu
import com.zs.compose.theme.menu.DropDownMenuItem
import com.zs.compose.theme.text.Label

/**
 * A simple pair that contais the information about the video fgame
 * @param first - Name of the game.
 * @param second - Url of the artwork of the game.
 */
typealias Game = Pair<String, String>

/**
 * The sample collection of [Game]s
 */
private val Games: List<Game> = listOf(
    "Prince of Persia 2008" to "https://cdn.thegamesdb.net/images/original/boxart/front/53-1.jpg",
    "Halo 3 (Classic)" to "https://cdn.thegamesdb.net/images/thumb/boxart/front/97251-1.jpg",
    "Assassin's Creed II" to "https://cdn.thegamesdb.net/images/thumb/boxart/front/9302-1.jpg",
    "Assassin's Creed" to "https://cdn.thegamesdb.net/images/thumb/boxart/front/7139-1.jpg",
    "Prince of Persia: The Two Thrones" to "https://cdn.thegamesdb.net/images/thumb/boxart/front/114326-1.jpg",
    "Crysis" to "https://cdn.thegamesdb.net/images/thumb/boxart/front/15246-1.jpg",
    "Crysis 3" to "https://cdn.thegamesdb.net/images/thumb/boxart/front/15073-1.jpg",
    "Crysis 3 Remastered" to "https://cdn.thegamesdb.net/images/thumb/boxart/front/94878-1.jpg",
    "Prince of Persia: The Sands of Time" to "https://cdn.thegamesdb.net/images/thumb/boxart/front/122145-1.jpg",
    "Halo Infinite" to "https://cdn.thegamesdb.net/images/thumb/boxart/front/100267-1.jpg",
    "The Witcher 3: Wild Hunt" to "https://cdn.thegamesdb.net/images/thumb/boxart/front/15072-1.jpg",
    "Horizon Zero Dawn" to "https://cdn.thegamesdb.net/images/thumb/boxart/front/15071-1.jpg",
    "Darksiders" to "https://cdn.thegamesdb.net/images/thumb/boxart/front/15070-1.jpg",
    "Rise of the Tomb Raider" to "https://cdn.thegamesdb.net/images/thumb/boxart/front/15069-1.jpg",
    "Darksiders II" to "https://cdn.thegamesdb.net/images/thumb/boxart/front/15068-1.jpg",
    "Blasphemous" to "https://cdn.thegamesdb.net/images/thumb/boxart/front/15067-1.jpg",
    "Dead Cells" to "https://cdn.thegamesdb.net/images/thumb/boxart/front/15066-1.jpg",
    "Ori and the Blind Forest" to "https://cdn.thegamesdb.net/images/thumb/boxart/front/15065-1.jpg",
    "Hollow Knight" to "https://cdn.thegamesdb.net/images/thumb/boxart/front/15064-1.jpg",
    "Laika: Aged Through Blood" to "https://cdn.thegamesdb.net/images/thumb/boxart/front/15063-1.jpg",
    "Braid" to "https://cdn.thegamesdb.net/images/thumb/boxart/front/15062-1.jpg",
    "Indivisible" to "https://cdn.thegamesdb.net/images/thumb/boxart/front/15061-1.jpg",
    "La-Mulana" to "https://cdn.thegamesdb.net/images/thumb/boxart/front/15060-1.jpg",
    "Shantae and the Pirate's Curse" to "https://cdn.thegamesdb.net/images/thumb/boxart/front/15059-1.jpg",
    "Super Time Force" to "https://cdn.thegamesdb.net/images/thumb/boxart/front/15058-1.jpg",
    "Timespinner" to "https://cdn.thegamesdb.net/images/thumb/boxart/front/15057-1.jpg",
    "Touhou Luna Nights" to "https://cdn.thegamesdb.net/images/thumb/boxart/front/15056-1.jpg",
    "Mirror's Edge" to "https://cdn.thegamesdb.net/images/thumb/boxart/front/15055-1.jpg",
    "Sacred" to "https://cdn.thegamesdb.net/images/thumb/boxart/front/15054-1.jpg",
    "Titan Quest" to "https://cdn.thegamesdb.net/images/thumb/boxart/front/15053-1.jpg",
    "Rayman" to "https://cdn.thegamesdb.net/images/thumb/boxart/front/15052-1.jpg",
    "Assassin's Creed Chronicles" to "https://cdn.thegamesdb.net/images/thumb/boxart/front/15051-1.jpg"
)


@Composable
context(_: RowScope)
private fun ActionRow(
    background: Background,
) {
    IconButton(
        Icons.Outlined.LocalFireDepartment,
        contentDescription = null,
        onClick = {}
    )

    var showMore by remember { mutableStateOf(false) }
    IconButton(
        onClick = { showMore = true },
        content = {
            Icon(Icons.Outlined.MoreVert, contentDescription = null)
            DropDownMenu(
                expanded = showMore,
                onDismissRequest = { showMore = false },
                background = background,
                content = {
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
            )
        }
    )
}

/**
 * Represents the games screen.
 */
@Composable
fun Games() {
    val behaviour = AppBarDefaults.exitUntilCollapsedScrollBehavior()
    val navInsets = WindowInsets.content
    val backdrop = rememberBackdropLayer()
    val (width, height) = LocalWindowSize.current

    Scaffold(
        topBar = {
            AdaptiveLargeTopAppBar(
                false,
                behavior = behaviour,
                background = Background(Modifier.legacyHazeEffect(
                    backdrop,
                    AppTheme.colors.background(0.4.dp),
                    blurConfig = BlurConfig(0.25f, 25f),
                    vibrancy = 1.8f,
                    tint = AppTheme.colors.background.copy(0.90f),
                    noiseAmount = 0.1f
                )),
                title = { Label(stringResource(R.string.video_games)) },
                navigationIcon = {
                    IconButton(
                        Icons.Outlined.Info,
                        contentDescription = null,
                        onClick = {}
                    )
                },
                actions = {
                    ActionRow(Background(Modifier
                        .legacyHazeEffect(
                            backdrop,
                            AppTheme.colors.background(0.4.dp),
                            blurConfig = BlurConfig(0.35f, 25f),
                            vibrancy = 1.8f,
                            //tint = AppTheme.colors.background(0.4.dp).copy(0.65f),
                            noiseAmount = 0.1f
                        )
                    ))
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
                    .backdrop(backdrop)/*.fadingEdge(state,  false, length = 50.dp)*/,
                content = {
                    items(Games) { item ->
                        Game(
                            item,
                            modifier = Modifier.clickable() {}
                        )
                    }
                }
            )
        }
    )
}