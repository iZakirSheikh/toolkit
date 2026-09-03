package com.zs.compose.foundation.backdrop.haze

import androidx.annotation.FloatRange
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Dp
import com.zs.compose.foundation.backdrop.Backdrop
import com.zs.compose.foundation.decorator.decorator

/**
 * A [ModifierNodeElement] that creates and updates a [RsHazeEffectNode], applying a frosted
 * "mist" (glassmorphism-style) blur effect to the composable it modifies.
 * @see Modifier.legacyHazeEffect
 */
private class RsHazeEffectElement(
    val backdrop: Backdrop,
    val config: BlurConfig,
    val vibrancy: Float,
    var luminsity: Float,
    val tint: Color
) : ModifierNodeElement<RsHazeEffectNode>() {

    /**
     * Creates a new [RsHazeEffectNode] initialized with this element's current parameters.
     */
    override fun create(): RsHazeEffectNode = RsHazeEffectNode(
        backdrop = backdrop,
        config = config,
        vibrancy = vibrancy,
        luminsity = luminsity,
        tint = tint
    )

    /**
     * Updates an existing [RsHazeEffectNode] with this element's current parameters,
     * allowing the node to be reused across recompositions instead of being torn down
     * and recreated.
     */
    override fun update(node: RsHazeEffectNode) {
        // Cache Invalidation:
        // If the blur profile (radius) or vibrancy or luminsity changed, we nullify
        // the cached ColorMatrix inside the node so they are regenerated on
        // the next draw pass. This avoids recreating expensive GPU objects if the values
        // haven't actually changed.
        if (node.vibrancy != vibrancy || node.luminsity != luminsity) node.filter = null

        // Apply new values
        node.backdrop = backdrop
        node.config = config
        node.vibrancy = vibrancy
        node.tint = tint
        node.settled = false

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
        properties["blurConfig"] = config
        properties["vibrancy"] = vibrancy
        properties["tint"] = tint
        properties["luminosity"] = luminsity
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RsHazeEffectElement

        if (vibrancy != other.vibrancy) return false
        if (luminsity != other.luminsity) return false
        if (backdrop != other.backdrop) return false
        if (config != other.config) return false
        if (tint != other.tint) return false

        return true
    }

    override fun hashCode(): Int {
        var result = vibrancy.hashCode()
        result = 31 * result + luminsity.hashCode()
        result = 31 * result + backdrop.hashCode()
        result = 31 * result + config.hashCode()
        result = 31 * result + tint.hashCode()
        return result
    }
}

/**
 * Applies a frosted glass (haze) effect to the modified composable.
 *
 * This is the legacy RenderScript-based implementation for older Android
 * versions. It continuously redraws the backdrop to asynchronously process
 * and display the latest blurred result.
 *
 * This approach is more expensive than the modern implementation and should
 * only be used when supporting Android versions that require the RenderScript
 * fallback.
 *
 * @see Modifier.hazeEffect
 */
@Deprecated(
    message = "Uses the legacy RenderScript-based implementation. " +
            "Use Modifier.hazeEffect instead on Android 12 (API 31) and above."
)
fun Modifier.legacyHazeEffect(
    backdrop: Backdrop,
    surface: Color,
    blurConfig: BlurConfig = BlurConfig(0.5f, 10.0f),
    tint: Color = Color.Unspecified,
    elevation: Dp = Dp.Unspecified,
    vibrancy: Float = 1.0f,
    @FloatRange(0.0, 1.0) luminosity: Float = if (tint.isUnspecified) -1f else tint.luminance(),
    @FloatRange(0.0, 1.0) noiseAmount: Float = 0f,
    edgeHighlight: BorderStroke? = null,
    shape: Shape = RectangleShape,
) = this then
        // Draw the base surface and decorative elements such as noise and border.
        Modifier.decorator(
            backgroundColor = surface,
            roughness = noiseAmount,
            border = edgeHighlight,
            elevation = elevation,
            shape = shape
        ) then
        // Capture and process the backdrop using the configured GPU blur effect.
        RsHazeEffectElement(
            backdrop = backdrop,
            config = blurConfig.copy(radiusPx = blurConfig.radiusPx.coerceAtMost(25f)),
            luminsity = luminosity,
            vibrancy = vibrancy,
            tint = tint
        )