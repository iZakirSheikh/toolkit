package com.zs.compose.foundation

import androidx.annotation.FloatRange
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.layer.setOutline
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ObserverModifierNode
import androidx.compose.ui.node.SemanticsModifierNode
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.node.invalidateSemantics
import androidx.compose.ui.node.observeReads
import androidx.compose.ui.node.requireGraphicsContext
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.isSpecified

/**
 * A highly optimized, consolidated modifier that applies common visual decorations to a component.
 *
 * Instead of chaining multiple modifiers like `Modifier.background()`, `Modifier.border()`,
 * `Modifier.clip()`, and `Modifier.shadow()`, this modifier handles all these aspects in a single
 * draw pass using a [GraphicsLayer].
 *
 * ### Key Benefits:
 * 1. **Performance**: Reduces the depth of the modifier tree and minimizes recomposition overhead.
 * 2. **Caching**: Automatically caches the [Outline] of the [shape] to prevent expensive path
 * recalculations on every frame, only invalidating when the size, layout direction, or shape state changes.
 * 3. **Layer-based Elevation**: Uses hardware-accelerated [GraphicsLayer] for shadow elevation.
 * 4. **Unified Foreground**: Supports drawing a foreground color or brush over the content, useful
 * for overlays or state-based dimming.
 *
 * @param backgroundColor The solid color to draw as the background.
 * @param backgroundBrush The optional brush for complex background patterns (gradients, etc.).
 * @param backgroundAlpha The opacity of the [backgroundBrush].
 * @param foregroundColor A solid color overlay drawn on top of the content.
 * @param foregroundBrush A brush overlay drawn on top of the content.
 * @param foregroundAlpha The opacity of the [foregroundBrush].
 * @param shape The geometric shape used for the background, border, clipping, and shadow.
 * Defaults to [RectangleShape].
 * @param borderColor The solid color of the border.
 * @param borderBrush The brush used for the border (e.g., a gradient border).
 * @param borderWidth The thickness of the border. If [Dp.Unspecified], no border is drawn.
 * @param elevation The size of the shadow below the component.
 */
@Stable
fun Modifier.decorator(
    // background
    backgroundColor: Color,
    backgroundBrush: Brush? = null,
    @FloatRange(from = 0.0, to = 1.0) backgroundAlpha: Float = 1.0f,

    // foreground
    foregroundColor: Color = Color.Unspecified,
    foregroundBrush: Brush? = null,
    @FloatRange(from = 0.0, to = 1.0) foregroundAlpha: Float = 1f,

    // shape
    shape: Shape = RectangleShape,
    //
    borderColor: Color = Color.Unspecified,
    borderBrush: Brush? = null,
    borderWidth: Dp = Dp.Unspecified,

    //
    elevation: Dp = Dp.Unspecified
) = this then DecoratorElement(
    // background
    backgroundColor = backgroundColor,
    backgroundBrush = backgroundBrush,
    backgroundAlpha = backgroundAlpha,

    // foreground
    foregroundColor = foregroundColor,
    foregroundBrush = foregroundBrush,
    foregroundAlpha = foregroundAlpha,

    // shape
    shape = shape,
    //
    borderColor = borderColor,
    borderBrush = borderBrush,
    borderWidth = borderWidth,
    //
    elevation = elevation
)

@Stable
fun Modifier.decorator(
    // background
    backgroundColor: Color,
    backgroundBrush: Brush? = null,
    @FloatRange(from = 0.0, to = 1.0) backgroundAlpha: Float = 1.0f,

    // foreground
    foregroundColor: Color = Color.Unspecified,
    foregroundBrush: Brush? = null,
    @FloatRange(from = 0.0, to = 1.0) foregroundAlpha: Float = 1f,

    // shape
    shape: Shape = RectangleShape,
    // border
    border: BorderStroke? = null,

    //
    elevation: Dp = Dp.Unspecified
) = decorator(
    // background
    backgroundColor = backgroundColor,
    backgroundBrush = backgroundBrush,
    backgroundAlpha = backgroundAlpha,

    // foreground
    foregroundColor = foregroundColor,
    foregroundBrush = foregroundBrush,
    foregroundAlpha = foregroundAlpha,

    // shape
    shape = shape,
    //
    borderBrush = border?.brush,
    borderWidth = border?.width ?: Dp.Unspecified,
    //
    elevation = elevation
)

