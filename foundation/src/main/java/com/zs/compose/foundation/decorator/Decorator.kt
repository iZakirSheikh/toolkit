package com.zs.compose.foundation.decorator

import androidx.annotation.FloatRange
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.layer.setOutline
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.LayoutModifierNode
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
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.offset
import com.zs.compose.foundation.R
import com.zs.compose.foundation.requirePrecondition

private const val TAG = "Decorator"

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
 * @param roughness Controls the grain effect intensity. Value ranges from 0 (off) to 1.
 * @param edgeInsets The additional space to be applied around the component's boundaries. Defaults to [EdgeInsets(0.dp)].
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
    borderPathEffect: PathEffect? = null,

    //
    outlineColor: Color = Color.Unspecified,
    outlineGap: Dp = 0.dp,
    outlineBrush: Brush? = null,
    outlineWidth: Dp = 0.dp,
    outlinePathEffect: PathEffect? = null,

    //
    elevation: Dp = Dp.Unspecified,

    // scale
    @FloatRange(from = 0.0, to = 1.0) scaleX: Float = 1.0f,
    @FloatRange(from = 0.0, to = 1.0) scaleY: Float = 1.0f,

    // noise
    @FloatRange(from = 0.0, to = 1.0) roughness: Float = 0.0f,

    // insets
    edgeInsets: EdgeInsets = EdgeInsets()
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
    borderPathEffect = borderPathEffect,
    //
    outlineColor = outlineColor,
    outlineGap = outlineGap,
    outlineBrush = outlineBrush,
    outlineWidth = outlineWidth,
    outlinePathEffect = outlinePathEffect,

    //
    elevation = elevation,

    // scale
    scaleX = scaleX,
    scaleY = scaleY,

    //
    roughness = roughness,

    //
    edgeInsets = edgeInsets
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
    borderPathEffect: PathEffect? = null,
    //
    outline: BorderStroke? = null,
    outlineGap: Dp = 0.dp,
    outlinePathEffect: PathEffect? = null,

    //
    elevation: Dp = Dp.Unspecified,
    @FloatRange(from = 0.0, to = 1.0) scale: Float = 1.0f,
    @FloatRange(from = 0.0, to = 1.0) roughness: Float = 0.0f,
    //
    edgeInsets: EdgeInsets = EdgeInsets(),
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
    borderPathEffect = borderPathEffect,
    //
    outlineColor = Color.Unspecified,
    outlineGap = outlineGap,
    outlineBrush = outline?.brush,
    outlineWidth = outline?.width ?: 0.dp,
    outlinePathEffect = outlinePathEffect,

    //
    scaleX = scale,
    scaleY = scale,
    //
    elevation = elevation,
    //
    roughness = roughness,
    //
    edgeInsets = edgeInsets
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
    val borderPathEffect: PathEffect?,

    // Outline styling (for styling that strictly traces the outer contour of the content.)
    var outlineColor: Color,
    var outlineGap: Dp,
    var outlineBrush: Brush?,
    var outlineWidth: Dp,
    var outlinePathEffect: PathEffect? = null, // e.g. dashed outine

    //
    val elevation: Dp,

    // scale
    @FloatRange(from = 0.0, to = 1.0) val scaleX: Float,
    @FloatRange(from = 0.0, to = 1.0) var scaleY: Float,

    //
    val roughness: Float,
    // content padding
    val edgeInsets: EdgeInsets,
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
        borderPathEffect = borderPathEffect,


        outlineColor = outlineColor,
        outlineGap = outlineGap,
        outlineBrush = outlineBrush,
        outlineWidth = outlineWidth,
        outlinePathEffect = outlinePathEffect,

        //
        elevation = elevation,

        //
        scaleX = scaleX,
        scaleY = scaleY,

        //
        roughness = roughness,
        //
        edgeInsets = edgeInsets
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

        // border
        if (node.borderWidth != borderWidth || node.borderPathEffect != borderPathEffect)
            node.borderStroke = null // reset
        node.borderColor = borderColor
        node.borderBrush = borderBrush
        node.borderWidth = borderWidth
        // outline stroke
        if (node.outlineWidth != outlineWidth || node.outlinePathEffect != outlinePathEffect)
            node.outlineStroke = null // reset
        node.outlineColor = outlineColor
        node.outlineGap = outlineGap
        node.outlineBrush = outlineBrush
        node.outlineWidth = outlineWidth
        node.outlinePathEffect = outlinePathEffect

        //
        node.elevation = node.elevation
        //
        node.scaleX = scaleX
        node.scaleY = scaleY
        // roughness
        if (node.roughness <= 0) node.noiseEffectBrush = null // clean up.
        node.roughness = roughness

        //
        node.edgeInsets = edgeInsets
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
        if (scaleX != other.scaleX) return false
        if (scaleY != other.scaleY) return false
        if (roughness != other.roughness) return false
        if (backgroundColor != other.backgroundColor) return false
        if (backgroundBrush != other.backgroundBrush) return false
        if (foregroundColor != other.foregroundColor) return false
        if (foregroundBrush != other.foregroundBrush) return false
        if (shape != other.shape) return false
        if (borderColor != other.borderColor) return false
        if (borderBrush != other.borderBrush) return false
        if (borderWidth != other.borderWidth) return false
        if (borderPathEffect != other.borderPathEffect) return false
        if (outlineColor != other.outlineColor) return false
        if (outlineGap != other.outlineGap) return false
        if (outlineBrush != other.outlineBrush) return false
        if (outlineWidth != other.outlineWidth) return false
        if (outlinePathEffect != other.outlinePathEffect) return false
        if (elevation != other.elevation) return false
        if (edgeInsets != other.edgeInsets) return false

        return true
    }

    override fun hashCode(): Int {
        var result = backgroundAlpha.hashCode()
        result = 31 * result + foregroundAlpha.hashCode()
        result = 31 * result + scaleX.hashCode()
        result = 31 * result + scaleY.hashCode()
        result = 31 * result + roughness.hashCode()
        result = 31 * result + backgroundColor.hashCode()
        result = 31 * result + (backgroundBrush?.hashCode() ?: 0)
        result = 31 * result + foregroundColor.hashCode()
        result = 31 * result + (foregroundBrush?.hashCode() ?: 0)
        result = 31 * result + shape.hashCode()
        result = 31 * result + borderColor.hashCode()
        result = 31 * result + (borderBrush?.hashCode() ?: 0)
        result = 31 * result + borderWidth.hashCode()
        result = 31 * result + (borderPathEffect?.hashCode() ?: 0)
        result = 31 * result + outlineColor.hashCode()
        result = 31 * result + outlineGap.hashCode()
        result = 31 * result + (outlineBrush?.hashCode() ?: 0)
        result = 31 * result + outlineWidth.hashCode()
        result = 31 * result + (outlinePathEffect?.hashCode() ?: 0)
        result = 31 * result + elevation.hashCode()
        result = 31 * result + edgeInsets.hashCode()
        return result
    }
}

