package com.zs.compose.foundation.backdrop

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.node.requireDensity
import androidx.compose.ui.node.requireGraphicsContext
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.toIntSize

private const val TAG = "LayerBackdrop"

class LayerBackdrop : Backdrop {
    /**
     * The source's recorded content. Assigned by the source once it starts
     * recording (e.g. `layer.record { drawContent() }`).
     */
    internal lateinit var content: GraphicsLayer

    /** The source'son-screen coordinates, set via its own `onGloballyPositioned`. */
    internal var position: LayoutCoordinates? = null

    /**
     * Draws [content], translated so the region behind [coordinates] lines up under the
     * consumer's own bounds.
     */
    override fun DrawScope.drawRegion(coordinates: LayoutCoordinates?) {
        val parent = position

        // Normal for the first frame or two, not an error — one or both sides
        // haven't completed layout yet.
        if (parent == null || coordinates == null) return

        withTransform({
            // How far child sits from parent's origin, in parent's local space —
            // this is what lets us "crop" the right chunk of the layer for child.
            val offset = if (parent.findRootCoordinates() === coordinates.findRootCoordinates()) {
                // Common case: same window. localPositionOf is transform-aware
                // (handles rotation/scale from ancestors), not just a translation.
                parent.localPositionOf(coordinates)
            } else {
                // child lives in a different Android Window (Dialog/Popup) than
                // the source — localPositionOf can't bridge that, so fall back to
                // window-space coordinates. Checked explicitly (not via try/catch)
                // so this never throws, even every frame while a dialog is open.
                coordinates.positionOnScreen() - parent.positionOnScreen()
            }
            translate(-offset.x, -offset.y)
            // translate(-offset.x, -offset.y)
        }) {
            drawLayer(content)
        }
    }
}

/**
 * Remembers a single [Backdrop] shared by one source and its consumers.
 */
@Composable
@NonRestartableComposable
fun rememberBackdropLayer(): LayerBackdrop = remember(::LayerBackdrop)

private class LayerBackdropNode(
    var backdrop: LayerBackdrop,
) : Modifier.Node(), DrawModifierNode, GlobalPositionAwareModifierNode {

    override fun onAttach() {
        // Layer is owned by this node's lifecycle, not by Backdrop itself —
        // Backdrop just holds a reference so consumers can read it.
        backdrop.content = requireGraphicsContext().createGraphicsLayer()
    }

    override fun onDetach() {
        // Release the hardware layer explicitly; GraphicsContext pools these,
        // so leaving this out leaks a layer every time the source leaves
        // composition (e.g. it's inside a removed conditional branch).
        requireGraphicsContext().releaseGraphicsLayer(backdrop.content)
        backdrop.position = null
    }

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        backdrop.position = coordinates
        invalidateDraw()
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        val layer = backdrop.content
        Log.d("backdrop", "draw: backdrop")
        // Record this node’s content into the shared layer
        layer.record(size.toIntSize()) {
            val prevDensity = drawContext.density
            drawContext.density = requireDensity()
            try {
                // Redirect the outer ContentDrawScope’s content into this layer
                this@draw.drawContent()
            } finally {
                // Always restore the previous density
                drawContext.density = prevDensity
            }
        }
    }
}

private class LayerBackdropElement(
    val backdrop: LayerBackdrop,
) : ModifierNodeElement<LayerBackdropNode>() {

    override fun create(): LayerBackdropNode = LayerBackdropNode(backdrop)

    override fun update(node: LayerBackdropNode) {
        // Only reached if the caller passes a *different* Backdrop instance
        // across recomposition (unusual — normally it's remembered once).
        node.backdrop = backdrop
        Log.d(TAG, "update: $node")
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "backdrop"
        properties["backdrop"] = backdrop
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LayerBackdropElement

        return backdrop == other.backdrop
    }

    override fun hashCode(): Int {
        return backdrop.hashCode()
    }
}

/**
 * Marks this composable as the backdrop source: its content is drawn normally,
 * and additionally recorded into [layerBackdropProvider]'s [Backdrop.content] so that any
 * consumer calling `with(backdrop) { draw(...) }` elsewhere in the tree can
 * read a translated view of it.
 */
@Stable
fun Modifier.layerBackdropProvider(backdrop: LayerBackdrop): Modifier = this then LayerBackdropElement(backdrop)