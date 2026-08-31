package com.zs.compose.foundation.backdrop.mist

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.node.requireGraphicsContext
import com.zs.compose.foundation.backdrop.Backdrop

private const val TAG = "BlurEffectNode"

/**
 * The core [Modifier.Node] that executes the rendering logic for the mist (glassmorphism) effect.
 *
 * This node relies on a hardware-accelerated [GraphicsLayer] to capture a specific portion
 * of the screen (the [backdrop]), apply expensive GPU operations (like blurs and color matrices),
 * and render the result efficiently.
 *
 * @property backdrop The shared state holding the background content to be blurred.
 * @property blurRadiusPx The radius for the Gaussian blur.
 * @property vibrancy A multiplier for color saturation to prevent the blur from looking washed out.
 */
internal class MistEffectNode(
    var backdrop: Backdrop,
    var blurRadiusPx: Float,
    var vibrancy: Float,
) : Modifier.Node(), DrawModifierNode, GlobalPositionAwareModifierNode {

    // Disables automatic invalidation. We handle recomposition and redraws manually
    // inside the [MistEffectElement.update] function and [onGloballyPositioned].
    // This prevents unnecessary GPU redraws when non-visual properties change.
    override val shouldAutoInvalidate: Boolean = false

    /**
     * The hardware-accelerated offscreen buffer used to process the blur and color effects.
     */
    private lateinit var content: GraphicsLayer

    /**
     * Tracks the exact location of this component on the screen.
     * This is required so we know *which part* of the global backdrop to crop and draw.
     */
    private var position: LayoutCoordinates? = null

    /**
     * Lazily initialized [ColorFilter] that boosts the background's saturation.
     * The companion [MistEffectElement] sets this to `null` when [vibrancy] changes,
     * forcing it to regenerate only when absolutely necessary.
     */
    var saturation: ColorFilter? = null
        get() {
            if (field != null) return field

            // Fast path: If vibrancy is 1.0 (neutral), avoid allocating a ColorMatrix entirely.
            if (vibrancy == 1f) return null

            field = ColorFilter.colorMatrix(ColorMatrix().apply {
                setToSaturation(vibrancy)
            })
            return field
        }

    /**
     * Lazily initialized [BlurEffect].
     * The companion [MistEffectElement] sets this to `null` when [blurRadiusPx] changes,
     * forcing it to regenerate. This prevents allocating new RenderEffects on every frame.
     */
    var effect: BlurEffect? = null
        get() {
            if (field != null) return field

            field = BlurEffect(
                blurRadiusPx,
                blurRadiusPx,
                TileMode.Clamp // Clamp prevents transparent pixels from bleeding in from the edges
            )
            return field
        }

    override fun onAttach() {
        super.onAttach()
        // Allocate the offscreen layer once the node enters the composition.
        // GraphicsLayers are backed by native GPU resources, so this is
        // deferred until attach rather than done at construction time.
        content = requireGraphicsContext().createGraphicsLayer()
    }

    // IMPORTANT NOTE FOR FUTURE REFERENCE:
    // Because GraphicsLayer allocates GPU resources, you should ideally override onDetach()
    // to release it. Otherwise, removing this node from the tree might cause a memory leak.
    override fun onDetach() {
        requireGraphicsContext().releaseGraphicsLayer(content)
        super.onDetach()
    }

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        position = coordinates
        // If the component moves on the screen, the background behind it has visually changed.
        // We must invalidate the draw phase to capture the new portion of the backdrop.
        invalidateDraw()
    }

    /**
     * The core rendering loop, executed every time this composable needs to paint.
     */
    override fun ContentDrawScope.draw() {
        // Step 1: Apply our cached effects to the offscreen layer
        content.colorFilter = saturation
        content.renderEffect = effect

        // Step 2: Record the background content into our GraphicsLayer buffer.
        content.record {
            with(backdrop) {
                // Draws only the slice of the background that sits directly behind this node,
                // using the coordinates captured in onGloballyPositioned.
                draw(this@MistEffectNode.position)
            }
        }

        // Step 3: Draw the fully processed (blurred and color-filtered) layer to the screen.
        drawLayer(content)
        // Step 4: Draw the content of this componet
        drawContent()
    }
}