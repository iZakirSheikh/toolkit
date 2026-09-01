package com.zs.compose.foundation.backdrop.haze

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
 * A [ModifierNodeElement] that creates and updates a [HazeEffectNode], applying a frosted
 * "mist" (glassmorphism-style) blur effect to the composable it modifies.
 * @see Modifier.hazeEffect
 */
private class HazeEffectElement(
    val backdrop: Backdrop,
    val blurProfile: BlurProfile,
    val vibrancy: Float,
    val tint: Color
) : ModifierNodeElement<HazeEffectNode>() {

    /**
     * Creates a new [HazeEffectNode] initialized with this element's current parameters.
     */
    override fun create(): HazeEffectNode = HazeEffectNode(
        backdrop = backdrop,
        blurProfile = blurProfile,
        vibrancy = vibrancy,
        tint = tint
    )

    /**
     * Updates an existing [HazeEffectNode] with this element's current parameters,
     * allowing the node to be reused across recompositions instead of being torn down
     * and recreated.
     */
    override fun update(node: HazeEffectNode) {
        // Cache Invalidation:
        // If the blur profile (radius) or vibrancy changed, we nullify
        // the cached RenderEffect and ColorMatrix inside the node so they are regenerated on
        // the next draw pass. This avoids recreating expensive GPU objects if the values
        // haven't actually changed.
        if (node.blurProfile.radiusPx != blurProfile.radiusPx) node.effect = null
        if (node.vibrancy != vibrancy) node.saturation = null

        // Apply new values
        node.backdrop = backdrop
        node.blurProfile = blurProfile
        node.vibrancy = vibrancy
        node.tint = tint

        // Trigger a redraw cycle:
        // Because this modifier manipulates pixels on the screen, changing its properties
        // must explicitly flag the node's layout for a redraw.
        node.invalidateDraw()
    }

    /**
     * Exposes this modifier's parameters to tooling (e.g. the Layout Inspector) under the
     * name `HazeEffect`. This has no impact on runtime performance or logic.
     */
    override fun InspectorInfo.inspectableProperties() {
        name = "HazeEffect"
        properties["backdrop"] = backdrop
        properties["blurProfile"] = blurProfile
        properties["vibrancy"] = vibrancy
        properties["tint"] = tint
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HazeEffectElement

        if (vibrancy != other.vibrancy) return false
        if (backdrop != other.backdrop) return false
        if (blurProfile != other.blurProfile) return false
        if (tint != other.tint) return false

        return true
    }

    override fun hashCode(): Int {
        var result = vibrancy.hashCode()
        result = 31 * result + backdrop.hashCode()
        result = 31 * result + blurProfile.hashCode()
        result = 31 * result + tint.hashCode()
        return result
    }


}

/**
 * Applies a frosted "haze" (glassmorphism) effect to the modified composable, using
 * a captured background layer to simulate translucent, blurred glass.
 *
 * This modifier chains a structural decorator (for shape, color, and noise) with the
 * heavy GPU blur logic ([HazeEffectElement]) to create the final glass aesthetic.
 *
 * @param backdrop The [Backdrop] state containing the pre-captured UI layer behind this component.
 * @param surface The base background color of the glass panel. Often a non-transparent color
 *                (e.g., `Color.White`) to ensure the panel has some physical presence.
 * @param blurProfile The [BlurProfile] defining the performance and visual specifications
 *                    for the blur, including the pixel radius and downsample factor.
 *                    Defaults to a radius of 10f and a downsample factor of 0.5f.
 * @param vibrancy A multiplier for the background's color saturation. Values > 1.0f boost the
 *                 underlying colors, preventing the glass from looking muddy or washed out.
 * @param tint An optional color overlay applied with a Softlight blend mode before the blur layer.
 * @param noiseAmount The intensity of the procedural grain/noise applied to the glass.
 *                    Noise is crucial for preventing color banding in smooth blurs.
 * @param edgeHighlight An optional border (e.g., a thin, semi-transparent white line) to simulate
 *                      light catching the physical edge of the glass.
 * @param shape The geometric shape of the glass panel. Used to clip the blur and draw the borders.
 */
@RequiresApi(Build.VERSION_CODES.S)
@Stable
fun Modifier.hazeEffect(
    backdrop: Backdrop,
    surface: Color,
    blurProfile: BlurProfile = BlurProfile(0.5f, 10f),
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
            //foregroundColor = tint,
            roughness = noiseAmount,
            border = edgeHighlight,
            shape = shape
        ) then
        // 2. GPU Blur Layer (HazeEffectElement)
        // This node specifically handles the expensive RenderEffect, GraphicsLayer buffering,
        // and scaling down the backdrop. It sits behind the decorator's foreground but
        // incorporates the backdrop state to draw the actual frosted glass effect.
        HazeEffectElement(
            backdrop = backdrop,
            blurProfile = blurProfile,
            vibrancy = vibrancy,
            tint = tint
        )