// Custom Modifier.Node that handles background, foreground, border, elevation,
// scaling, noise effects, and content padding in a single unified draw/layout pass.
private class DecoratorNode(
    // 🎨 Background styling
    var backgroundColor: Color,          // Solid background color
    var backgroundBrush: Brush?,         // Gradient/pattern background
    var backgroundAlpha: Float,          // Transparency for background

    // 🎨 Foreground styling (overlay drawn above content)
    var foregroundColor: Color,
    var foregroundBrush: Brush?,
    var foregroundAlpha: Float,

    // 🔲 Shape definition (used for outline, clipping, border)
    var shape: Shape,

    // ⬛ Border styling
    var borderColor: Color,
    var borderBrush: Brush?,
    var borderWidth: Dp,
    var borderPathEffect: PathEffect? = null, // e.g. dashed border

    // Outline styling (for styling that strictly traces the outer contour of the content.)
    var outlineColor: Color,
    var outlineGap: Dp,
    var outlineBrush: Brush?,
    var outlineWidth: Dp,
    var outlinePathEffect: PathEffect? = null, // e.g. dashed outine

    // 🌑 Elevation (shadow depth)
    var elevation: Dp,

    // 🔍 Transformations
    var scaleX: Float,
    var scaleY: Float,

    // 🎛️ Noise effect (grain overlay)
    var roughness: Float,

    // 📐 Content padding (space inside border before child content)
    var edgeInsets: EdgeInsets
) : Modifier.Node(), DrawModifierNode, LayoutModifierNode, ObserverModifierNode,
    SemanticsModifierNode, CompositionLocalConsumerModifierNode {
    // Disable auto-invalidation for performance; we manually control redraws
    // This node doesn’t affect parent bounds (important for hit-testing/layout)
    override val shouldAutoInvalidate = false
    override val isImportantForBounds = false

    // GraphicsLayer used for clipping, shadow, scaling, and recording draw ops
    private lateinit var graphicsLayer: GraphicsLayer

    // 🗂️ Cached outline state to avoid recomputation unless inputs change
    private var lastSize: Size = Size.Unspecified
    private var lastLayoutDirection: LayoutDirection? = null
    private var lastOutline: Outline? = null
    private var lastShape: Shape? = null
    private var tmpOutline: Outline? = null

    // Cached shader brush for noise effect
    // Cached border stroke style
    var noiseEffectBrush: ShaderBrush? = null
    var borderStroke: Stroke? = null
    var outlineStroke: Stroke? = null

    // 🔗 Lifecycle: initialize graphics layer when node is attached
    override fun onAttach() {
        if (!(::graphicsLayer.isInitialized) || graphicsLayer.isReleased) graphicsLayer =
            requireGraphicsContext().createGraphicsLayer()
    }

    // 📏 Measure phase: apply content padding and enforce constraints
    override fun MeasureScope.measure(
        measurable: Measurable, constraints: Constraints
    ): MeasureResult {
        // Resolve padding based on layout direction
        val leftPadding = edgeInsets.calculateLeftPaddingInset(layoutDirection)
        val topPadding = edgeInsets.calculateTopInset()
        val rightPadding = edgeInsets.calculateRightInset(layoutDirection)
        val bottomPadding = edgeInsets.calculateBottomInset()

        // Ensure padding is valid
        requirePrecondition((leftPadding >= 0.dp) && (topPadding >= 0.dp) && (rightPadding >= 0.dp) && (bottomPadding >= 0.dp)) {
            "Padding must be non-negative"
        }

        // Convert to pixels
        val roundedLeftPadding = leftPadding.roundToPx()
        val horizontal = roundedLeftPadding + rightPadding.roundToPx()
        val roundedTopPadding = topPadding.roundToPx()
        val vertical = roundedTopPadding + bottomPadding.roundToPx()

        // Measure child with reduced constraints (accounting for padding)
        val placeable = measurable.measure(constraints.offset(-horizontal, -vertical))

        // Expand measured size to include padding
        val width = constraints.constrainWidth(placeable.width + horizontal)
        val height = constraints.constrainHeight(placeable.height + vertical)

        // Place child inside padded area
        return layout(width, height) { placeable.place(roundedLeftPadding, roundedTopPadding) }
    }

    // ♿ Accessibility semantics: expose shape
    override fun SemanticsPropertyReceiver.applySemantics() {
        this.shape = this@DecoratorNode.shape
    }

    // 🔄 Reset cached outline when observed reads change
    override fun onObservedReadsChanged() {
        lastSize = Size.Unspecified
        lastLayoutDirection = null
        lastOutline = null
        lastShape = null
        invalidateDraw() // force redraw with updated cache
    }

    // 🖼️ Outline calculation with caching
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

    // 🎨 Draw phase: background → content → noise → foreground → border
    override fun ContentDrawScope.draw() {
        val outline = obtainOutline()
        val borderWidthPx = if (borderWidth.isSpecified) borderWidth.toPx() else -1f

        // Draws a silhouette-style outline positioned at a configurable distance (GAP)
        // from the actual content. The GAP ensures the outline sits outside the shape
        // rather than overlapping it.
        //
        // Currently, we only support drawing outlines from paths, which may represent
        // complex shapes (e.g., a compact disk with a hole in the center). For now, we
        // assume the background covers any internal holes; in future we may revisit
        // this with more precise operations.
        //
        // TODO: Explore better alternatives such as clipPath or PathOperation.Difference
        // for handling complex shapes and internal cutouts.
        //
        // Note: This outline is drawn here instead of using a graphicsLayer to avoid
        // unintended clipping behavior.
        val outlineWidthPx = outlineWidth.toPx().coerceAtLeast(0f)
        val outlineGapPx = outlineGap.toPx().coerceAtLeast(0f)
        if (outlineWidthPx >= 0 && (outlineColor.isSpecified || outlineBrush != null)) {
            // Compute scale factors relative to the content size.
            // The outline is centered, so we expand by GAP and half the outline width
            // to ensure the stroke sits correctly outside the content.
            val scaleOutlineX = (size.width + outlineGapPx * 2 + outlineWidthPx / 2) / size.width
            val scaleOutlineY = (size.height + outlineGapPx * 2 + outlineWidthPx / 2) / size.height

            scale(scaleX = scaleOutlineX, scaleY = scaleOutlineY) {
                val stroke = outlineStroke ?: Stroke(outlineWidthPx, pathEffect = outlinePathEffect)
                outlineStroke = stroke
                if (outlineColor.isSpecified)
                    drawOutline(outline, color = outlineColor, style = stroke)
                val brush = outlineBrush ?: return@scale
                drawOutline(outline, brush = brush, style = stroke)
            }
        }

        // Configure graphics layer
        graphicsLayer.setOutline(outline)
        graphicsLayer.shadowElevation = elevation.toPx()
        graphicsLayer.clip = true
        graphicsLayer.scaleX = scaleX
        graphicsLayer.scaleY = scaleY

        // Record drawing commands into graphics layer
        graphicsLayer.record {
            // Draw Background
            // Backgrounds are drawn first, behind the content.
            if (backgroundColor != Color.Unspecified) drawRect(
                backgroundColor,
                alpha = backgroundAlpha
            )
            val backgroundBrush = backgroundBrush
            if (backgroundBrush != null) drawRect(backgroundBrush, alpha = backgroundAlpha)
            // B. Draw Main Content
            // This calls the actual Composable content that this modifier is attached to.
            this@draw.drawContent()
            // Apply Texture Effects
            // If noise is enabled, we overlay the noise texture using the Overlay blend mode.
            // This adds fine-grained grain/texture to both the background and the content,
            // increasing visual depth without relying on high-resolution assets.
            val noise =
                if (noiseEffectBrush == null && roughness > 0) ShaderBrush(id = R.drawable.noise) else noiseEffectBrush
            noiseEffectBrush = noise // cache
            if (noise != null) drawRect(noise, alpha = roughness, blendMode = BlendMode.Hardlight)

            // Draw Foreground
            // Foregrounds (overlays) are drawn on top of the content, but inside the border.
            if (foregroundColor != Color.Unspecified) drawRect(
                foregroundColor,
                alpha = foregroundAlpha
            )
            val foregroundBrush = foregroundBrush
            if (foregroundBrush != null) drawRect(foregroundBrush, alpha = foregroundAlpha)

            // Draw Border/Outline
            // We draw the border last. We use a Stroke style with double thickness (borderWidthPx * 2)
            // because drawOutline centered on the path clips half of the stroke width inside/outside.
            if (borderWidthPx != -1f && (borderColor.isSpecified || borderBrush != null)) {
                val style = borderStroke ?: Stroke(borderWidthPx * 2, pathEffect = borderPathEffect)
                borderStroke = style // cache
                if (borderColor.isSpecified) drawOutline(
                    outline,
                    style = style,
                    color = borderColor
                )
                val brush = borderBrush
                if (brush != null) drawOutline(outline, style = style, brush = brush)
            }


        }
        drawLayer(graphicsLayer)
    }

    override fun onDetach() {
        super.onDetach()
        if (!(::graphicsLayer.isInitialized) || graphicsLayer.isReleased) graphicsLayer =
            requireGraphicsContext().createGraphicsLayer()
        noiseEffectBrush = null
        borderStroke = null
    }
}