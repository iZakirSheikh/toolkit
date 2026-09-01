package com.zs.compose.foundation.backdrop.haze

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.renderscript.Type
import android.util.Log
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import com.zs.compose.foundation.backdrop.haze.RsBlurEffect.Companion.MAX_RS_RADIUS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.sqrt
import androidx.compose.ui.geometry.Offset as ScaleRadius

private const val MAX_BLUR_RADIUS = 25f
private const val SKIA_ALIGNMENT_MULTIPLIER = 0.85f
private const val TAG = "RenderScriptContext"

/**
 * A stateful processor that applies hardware-accelerated Gaussian blur to Jetpack Compose
 * [GraphicsLayer]s using Android's legacy RenderScript API.
 *
 * To bypass RenderScript's hard limit of a 25px blur radius, this class dynamically
 * downscales the input resolution for larger radii, achieving visual parity with
 * Android 12's native Skia `RenderEffect`.
 *
 * @property rs The RenderScript context environment.
 * @property rsBlurScript The highly optimized intrinsic script for Gaussian blur.
 * @property inAllocation The input memory buffer (USAGE_IO_INPUT) tied to a hardware Surface.
 * @property outAllocation The output memory buffer mapped directly to our reusable [buffer].
 * @property buffer The cached Compose [ImageBitmap] that holds the final blurred pixels.
 * @property lock A mutex ensuring thread safety during teardown and reallocation phases.
 * @property channel A synchronization primitive that bridges the callback-based Android Surface
 *                   lifecycle with Kotlin Coroutines, pausing rendering until GPU writes complete.
 * @property isReleased Tracks the lifecycle state to prevent operations on destroyed RS contexts.
 */
