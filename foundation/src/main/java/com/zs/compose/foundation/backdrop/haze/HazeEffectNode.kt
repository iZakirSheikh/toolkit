package com.zs.compose.foundation.backdrop.haze

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.node.requireGraphicsContext
import androidx.compose.ui.unit.IntSize
import com.zs.compose.foundation.backdrop.Backdrop

private const val TAG = "BlurEffectNode"

/**
 * The core [Modifier.Node] that executes the rendering logic for the haze (glassmorphism) effect.
 *
 * This node relies on a hardware-accelerated [GraphicsLayer] to capture a specific portion
 * of the screen (the [backdrop]), apply expensive GPU operations (like blurs and color matrices),
 * and render the result efficiently.
 *
 * @see Modifier.hazeEffect
 *
 * @property backdrop The shared state holding the background content to be blurred.
 * @property blurProfile The performance and visual specifications for the blur (downsample factor and radius).
 * @property vibrancy A multiplier for color saturation to prevent the blur from looking washed out.
 * @property tint An optional color overlay applied with a Softlight blend mode before the blur layer.
 * @property content The hardware-accelerated offscreen buffer used to process and cache the blur and color effects.
 * @property position Tracks the exact screen coordinates of this component to properly crop the global backdrop.
 * @property saturation Lazily initialized and cached [ColorFilter] used to apply the [vibrancy] boost.
 * @property effect Lazily initialized and cached [BlurEffect] to prevent allocating new RenderEffects on every frame.
 */
internal class HazeEffectNode(
    var backdrop: Backdrop,
    var blurProfile: BlurProfile,
    var vibrancy: Float,
    var tint: Color
) : Modifier.Node(), DrawModifierNode, GlobalPositionAwareModifierNode {
    // Disables automatic invalidation. We handle recomposition and redraws manually
    // inside the [HazeEffectElement.update] function and [onGloballyPositioned].
    // This prevents unnecessary GPU redraws when non-visual properties change.
    override val shouldAutoInvalidate: Boolean = false

    private lateinit var content: GraphicsLayer
    private var position: LayoutCoordinates? = null
    var effect: BlurEffect? = null
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


    override fun ContentDrawScope.draw() {

        // Step 1: Configure the offscreen layer with the cached color filter.
        content.colorFilter = saturation

        // Step 2: Calculate the downsample factor and blur radius.
        //
        // The backdrop is rendered at a reduced resolution before being blurred.
        // This significantly reduces the number of pixels the blur operation needs
        // to process.
        val (downsample, radius) = blurProfile

        if (effect == null) {
            if (radius != 0f)
                effect = BlurEffect(
                    radius,
                    radius,
                    TileMode.Decal // Prevents transparent/empty pixels from bleeding in at the edges.
                )

            content.renderEffect = effect
        }

        // Step 3: Calculate the size of the reduced-resolution backdrop buffer.
        val newSize = IntSize(
            (size.width * downsample).toInt().coerceAtLeast(1),
            (size.height * downsample).toInt().coerceAtLeast(1)
        )

        // Step 4: Capture the portion of the BACKDROP that sits behind this node.
        content.record(size = newSize) {
            scale(scaleX = downsample, scaleY = downsample, pivot = Offset.Zero) {
                with(backdrop) {
                    draw(this@HazeEffectNode.position)
                }
            }
        }

        // Step 5: Draw the SURFACE TINT.
        //
        // We apply the softlight tint first so it blends directly with the underlying
        // canvas before the blurred layer is drawn over it.
        if (tint.isSpecified) {
            drawRect(tint, blendMode = BlendMode.Softlight)
        }

        // Step 6: Draw the PROCESSED SURFACE back at its original size.
        //
        // The small blurred layer is stretched back to the node's full size
        // and rendered on top of the tinted base.
        scale(scaleX = 1f / downsample, scaleY = 1f / downsample, pivot = Offset.Zero) {
            drawLayer(content)
        }

        // Step 7: Draw the FOREGROUND CONTENT.
        //
        // This renders the actual children of this composable, such as text or icons.
        drawContent()
    }
}