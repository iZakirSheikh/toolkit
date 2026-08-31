package com.zs.compose.foundation.backdrop.glass

import android.util.Log
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
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

internal class GlassEffectNode(
    var backdrop: Backdrop,
    var blurRadiusPx: Float
) : Modifier.Node(), DrawModifierNode, GlobalPositionAwareModifierNode {
    override val shouldAutoInvalidate: Boolean = false

    // cached params
    private lateinit var content: GraphicsLayer
    private var position: LayoutCoordinates? = null
    var effect: BlurEffect? = null // the element clears it once blur params changes.

    override fun onAttach() {
        super.onAttach()
        content = requireGraphicsContext().createGraphicsLayer()
        Log.d(TAG, "onAttach")
    }

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        position = coordinates
        invalidateDraw()
        Log.d(TAG, "onGloballyPositioned: poition: $position poition: $coordinates")
    }

    override fun ContentDrawScope.draw() {
        Log.d(TAG, "draw:")
        if (effect == null) {
            effect = BlurEffect(
                blurRadiusPx,
                blurRadiusPx,
                TileMode.Decal
            )
            content.renderEffect = effect
        }

        content.record {
            with(backdrop) {
                draw(this@GlassEffectNode.position)
            }
        }
        drawRect(Color.White) // bg
        drawLayer(content)
        drawContent()
    }

    override fun onDetach() {
        super.onDetach()
        requireGraphicsContext().releaseGraphicsLayer(content)
        effect = null
    }
}