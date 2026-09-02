package com.zs.compose.foundation.backdrop.haze

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.node.requireGraphicsContext
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import com.zs.compose.foundation.backdrop.Backdrop
import com.zs.compose.foundation.util.setToLuminance
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.time.measureTime

private const val TAG = "RsHazeEffectNode"

/**
 * [Modifier.Node] that draws a blurred, vibrancy/tint-adjusted view of [backdrop]
 * behind this composable's own content.
 * This node relies on a hardware-accelerated [GraphicsLayer] to capture a specific portion
 * of the screen (the [backdrop]), apply expensive GPU operations (like blurs and color matrices),
 * and render the result efficiently.
 *
 * @see Modifier.hazeEffect
 * @see Modifier.legacyHazeEffect
 */
internal class RsHazeEffectNode(
    var backdrop: Backdrop,
    var config: BlurConfig,
    var vibrancy: Float,
    var luminsity: Float,
    var tint: Color,
) : Modifier.Node(), DrawModifierNode, GlobalPositionAwareModifierNode,
    CompositionLocalConsumerModifierNode {

    // Disables automatic invalidation. We handle recomposition and redraws manually
    // inside the [HazeEffectElement.update] function and [onGloballyPositioned].
    // This prevents unnecessary GPU redraws when non-visual properties change.
    override val shouldAutoInvalidate: Boolean = false

    private lateinit var content: GraphicsLayer
    private var position: LayoutCoordinates? = null
    private var renderer: Job? = null
    var effect: RsBlurEffect? = null
    var filter: ColorFilter? = null
        get() {
            // Reuse the cached filter until any dependent property changes.
            if (field != null)
                return field

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
        effect?.release()
        effect = null
        super.onDetach()
    }

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        position = coordinates
        // If the component moves on the screen, the background behind it has visually changed.
        // We must invalidate the draw phase to capture the new portion of the backdrop.
        invalidateDraw()
    }

    override fun ContentDrawScope.draw() {
        // Step 1: Apply the current color filter to the captured layer.
        content.colorFilter = filter

        // Step 2: Read the current blur configuration.
        //
        // Downsampling reduces the number of pixels that RenderScript needs to
        // process, while radiusPx controls the blur strength.
        val (downsample, radiusPx) = config

        // Step 3: Calculate the size of the downsampled capture buffer.
        val newSize = IntSize(
            (size.width * downsample).toInt(),
            (size.height * downsample).toInt()
        )

        // Step 4: Capture the portion of the backdrop behind this node.
        //
        // The backdrop is recorded at the configured downsampled resolution.
        content.record(size = newSize) {
            scale(scaleX = downsample, scaleY = downsample, pivot = Offset.Zero) {
                with(backdrop) {
                    draw(this@RsHazeEffectNode.position)
                }
            }
        }

        // Step 5: Draw the backdrop.
        //
        // When blur is disabled, draw the captured layer directly. Since the
        // capture was downsampled, scale it back to the node's original size.
        //
        // When blur is enabled, draw the most recently processed RenderScript
        // result. `drawLayer()` handles scaling the processed layer back to size.
        if (radiusPx == 0f) {
            if (downsample != 0f)
                scale(scaleX = 1f / downsample, scaleY = 1f / downsample, pivot = Offset.Zero) {
                    drawLayer(content)
                }

            // No blur is required, so clean up any previously-created effect
            // and cancel any pending RenderScript work.
            effect?.release()
            effect = null
            renderer?.cancel()
        } else {
            effect?.drawLayer()
        }

        // Step 6: Draw the tint as a normal overlay.
        //
        // The tint's alpha controls its opacity; no additional alpha is applied.
        if (tint.isSpecified)
            drawRect(tint)

        // Step 7: Draw the foreground content above the haze effect.
        drawContent()

        // Stop here when blur is disabled. The backdrop, tint, and content have
        // already been drawn, so there is no RenderScript work to schedule.
        if (radiusPx == 0f)
            return

        // Step 8: Lazily create the RenderScript blur effect.
        //
        // The effect is created only when blur is required and reused across
        // subsequent draw passes. It is released when blur is disabled.
        val effect = effect ?: RsBlurEffect(currentValueOf(LocalContext)).also {
            effect = it
        }

        // Step 9: Process the captured layer asynchronously.
        //
        // Only one RenderScript operation runs at a time. The current processed
        // result is displayed on the next draw pass.
        if (renderer?.isActive != true) {
            renderer = coroutineScope.launch {
                val mills = measureTime {
                    effect.record(content, config)
                }
                Log.d(TAG, "draw: rendering: $mills")

                // Wait for the next frame before invalidating.
                //
                // This keeps the RenderScript processing loop synchronized with
                // the display instead of immediately triggering another draw.
                withFrameMillis { }

                // Request the next draw so the newly processed result is displayed.
                invalidateDraw()
            }
        }
    }
}