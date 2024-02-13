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

package com.prime.toolkit.core

/**
 * A simple pair that contais the information about the video fgame
 * @param first - Name of the game.
 * @param second - Url of the artwork of the game.
 */
typealias Game = Pair<String, String>

/**
 * The sample collection of [Game]s
 */
val games: List<Game> = listOf(
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
