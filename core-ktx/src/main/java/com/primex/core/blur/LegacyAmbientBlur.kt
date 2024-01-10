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

package com.primex.core.blur

import android.annotation.TargetApi
import android.os.Build
import androidx.annotation.FloatRange
import androidx.compose.ui.Modifier

/**
 * Applies a ambient blur modifier to the composable.
 *
 * This extension function creates a ambient blur effect by combining a RenderScript blur element
 * with the existing modifier chain. The blur effect is controlled by the specified [radius] and [downsample].
 *
 * @param radius The radius of the blur effect in pixels, capped at 25f.
 * @param downsample The downscale factor used to downscale the captured bitmap.
 *
 * @return A modified version of the original [Modifier] with the added background blur effect.
 */
@TargetApi(Build.VERSION_CODES.S_V2)
internal fun Modifier.legacyAmbientBlur(
    @FloatRange(0.0, 25.0) radius: Float = 25f,
    @FloatRange(0.0, 1.0, fromInclusive = false) downsample: Float = 1.0f
) = this then TODO()