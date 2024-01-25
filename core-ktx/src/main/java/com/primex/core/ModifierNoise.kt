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

package com.primex.core

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode

/**
 * A Style Modifier that applies a noise effect to the content using a Shader.
 *
 * This modifier draws a tiled noise pattern on top of the content, creating a visually textured overlay.
 * It can be combined with other modifiers to achieve various effects, like:
 *
 * - Simulating iOS-style blur effects by combining it with blur and background modifiers.
 * - Adding subtle texture to backgrounds or images for a more organic look.
 * - Creating unique visual styles and emphasizing depth in UI elements.
 *
 * @param alpha: The opacity of the noise effect, ranging from 0 (transparent) to 1 (opaque).
 * @constructor Creates a NoiseModifier with the specified alpha value.
 */
@ExperimentalToolkitApi
@Deprecated("Use visualEffect", replaceWith = ReplaceWith("visualEffect"))
fun Modifier.noise(alpha: Float) =
    this then visualEffect(ImageBrush.NoiseBrush, alpha, true, BlendMode.Hardlight)
