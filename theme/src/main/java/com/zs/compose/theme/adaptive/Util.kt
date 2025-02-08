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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import com.zs.compose.theme.internal.ZeroPadding

/**
 * The content insets for the screen under current [NavigationSuitScaffold]
 */
internal val LocalContentInsets =
    compositionLocalOf { WindowInsets.ZeroPadding }

/**
 * Provides the insets for the current content within the [Scaffold].
 */
val WindowInsets.Companion.contentInsets
    @ReadOnlyComposable @Composable get() = LocalContentInsets.current