class RsBlurEffect(context: Context) {
    private val rs = RenderScript.create(context.applicationContext)
    private val rsBlurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))

    private lateinit var inAllocation: Allocation
    private lateinit var outAllocation: Allocation
    private lateinit var buffer: ImageBitmap

    private val lock = ReentrantLock()
    private val channel = Channel<Unit>(Channel.CONFLATED)

    @Volatile
    private var isReleased = false

    /**
     * Calculates the necessary scaling factors and re-allocates RenderScript memory buffers
     * if the required dimensions have changed.
     *
     * @param size The physical pixel dimensions of the UI layer being blurred.
     * @param radius The requested blur radius in pixels.
     * @return The actual radius to pass to RenderScript (guaranteed to be <= 25f).
     * @throws IllegalStateException if called after [release] has been invoked.
     */
    private fun prepare(size: IntSize, radius: Float): ScaleRadius {
        check(!isReleased) {
            "Cannot prepare blur: the blur processor has already been released."
        }

        // Align perceived RenderScript blur strength with Compose/Skia.
        val tunedRadiusPx = radius * SKIA_ALIGNMENT_MULTIPLIER

        var scale = 1.0f
        var radiusPx = 25f

        if (tunedRadiusPx <= 0f) {
            scale = 1f
            radiusPx = 0f
        } else if (tunedRadiusPx <= 25f) {
            // THE ANDROID 12 ZONE: Full 1:1 resolution, true Gaussian blur
            scale = 1f
            radiusPx = tunedRadiusPx
        } else {
            // THE SIMULATION ZONE: Smoothly downscale only when forced to.
            // This maintains the illusion of a massive blur by shrinking the image,
            // blurring at the max 25px, and letting bilinear upscaling handle the rest.
            scale = MAX_BLUR_RADIUS / tunedRadiusPx
            radiusPx = MAX_BLUR_RADIUS
        }

        val newSize = IntSize(
            width = (size.width * scale).toInt().coerceAtLeast(1),
            height = (size.height * scale).toInt().coerceAtLeast(1)
        )

        // Only destroy and recreate expensive memory allocations if the bounding box changes.
        val needsRecreate =
            !::buffer.isInitialized ||
                    buffer.width != newSize.width ||
                    buffer.height != newSize.height
        Log.d(TAG, "prepare: recreate: $needsRecreate")

        if (needsRecreate) {
            // Lock the ENTIRE recreation process to prevent race conditions where
            // a drawing thread tries to use an allocation mid-destruction.
            lock.withLock {
                // Double-check release state inside the lock.
                // Return from function entirely to avoid touching dead RS contexts.
                if (isReleased) return ScaleRadius(scale, radiusPx)

                if (::buffer.isInitialized) {
                    inAllocation.destroy()
                    outAllocation.destroy()
                    if (!buffer.asAndroidBitmap().isRecycled) {
                        buffer.asAndroidBitmap().recycle()
                    }
                    // Note: Channel is NOT cancelled here because it is reused for the
                    // lifetime of this class instance, not just the allocation lifecycle.
                }

                val (width, height) = newSize
                val type = Type.Builder(rs, Element.U8_4(rs)).setX(width).setY(height).create()
                val flags = Allocation.USAGE_SCRIPT or Allocation.USAGE_IO_INPUT

                inAllocation = Allocation.createTyped(rs, type, flags).apply {
                    setOnBufferAvailableListener { allocation ->
                        if (!isReleased) {
                            // Signal that the GPU has finished writing the surface pixels.
                            allocation.ioReceive()
                            channel.trySendBlocking(Unit)
                        }
                    }
                }

                // Create the wrapper once. asAndroidBitmap() will be used for RS writes,
                // while the Compose ImageBitmap will be used for drawing without reallocation.
                buffer = ImageBitmap(width = width, height = height)
                outAllocation = Allocation.createFromBitmap(rs, buffer.asAndroidBitmap())
                rsBlurScript.setInput(inAllocation)
            }
        }

        return ScaleRadius(scale, radiusPx)
    }

    /**
     * Safely destroys all RenderScript contexts, script intrinsics, and memory allocations.
     * Once called, this instance cannot be used again.
     */
    fun release() {
        lock.withLock {
            if (isReleased) return@withLock
            isReleased = true

            rsBlurScript.destroy()
            inAllocation.destroy()
            outAllocation.destroy()
            channel.cancel()

            if (::buffer.isInitialized) {
                val bitmap = buffer.asAndroidBitmap()
                if (!bitmap.isRecycled) bitmap.recycle()
            }
            rs.destroy()
        }
    }

    /**
     * Draws the fully processed, blurred image onto the Compose Canvas.
     *
     * If the processor was downscaled (radius > 25px), this step stretches the small
     * buffer back to the layout's original dimensions, applying bilinear interpolation
     * which naturally smooths the upscaling process.
     */
    context(scope: DrawScope)
    fun drawLayer() {
        if (isReleased || !::buffer.isInitialized || buffer.asAndroidBitmap().isRecycled) return
        val size = scope.size

        scope.drawImage(
            image = buffer,
            srcOffset = IntOffset.Zero,
            srcSize = IntSize(buffer.width, buffer.height), // The physical memory size
            dstOffset = IntOffset.Zero,
            dstSize = IntSize(size.width.toInt(), size.height.toInt()) // The visual UI size
        )
    }

    /**
     * Captures a Compose [GraphicsLayer], renders it into native RenderScript memory,
     * applies the Gaussian blur off the main thread, and copies it back for rendering.
     *
     * @param layer The pre-recorded Compose UI layer to be blurred.
     * @param radius The requested blur radius.
     */
    context(scope: DrawScope)
    suspend fun renderLayer(layer: GraphicsLayer, radius: Float) {
        val size = layer.size
        val (scale, newRadiusPx) = prepare(size = size, radius)

        // Calculate the exact scaling ratio applied during the prepare() phase.
        Log.d(TAG, "renderLayer: size: $size scale: $scale, radius: $radius, newRadius: $newRadiusPx")

        // ----------------------------------------------------------------------
        // PHASE 1: HARDWARE CAPTURE & SCALING
        // We draw the Compose UI layer directly into the RenderScript input memory buffer.
        // ----------------------------------------------------------------------
        val surface = inAllocation.surface

        // Request a hardware-accelerated canvas. This is significantly faster than
        // lockCanvas(null) but requires strict lock/unlock pairings.
        val canvas = surface.lockHardwareCanvas()
        try {
            // 1. Wipe the buffer clean to prevent ghosting from previous frames
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

            // 2. Shrink the canvas coordinate system. This is the core of the >25px blur
            // workaround. The 1:1 drawing instructions below will be rasterized directly
            // into a physically smaller memory buffer, saving massive GPU/CPU overhead.
            canvas.scale(scale, scale)

            // 3. Bridge Compose's GraphicsLayer instructions onto the native Android canvas
            with(scope) {
                draw(
                    density = Density(density, fontScale),
                    layoutDirection = layoutDirection,
                    canvas = Canvas(canvas), // Wrap native Canvas into Compose Canvas
                    size = size.toSize(),
                ) {
                    drawLayer(layer)
                }
            }
        } finally {
            // CRITICAL: Always unlock the canvas, even if an exception occurs during drawing.
            // Failing to do this causes a permanent system deadlock on the Surface.
            surface.unlockCanvasAndPost(canvas)
        }

        // ----------------------------------------------------------------------
        // PHASE 2: BUFFER SYNCHRONIZATION
        // ----------------------------------------------------------------------
        // Suspend the coroutine until the GPU has actually finished writing the pixels
        // into the Allocation memory. (Triggered by setOnBufferAvailableListener in prepare).
        channel.receive()

        // ----------------------------------------------------------------------
        // PHASE 3: RENDERSCRIPT PROCESSING
        // ----------------------------------------------------------------------
        if (newRadiusPx > 0) {
            // Shift heavy mathematical processing off the main thread to prevent UI lag
            withContext(Dispatchers.Default) {
                // Thread safety: Prevent execution if the UI node was detached while we
                // were waiting in the suspension/receive step above.
                lock.withLock {
                    require(newRadiusPx in 1f..MAX_BLUR_RADIUS) {
                        "blurRadius needs to be >= 1 and <= 25"
                    }

                    // Double-check destruction state to prevent Fatal SIGSEGV crashes
                    if (isReleased) return@withLock

                    // 1. Configure the Gaussian blur bell curve size
                    rsBlurScript.setRadius(newRadiusPx.coerceAtMost(MAX_BLUR_RADIUS))

                    // 2. Execute the blur script on the input buffer, writing to the output buffer
                    rsBlurScript.forEach(outAllocation)

                    // 3. Copy the finished, blurred pixels out of RenderScript memory
                    // and into our standard Android Bitmap, ready for Compose to draw.
                    if (!isReleased) {
                        outAllocation.copyTo(buffer.asAndroidBitmap())
                    }
                }
            }
        }
    }

    companion object {
        private const val MAX_RS_RADIUS = 25f

        // K controls how aggressively downsampling ramps in. Larger K = downsampling
        // stays closer to 1.0 for longer (delays quality loss), smaller K = ramps in
        // sooner. Tune empirically — try values in the 20–40 range.
        private const val K = 30f

        private const val MIN_SCALE = 0.05f

        /**
         * Computes a downsample scale and adjusted blur radius for the given input.
         *
         * The scale curve is smooth and continuous across all radii:
         * - Approaches 1.0 as radius → 0
         * - Gradually decreases as radius grows, avoiding hard thresholds
         * - Ensures a minimum scale and caps the effective radius at [MAX_RS_RADIUS]
         *
         * @param radius Desired blur radius in pixels.
         * @return [ScaleRadius] containing the resolved scale and effective radius.
         */
        fun calculateAdaptiveScaleAndRadius(radius: Float): ScaleRadius {
            // 1 / sqrt(1 + (radius/K)^2) — smoothly transitions from ~1.0 at small
            // radius to ~K/radius at large radius, with a gentle knee instead of a
            // hard corner (this shape is the same family used for smooth mip-level
            // selection in graphics pipelines).
            val ratio = radius / K
            val scale =  (1f / sqrt(1f + ratio * ratio)).coerceAtLeast(MIN_SCALE)
            val adjustedRadius = (radius * scale).coerceAtMost(MAX_RS_RADIUS)
            return ScaleRadius(scale, adjustedRadius)
        }
    }
}