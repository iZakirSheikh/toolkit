/*
 * Copyright 2025 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 31-01-2025.
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

package com.zs.compose.theme.internal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp

internal val ZeroPadding = PaddingValues(0.dp)

@PublishedApi
internal val WindowInsets.Companion.ZeroPadding get() = com.zs.compose.theme.internal.ZeroPadding

/**
 * A simple slot for holding content within the TwoPane layout.
 *
 * @param content The composable content to be displayed within the slot.
 */
@Composable
internal inline fun Slot(content: @Composable () -> Unit) = Box(content = { content() })

internal operator fun Constraints.component1(): Int = minWidth
internal operator fun Constraints.component2(): Int = maxWidth
internal operator fun Constraints.component3(): Int = minHeight
internal operator fun Constraints.component4(): Int = maxHeight

