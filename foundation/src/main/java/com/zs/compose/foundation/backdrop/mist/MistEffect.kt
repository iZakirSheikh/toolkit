package com.zs.compose.foundation.backdrop.mist

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.InspectorInfo
import com.zs.compose.foundation.backdrop.Backdrop
import com.zs.compose.foundation.decorator.decorator

/**
 * A [ModifierNodeElement] that creates and updates a [MistEffectNode], applying a frosted
 * "mist" (glassmorphism-style) blur effect to the composable it modifies.
 *
 * @property backdrop The shared [Backdrop] state containing the captured background layer to be blurred.
 * @property blurRadiusPx The radius of the blur effect in pixels.
 * @property vibrancy A multiplier for the background's color saturation to boost vibrancy.
 *                    (e.g., 1.0f is neutral, >1.0f increases color saturation to prevent washed-out glass).
 */
private class MistEffectElement(
    val backdrop: Backdrop,
    val blurRadiusPx: Float,
    val vibrancy: Float,
) : ModifierNodeElement<MistEffectNode>() {

    /**
     * Creates a new [MistEffectNode] initialized with this element's current parameters.
     */
    override fun create(): MistEffectNode = MistEffectNode(
        backdrop = backdrop,
        blurRadiusPx = blurRadiusPx,
        vibrancy = vibrancy,
    )

    /**
     * Updates an existing [MistEffectNode] with this element's current parameters,
     * allowing the node to be reused across recompositions instead of being torn down
     * and recreated.
     */
    override fun update(node: MistEffectNode) {
        // Cache Invalidation:
        // If the blur radius or vibrancy changed, we nullify the cached RenderEffect
        // and ColorMatrix inside the node so they are regenerated on the next draw pass.
        // This avoids recreating expensive GPU objects if the values haven't actually changed.
        if (node.blurRadiusPx != blurRadiusPx) node.effect = null
        if (node.vibrancy != vibrancy) node.saturation = null

        // Apply new values
        node.backdrop = backdrop
        node.blurRadiusPx = blurRadiusPx
        node.vibrancy = vibrancy

        // Trigger a redraw cycle:
        // Because this modifier manipulates pixels on the screen, changing its properties
        // must explicitly flag the node's layout for a redraw.
        node.invalidateDraw()
    }

    /**
     * Exposes this modifier's parameters to tooling (e.g. the Layout Inspector) under the
     * name `MistEffect`. This has no impact on runtime performance or logic.
     */
    override fun InspectorInfo.inspectableProperties() {
        name = "MistEffect"
        properties["backdrop"] = backdrop
        properties["blurRadiusPx"] = blurRadiusPx
        properties["vibrancy"] = vibrancy
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MistEffectElement

        if (blurRadiusPx != other.blurRadiusPx) return false
        if (vibrancy != other.vibrancy) return false
        if (backdrop != other.backdrop) return false

        return true
    }

    override fun hashCode(): Int {
        var result = blurRadiusPx.hashCode()
        result = 31 * result + vibrancy.hashCode()
        result = 31 * result + backdrop.hashCode()
        return result
    }
}

/**
 * Applies a frosted "mist" (glassmorphism) effect to the modified composable, using
 * a captured background layer to simulate translucent, blurred glass.
 *
 * This modifier chains a structural decorator (for shape, color, and noise) with the
 * heavy GPU blur logic ([MistEffectElement]) to create the final glass aesthetic.
 *
 * @param backdrop The [Backdrop] state containing the pre-captured UI layer behind this component.
 * @param surface The base background color of the glass panel. Often a semi-transparent color
 *                (e.g., `Color.White.copy(alpha = 0.1f)`) to ensure the panel has some physical presence.
 * @param blurRadiusPx The radius of the Gaussian blur in pixels. Defaults to 0f (no blur).
 * @param vibrancy A multiplier for the background's color saturation. Values > 1.0f boost the
 *                 underlying colors, preventing the glass from looking muddy or washed out.
 * @param tint An optional color overlay applied on top of the blurred background, useful for
 *             enforcing light/dark mode legibility.
 * @param noiseAmount The intensity of the procedural grain/noise applied to the glass.
 *                    Noise is crucial for preventing color banding in smooth blurs.
 * @param edgeHighlight An optional border (e.g., a thin, semi-transparent white line) to simulate
 *                      light catching the physical edge of the glass.
 * @param shape The geometric shape of the glass panel. Used to clip the blur and draw the borders.
 */
@RequiresApi(Build.VERSION_CODES.S)
@Stable
fun Modifier.mistEffect(
    backdrop: Backdrop,
    surface: Color,
    blurRadiusPx: Float = 0f,
    vibrancy: Float = 1.0f,
    tint: Color = Color.Unspecified,
    noiseAmount: Float = 0f,
    edgeHighlight: BorderStroke? = null,
    shape: Shape = RectangleShape,
) = this then
        // 1. Structural & Aesthetic Layer (Decorator)
        // We delegate the standard drawing properties (base colors, noise texture, borders, and clipping)
        // to a separate modifier. This keeps the rendering logic modular.
        // The decorator draws the foundation and the top-level accents.
        Modifier.decorator(
            backgroundColor = surface,
            foregroundColor = tint,
            roughness = noiseAmount,
            border = edgeHighlight,
            shape = shape
        ) then
        // 2. GPU Blur Layer (MistEffectElement)
        // This node specifically handles the expensive RenderEffect, GraphicsLayer buffering,
        // and scaling down the backdrop. It sits behind the decorator's foreground but
        // incorporates the backdrop state to draw the actual frosted glass effect.
        MistEffectElement(
            backdrop = backdrop,
            blurRadiusPx = blurRadiusPx,
            vibrancy = vibrancy,
        )