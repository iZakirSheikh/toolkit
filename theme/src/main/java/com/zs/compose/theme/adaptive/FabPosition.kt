/*
 * Copyright 2025 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 08-02-2025.
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

package com.zs.compose.theme.adaptive

/** The possible positions for a [FloatingActionButton] attached to a [Scaffold]. */
@JvmInline
value class FabPosition internal constructor(@Suppress("unused") private val value: Int) {
    companion object {
        /**
         * Position FAB at the bottom of the screen at the start, above the [BottomAppBar] (if it
         * exists)
         */
        val Start = FabPosition(0)

        /**
         * Position FAB at the bottom of the screen in the center, above the [BottomAppBar] (if it
         * exists)
         */
        val Center = FabPosition(1)

        /**
         * Position FAB at the bottom of the screen at the end, above the [BottomAppBar] (if it
         * exists)
         */
        val End = FabPosition(2)
    }

    override fun toString(): String {
        return when (this) {
            Start -> "FabPosition.Start"
            Center -> "FabPosition.Center"
            else -> "FabPosition.End"
        }
    }
}