private class DecoratorElement(
    // background
    val backgroundColor: Color,
    val backgroundBrush: Brush?,
    val backgroundAlpha: Float,

    // foreground
    val foregroundColor: Color,
    val foregroundBrush: Brush?,
    val foregroundAlpha: Float,

    // shape
    val shape: Shape,
    //
    val borderColor: Color,
    val borderBrush: Brush?,
    val borderWidth: Dp,

    //
    val elevation: Dp
) : ModifierNodeElement<DecoratorNode>() {
    override fun create(): DecoratorNode = DecoratorNode(
        // background
        backgroundColor = backgroundColor,
        backgroundBrush = backgroundBrush,
        backgroundAlpha = backgroundAlpha,

        // foreground
        foregroundColor = foregroundColor,
        foregroundBrush = foregroundBrush,
        foregroundAlpha = foregroundAlpha,

        // shape
        shape = shape,
        //
        borderColor = borderColor,
        borderBrush = borderBrush,
        borderWidth = borderWidth,
        //
        elevation = elevation
    )

    override fun update(node: DecoratorNode) {
        // background
        node.backgroundColor = backgroundColor
        node.backgroundBrush = backgroundBrush
        node.backgroundAlpha = backgroundAlpha

        // foreground
        node.foregroundColor = foregroundColor
        node.foregroundBrush = foregroundBrush
        node.foregroundAlpha = foregroundAlpha

        //
        node.borderColor = borderColor
        node.borderBrush = borderBrush
        node.borderWidth = borderWidth

        //
        var elevation: Dp
        if (node.shape != shape) {
            node.shape = shape

            node.invalidateSemantics()
        }
        node.invalidateDraw()
    }

    override fun InspectorInfo.inspectableProperties() {

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DecoratorElement

        if (backgroundAlpha != other.backgroundAlpha) return false
        if (foregroundAlpha != other.foregroundAlpha) return false
        if (backgroundColor != other.backgroundColor) return false
        if (backgroundBrush != other.backgroundBrush) return false
        if (foregroundColor != other.foregroundColor) return false
        if (foregroundBrush != other.foregroundBrush) return false
        if (shape != other.shape) return false
        if (borderColor != other.borderColor) return false
        if (borderBrush != other.borderBrush) return false
        if (borderWidth != other.borderWidth) return false
        if (elevation != other.elevation) return false

        return true
    }

    override fun hashCode(): Int {
        var result = backgroundAlpha.hashCode()
        result = 31 * result + foregroundAlpha.hashCode()
        result = 31 * result + backgroundColor.hashCode()
        result = 31 * result + (backgroundBrush?.hashCode() ?: 0)
        result = 31 * result + foregroundColor.hashCode()
        result = 31 * result + (foregroundBrush?.hashCode() ?: 0)
        result = 31 * result + shape.hashCode()
        result = 31 * result + borderColor.hashCode()
        result = 31 * result + (borderBrush?.hashCode() ?: 0)
        result = 31 * result + borderWidth.hashCode()
        result = 31 * result + elevation.hashCode()
        return result
    }
}

