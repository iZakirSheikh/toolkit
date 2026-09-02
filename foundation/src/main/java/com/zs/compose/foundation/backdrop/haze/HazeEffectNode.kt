package com.zs.compose.foundation.backdrop.haze

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
import com.zs.compose.foundation.util.setToLuminance

private const val TAG = "BlurEffectNode"

/**
 * [Modifier.Node] that draws a blurred, vibrancy/tint-adjusted view of [backdrop]
 * behind this composable's own content.
 */
internal class HazeEffectNode(
    var backdrop: Backdrop,
    var config: BlurConfig,
    var vibrancy: Float,
    var luminsity: Float,
    var tint: Color,
) : Modifier.Node(), DrawModifierNode, GlobalPositionAwareModifierNode {

    // Disables automatic invalidation. We handle recomposition and redraws manually
    // inside the [HazeEffectElement.update] function and [onGloballyPositioned].
    // This prevents unnecessary GPU redraws when non-visual properties change.
    override val shouldAutoInvalidate: Boolean = false

    private lateinit var content: GraphicsLayer
    private var position: LayoutCoordinates? = null
    var effect: BlurEffect? = null
    var filter: ColorFilter? = null
        get() {
            // Reuse the cached filter until any dependent property changes.
            field?.let { return it }

            // No filter is needed when all color adjustments are disabled.
            // `-1f` means luminosity has not been explicitly set.
            if (vibrancy == 1f && luminsity == -1f)
                return null

            // Combine saturation and luminosity adjustments into a single matrix.
            val matrix = ColorMatrix().apply {
                setToSaturation(vibrancy)
                timesAssign(
                    ColorMatrix().apply {
                        setToLuminance(luminsity)
                    }
                )
            }

            // Cache the filter to avoid rebuilding it on every draw.
            return ColorFilter.colorMatrix(matrix).also {
                field = it
            }
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
        // Step 1: Configure the offscreen layer with the current (possibly
        // just-rebuilt, see filter's getter) color filter.
        content.colorFilter = filter

        // Step 2: Read the downsample factor and blur radiusPx for this frame.
        //
        // The backdrop is rendered at a reduced resolution before being
        // blurred. This significantly reduces the number of pixels the
        // blur operation needs to process.
        val (downsample, radiusPx) = config
        // Step 3: Rebuild the blur effect ONLY when radiusPx has actually
        // changed since we last built one — not merely "if none exists yet".
        // This is what lets an animated radiusPx keep updating correctly
        // instead of freezing at its first non-zero value.
        if (effect == null) {
            if (radiusPx != 0f)
                effect = BlurEffect(
                    radiusPx,
                    radiusPx,
                    TileMode.Decal // Prevents transparent/empty pixels from bleeding in at the edges.
                )
            // since effect is already null from update of element hence this is good
            content.renderEffect = effect
        }
        // Step 3: Calculate the size of the reduced-resolution backdrop buffer.
        val newSize = IntSize(
            (size.width * downsample).toInt(),
            (size.height * downsample).toInt()
        )

        // Step 4: Capture the portion of the BACKDROP that sits behind this node.
        content.record(size = newSize) {
            scale(scaleX = downsample, scaleY = downsample, pivot = Offset.Zero) {
                with(backdrop) {
                    draw(this@HazeEffectNode.position)
                }
            }
        }

        // Step 6: Draw the PROCESSED SURFACE back at its original size.
        //
        // The small blurred layer is stretched back to the node's full size
        // and rendered on top of the tinted base.
        if (downsample != 0f) // dont draw if downsample is 0
            scale(scaleX = 1f / downsample, scaleY = 1f / downsample, pivot = Offset.Zero) {
                drawLayer(content)
            }
        // Step 7: Draw the tint as noraml overlay
        //
        // the user must contrl opacity of tint through its alpha
        if (tint.isSpecified)
            drawRect(tint)

        // Step 7: Draw the FOREGROUND CONTENT.
        //
        // This renders the actual children of this composable, such as text
        // or icons.
        drawContent()
    }
}