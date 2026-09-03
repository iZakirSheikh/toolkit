package com.zs.compose.foundation.backdrop.haze

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
import com.zs.compose.foundation.backdrop.LayerBackdrop
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
        settled = false
        // If the component moves on the screen, the background behind it has visually changed.
        // We must invalidate the draw phase to capture the new portion of the backdrop.
        invalidateDraw()
    }

    // LayerBackdrop wraps a live GraphicsLayer that can change every frame
    // (e.g. animating content behind it) — needs continuous re-capture and
    // re-blur. ImageBackdrop/ScreenBackdrop are static: their content only
    // changes on discrete external events (resize, new image), not
    // continuously, so they blur once per event and then idle.
    private val isLiveBackdrop: Boolean
        get() = backdrop is LayerBackdrop

    /**
     * True once a full draw→blur→show cycle has completed for a STATIC
     * backdrop (ImageBackdrop, ScreenBackdrop) and its result is on screen.
     * While true, static backdrops skip launching further RenderScript work —
     * there's nothing new to blur until something external changes (resize,
     * new image, config change) and calls requestUpdate() to reset this.
     */
    var settled = false

    override fun ContentDrawScope.draw() {
        // Step 1: Apply the current color filter to the captured layer.
        content.colorFilter = filter

        // Step 2: Read the current blur configuration.
        //
        // Downsampling reduces the number of pixels that RenderScript needs to
        // process, while radiusPx controls the blur strength.
        val (downsample, radiusPx) = config

        val newSize = IntSize(
            (size.width * downsample).toInt(),
            (size.height * downsample).toInt()
        )

        // Step 3 (the "draw" half of the cycle): capture whatever is currently
        // behind this node into `content`, at the downsampled resolution. This
        // ALWAYS runs, every draw() call, regardless of `settled` — even a
        // static backdrop's raw (unblurred) capture must stay current, because
        // the radiusPx < 1f branch below needs a fresh, correct capture to
        // display directly when blur is turned off.
        content.record(size = newSize) {
            scale(scaleX = downsample, scaleY = downsample, pivot = Offset.Zero) {
                with(backdrop) {
                    drawRegion(this@RsHazeEffectNode.position)
                }
            }
        }

        // Step 4: Draw the backdrop.
        //
        // When blur is disabled, draw the captured layer directly. Since the
        // capture was downsampled, scale it back to the node's original size.
        //
        // When blur is enabled, draw the most recently processed RenderScript
        // result. `drawLayer()` handles scaling the processed layer back to size.
        if (radiusPx < 1f) {
            if (downsample != 0f)
                scale(scaleX = 1f / downsample, scaleY = 1f / downsample, pivot = Offset.Zero) {
                    drawLayer(content)
                }

            // Tear down any existing RenderScript effect/work — nothing should
            // be running while blur is off.
            effect?.release()
            effect = null
            renderer?.cancel()
            settled = false
        } else {
            // Blur enabled: show whatever was produced by the most recently
            // completed blur pass (from a previous draw() call's coroutine) —
            // NOT a fresh blur; that happens asynchronously below.
            effect?.drawLayer()
        }

        // Step 5: Draw the tint as a normal overlay.
        //
        // The tint's alpha controls its opacity; no additional alpha is applied.
        if (tint.isSpecified)
            drawRect(tint)

        // Step 6: Draw the foreground content above the haze effect.
        drawContent()

        // Stop here when blur is disabled. The backdrop, tint, and content have
        // already been drawn, so there is no RenderScript work to schedule.
        if (radiusPx < 1f)
            return

        // shouldRunCycle: do we need to kick off a new blur pass right now?
        // - Live backdrops: always yes — content may have changed since the
        //   last capture, so it must be continuously re-blurred.
        // - Static backdrops: only if not yet settled, i.e. either this is the
        //   very first cycle, or requestUpdate() reset `settled` since the
        //   last completed cycle.
        val shouldRunCycle = isLiveBackdrop || !settled

        // Skip launching new work when either: we don't need a new cycle right
        // now (shouldRunCycle false), or one is already in flight (renderer
        // still active — RenderScript work is deliberately serialized, only
        // one pass at a time).
        //
        // NOTE: this condition is the inverse of shouldRunCycle — double check
        // this reads correctly if touching it again. An earlier version of
        // this had it inverted (`shouldRunCycle && ...`), which caused the
        // function to return before ever launching the first blur pass.
        if (!shouldRunCycle || renderer?.isActive == true)
            return
        // Step 8: Lazily create the RenderScript blur effect.
        //
        // The effect is created only when blur is required and reused across
        // subsequent draw passes. It is released when blur is disabled.
        val effect = effect ?: RsBlurEffect(currentValueOf(LocalContext)).also {
            effect = it
        }

        // Step 9 (the "blur" half of the cycle): process the just-captured
        // content asynchronously off the draw path.
        renderer = coroutineScope.launch {
            val mills = measureTime {
                //TODO - Remove this block; it is throwing error for now.
                try {
                    effect.record(content, config)
                } catch (e: Exception) {
                    Log.d(TAG, "RsBlurEffect: ${e.message}")
                }
            }
            Log.d(TAG, "draw: rendering: $mills")

            // Sync to the next frame before invalidating, rather than firing
            // invalidateDraw() immediately off the RenderScript thread — keeps
            // the redraw aligned with the display's own frame timing.
            withFrameMillis { }

            // Mark settled BEFORE invalidating:
            // - Live backdrop: irrelevant — isLiveBackdrop will force
            //   shouldRunCycle = true again on the very next draw() regardless
            //   of this flag, so the cycle continues exactly as before.
            // - Static backdrop: this is what actually stops the loop — the
            //   draw() call triggered by invalidateDraw() below will see
            //   shouldRunCycle = false and skip launching another coroutine,
            //   so the cycle terminates here until requestUpdate() runs again.
            settled = true

            // Ask for a redraw so the freshly blurred result actually gets
            // shown (draw() will re-run, effect?.drawLayer() above will now
            // display this pass's output).
            invalidateDraw()
        }
    }
}