private class DecoratorNode(
    // background
    var backgroundColor: Color,
    var backgroundBrush: Brush?,
    var backgroundAlpha: Float,

    // foreground
    var foregroundColor: Color,
    var foregroundBrush: Brush?,
    var foregroundAlpha: Float,

    // shape
    var shape: Shape,
    //
    var borderColor: Color,
    var borderBrush: Brush?,
    var borderWidth: Dp,

    //
    var elevation: Dp
) : Modifier.Node(), DrawModifierNode, ObserverModifierNode, SemanticsModifierNode {
    override val shouldAutoInvalidate = false
    override val isImportantForBounds = false

    // Naively cache outline calculation if input parameters are the same, we manually observe
    // reads inside shape#createOutline separately
    private var lastSize: Size = Size.Unspecified
    private var lastLayoutDirection: LayoutDirection? = null
    private var lastOutline: Outline? = null
    private var lastShape: Shape? = null
    private var tmpOutline: Outline? = null
    private lateinit var graphicsLayer: GraphicsLayer

    // at the time of the attachment.
    override fun onAttach() {
        if (!(::graphicsLayer.isInitialized) || graphicsLayer.isReleased)
            graphicsLayer = requireGraphicsContext().createGraphicsLayer()
    }

    override fun onObservedReadsChanged() {
        // Reset cached properties
        lastSize = Size.Unspecified
        lastLayoutDirection = null
        lastOutline = null
        lastShape = null
        // Invalidate draw so we build the cache again - this is needed because observeReads within
        // the draw scope obscures the state reads from the draw scope's observer
        invalidateDraw()
    }

    private fun ContentDrawScope.obtainOutline(): Outline {
        val outline: Outline?
        if (size == lastSize && layoutDirection == lastLayoutDirection && lastShape == shape) {
            outline = lastOutline!!
        } else {
            // Manually observe reads so we can directly invalidate the outline when it changes
            // Use tmpOutline to avoid creating an object reference to local var outline
            observeReads { tmpOutline = shape.createOutline(size, layoutDirection, this) }
            outline = tmpOutline
            tmpOutline = null
        }
        lastOutline = outline
        lastSize = size
        lastLayoutDirection = layoutDirection
        lastShape = shape
        return outline!!
    }

    override fun SemanticsPropertyReceiver.applySemantics() {
        this.shape = this@DecoratorNode.shape
    }

    override fun ContentDrawScope.draw() {
        // 1. Preparation & Layout Calculations
        // We retrieve the cached or newly calculated outline based on current size/shape.
        val outline = obtainOutline()
        // Convert Dp border width to Px for drawing. -1f acts as a sentinel for 'no border'.
        val borderWidthPx = if (borderWidth.isSpecified) borderWidth.toPx() else -1f

        // 2. Configure GraphicsLayer Properties
        // These properties affect how the entire layer is composited onto the screen.
        graphicsLayer.setOutline(outline)
        graphicsLayer.shadowElevation = elevation.toPx()
        graphicsLayer.clip = true

        // 3. Record Drawing Commands
        // We 'record' the draw calls into the GraphicsLayer. This is efficient as it avoids
        // re-executing complex draw logic unless the layer is invalidated.
        graphicsLayer.record {
            // A. Draw Background
            // Backgrounds are drawn first, behind the content.
            if (backgroundColor != Color.Unspecified)
                drawRect(backgroundColor)
            val backgroundBrush = backgroundBrush
            if (backgroundBrush != null)
                drawRect(backgroundBrush, alpha = backgroundAlpha)

            // B. Draw Main Content
            // This calls the actual Composable content that this modifier is attached to.
            this@draw.drawContent()

            // C. Draw Foreground
            // Foregrounds (overlays) are drawn on top of the content, but inside the border.
            if (foregroundColor != Color.Unspecified)
                drawRect(foregroundColor)
            val foregroundBrush = foregroundBrush
            if (foregroundBrush != null)
                drawRect(foregroundBrush, alpha = foregroundAlpha)

            // D. Draw Border/Outline
            // We draw the border last. We use a Stroke style with double thickness (borderWidthPx * 2)
            // because drawOutline centered on the path clips half of the stroke width inside/outside.
            if (borderWidthPx != -1f){
                if (borderColor.isSpecified)
                    drawOutline(outline, style = Stroke(width = borderWidthPx * 2), color = borderColor, )
                val borderBrush = borderBrush
                if (borderBrush != null)
                    drawOutline(outline, style = Stroke(width = borderWidthPx * 2), brush = borderBrush)
            }
        }

        // 4. Final Compositing
        // Draw the recorded GraphicsLayer into the current canvas.
        drawLayer(graphicsLayer)
    }
}
