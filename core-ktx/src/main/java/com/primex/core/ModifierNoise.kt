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

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import com.primex.core.R

private const val TAG = "ModifierNoise"

// TODO - Add various kinds of noise options like Perlin, Simplex, Fractal noise etc.

/**
 * NoiseNode is a DrawModifierNode that implements the noise effect.
 *
 * It creates a ShaderBrush from a noise drawable and draws a rectangle with that brush on top of the content.
 * The alpha and blend mode can be adjusted to control the intensity and blending of the noise effect.
 */
private class NoiseNode(var alpha: Float) : Modifier.Node(), DrawModifierNode,
    CompositionLocalConsumerModifierNode {

    /**
     * The ShaderBrush used to paint the noise effect.
     */
    lateinit var shader: ShaderBrush

    /**
     * Initializes the ShaderBrush with a tiled noise pattern when attached to a LayoutNode.
     */
    @SuppressLint("SuspiciousCompositionLocalModifierRead")
    override fun onAttach() {
        super.onAttach()
        // construct the shader that is going to be used in the drawing of the noies
        val drawable =
            currentValueOf(LocalContext).getDrawable(R.drawable.noise_wndows)!!.toBitmap().asImageBitmap()
        shader = ShaderBrush(
            ImageShader(
                drawable, TileMode.Repeated, TileMode.Repeated,
            )
        )
    }


    override fun ContentDrawScope.draw() {
        // draw the content
        drawContent()
        // after the content is drawn; draw the shader.
        drawRect(
            brush = shader, alpha = alpha, blendMode = BlendMode.Hardlight
        )
    }
}


private data class ModifierNoiseElement(val alpha: Float) : ModifierNodeElement<NoiseNode>() {
    override fun create(): NoiseNode = NoiseNode(alpha)

    override fun update(node: NoiseNode) {
        node.alpha = alpha
        Log.d(TAG, "update: $alpha")
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "NoiseModifier"
        properties["alpha"] = alpha
    }
}

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
fun Modifier.noise(alpha: Float) = this then ModifierNoiseElement(alpha)