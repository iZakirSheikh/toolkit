/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 11-01-2024.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.primex.core.blur

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Picture
import android.os.Build
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Log
import android.view.Window
import androidx.annotation.DeprecatedSinceApi
import androidx.annotation.FloatRange
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.NativeCanvas
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.LayoutAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntSize
import androidx.core.graphics.record
import com.primex.core.ExperimentalToolkitApi
import com.primex.core.IsRunningInPreview
import com.primex.core.findActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.system.measureTimeMillis

private const val TAG = "LegacyBackgroundBlur"

/**
 * Represents a modifier element for applying an RS (RenderScript) blur effect.
 *
 * @see RsBlurNode
 */
private data class RsBlurElement(
    var radius: Float,
    var downscale: Float
) : ModifierNodeElement<RsBlurNode>() {

    /**
     * Creates a new instance of [RsBlurNode] with the specified radius and factor.
     *
     * @return The created [RsBlurNode] instance.
     */
    override fun create(): RsBlurNode =
        RsBlurNode(radius, downscale)

    /**
     * Updates the properties of the given [RsBlurNode].
     *
     * @param node The [RsBlurNode] to be updated.
     */
    override fun update(node: RsBlurNode) {
        node.radius = radius
        if (node.downscale != downscale) {
            node.downscale = downscale
        }
        node.onReset()
        //TODO -  Call reset manually maybe.
        Log.d(TAG, "onUpdate: $node")
    }

    /**
     * Provides the inspectable properties for the [RsBlurElement].
     *
     * @receiver The [InspectorInfo] instance to populate with the properties.
     */
    override fun InspectorInfo.inspectableProperties() {
        name = "RsBlur"
        properties["radius"] = radius
        properties["downscale"] = downscale
    }
}

/**
 * Applies a background blur modifier to the composable using RenderScript.
 *
 * **Important:** This modifier is intended for API levels below 33. For API 33 and above, use the
 * [backgroundBlur] modifier for better performance and compatibility.
 *
 * **Key features:**
 * - Applies a blur effect to the composable's content using RenderScript.
 * - Captures the view's content, downscales it, and blurs it using RenderScript.
 * - Supports API levels 21 (Lollipop) to 32 (Android 12).
 * - Works flawlessly in API Level 34
 * - **Works flawlessly with dialogs and popups.**
 *
 * **Known limitations and considerations:**
 * - **Compatibility:** Might not work correctly with hardware bitmaps from Coil or Glide.
 * To avoid issues, disable hardware bitmaps for these libraries.
 * - **Preview:** In Preview; the **modifier** shows **Bluish Scrim Modifier** instead of *Blur*
 * - **Future plans:** An alternative implementation using PixelCopy is planned for better performance
and compatibility with hardware bitmaps.
 *
 * **Usage:**
 *
 * ```kotlin
 * Scaffold(
 *     topBar = {
 *         Row(
 *             modifier = Modifier
 *                 .statusBarsPadding()
 *                 .legacyBackgroundBlur(radius = 25f, downsample = 0.4f)
 *                 // ... other modifiers
 *         ) {
 *             // ... composable content
 *         }
 *     },
 *     // ... other content
 * )
 * ```
 *
 *
 * @param radius The radius of the blur effect in pixels, capped at 25f.
 * @param downscale The downscale factor used to downscale the captured bitmap, capped at 1.0
 *
 * **@see [RsBlurNode] for the underlying implementation.
 * @author Zakir Sheikh
 * @author Bard (enhanced documentation)
 */
//@ChecksSdkIntAtLeast(Build.VERSION_CODES.R)
@DeprecatedSinceApi(Build.VERSION_CODES.R, "This fun works flawlessly above api 30; but use cautiously")
@ExperimentalToolkitApi
fun Modifier.legacyBackgroundBlur(
    @FloatRange(from = 0.0, to = 25.0) radius: Float = 25f,
    @FloatRange(from = 0.0, to = 1.0, fromInclusive = false) downsample: Float = 1.0f,
) = this then when {
    IsRunningInPreview -> ScrimModifier(radius, downsample)
    else -> RsBlurElement(radius, downsample)
}

/**
 * Implementation of a blur modifier using RenderScript for API levels below 33.
 *
 * @param radius The radius of the blur effect in pixels, capped at 25f.
 * @param downscale The downscale factor used to downscale the captured bitmap, capped at 1.0
 */
private class RsBlurNode(
    var radius: Float = 25f,
    var downscale: Float = 1.0f,
) : Modifier.Node(),
    DrawModifierNode,
    CompositionLocalConsumerModifierNode,
    LayoutAwareModifierNode {

    // The job associated with the effect operation.
    private var job: Job? = null
    private var processing = false

    // Note to self -
    // Replaced onPreDraw with withFrameMillis for a more concise and readable implementation.
    // PixelCopy was considered but ultimately rejected due to similar memory usage as the previous approach.
    // TODO: Optimize performance by using a callback mechanism to trigger redraws only when content changes.
    // The current implementation relies on View.onDraw, which leads to unnecessary redraws of all composable on every frame.
    // Explore using onRequestDraw or a similar approach to trigger redraws selectively when content updates occur.
    // This optimization will prevent redundant calls to composable draw methods and improve overall performance.

    override val shouldAutoInvalidate: Boolean get() = false

    // Required global values for blurring the bitmap
    // A RenderScript instance used to create and execute the blur script
    private lateinit var rs: RenderScript
    private lateinit var rsBlurScript: ScriptIntrinsicBlur

    // An Allocation that holds the output/input bitmap of the blur effect,
    // initialized with the changing bounds of the bitmap.
    private lateinit var outAllocation: Allocation
    private lateinit var inAllocation: Allocation

    // The cached bitmap and canvas; used to capture the background content
    // The bitmap used to draw on the screen;
    // this bitmap is not initialized if [bounds] is zero.
    // The associated canvas for drawing on the bitmap
    private lateinit var bitmap: Bitmap
    private val picture = Picture()
    private val canvas by lazy { NativeCanvas() } // not required if view is hardware accelerated.

    // The root window for of this app
    private lateinit var window: Window
    private lateinit var bounds: Rect

    override fun onAttach() {
        // Get the current view from the local composition.
        val ctx = currentValueOf(LocalContext)
        window = ctx.findActivity().window
        // Create a RenderScript instance from the current composition's context.
        rs = RenderScript.create(ctx)
        // Create a ScriptIntrinsicBlur instance using the RenderScript instance and the element type.
        rsBlurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
    }

    override fun onPlaced(coordinates: LayoutCoordinates) {
        // calculate new bound
        val new = coordinates.boundsInWindow()
        // Get coordinates of the view relative to the backdrop.
        val view = currentValueOf(LocalView)
        val (x, y) = view relativeTo window.decorView
        // calculate bounds relative to root
        val relative = Rect(x + new.left, y + new.top, x + new.right, y + new.bottom)
        if (this::bounds.isInitialized && relative == bounds)
            return
        bounds = relative
        // since bounds have changed this causes reset.
        onReset()
    }

    // cleanup.
    override fun onDetach() {
        job?.cancel()
        rs.destroy()
        bitmap.recycle()
        rsBlurScript.destroy()
        if (this::outAllocation.isInitialized)
            outAllocation.destroy()
        if (this::inAllocation.isInitialized)
            inAllocation.destroy()
    }

    /**
     * Captures the current view content.
     */
    fun capture() {
        val width = (bounds.width * downscale).roundToInt()
        val height = (bounds.height * downscale).roundToInt()
        picture.record(width, height) {
            // First, make sure that the subsequent drawing steps are
            // done in the correct coordinates
            if (downscale < 1f)
                scale(downscale, downscale)
            // Translate the canvas to the origin of the view's bounds
            translate(-bounds.left, -bounds.top)
            // Clip the canvas to the view's bounds
            clipRect(bounds.left, bounds.top, bounds.right, bounds.bottom)
            // Draw all the contents of the view to this bitmap
            // This code seems to iterate over all the descendants of the
            // view's root view and draw them on the canvas
            window.decorView.draw(this)
            Log.d(TAG, "captured: ")
        }
        picture.endRecording()
        when {
            // recycle old bitmap and create new one
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && picture.requiresHardwareAcceleration() -> {
                bitmap.recycle()
                bitmap = Bitmap.createBitmap(picture).copy(Bitmap.Config.ARGB_8888, true)
            }
            // use created bitmap to save memory
            else -> {
                canvas.drawColor(Color.TRANSPARENT)
                canvas.drawPicture(picture)
                canvas.setBitmap(bitmap)
                bitmap
            }
        }
    }

    /**
     * Applies blur to the captured bitmap.
     */
    fun blur() {
        // Set the radius and the input of the blur script
        inAllocation.copyFrom(bitmap)
        // If the bitmap size has changed, create a new output allocation
        rsBlurScript.setRadius(radius)
        rsBlurScript.setInput(inAllocation)
        // Apply the blur script to the output allocation
        // Note: Do not use input Allocation in forEach. it will cause visual artifacts on blurred Bitmap
        rsBlurScript.forEach(outAllocation)
        // Copy the output allocation to the output bitmap
        outAllocation.copyTo(this@RsBlurNode.bitmap)
    }

    override fun onReset() {
        job?.cancel()
        // If bounds are empty, no need to continue.
        if (bounds.isEmpty)
            return
        Log.d(TAG, "reset: $bounds $downscale")
        // Recreate the bitmap with adjusted dimensions based on the current configuration.
        bitmap = Bitmap.createBitmap(
            (bounds.width * downscale).roundToInt(),
            (bounds.height * downscale).roundToInt(),
            Bitmap.Config.ARGB_8888
        )
        // Create Allocation objects from the recreated bitmap for the blur effect.
        inAllocation =
            Allocation.createFromBitmap(
                rs,
                bitmap,
                Allocation.MipmapControl.MIPMAP_NONE,
                Allocation.USAGE_SCRIPT
            )
        outAllocation = Allocation.createTyped(rs, inAllocation.type)
        // launch the job
        val _onFrame: (frameTimeMillis: Long) -> Unit = {}
        job = coroutineScope.launch {
            while (true) {
                // wait for next frame and then proceed
                withFrameMillis(_onFrame)
                processing = true
                // Why do we need this?
                // Because when copying bitmap while device is performing
                // animation for overscroll stretching; that is done of RenderNode; we here
                // do that to catch the exception;
                // it causes the blur for that amount of time to go off.
                // This can be avoided if the user disables that animation.
                val captureMills = try {
                    measureTimeMillis(::capture)
                } catch (ignored: IllegalArgumentException) {
                    Log.i(TAG, "capture: ", ignored)
                    0
                }
                val blurMills = measureTimeMillis(::blur)
                Log.d(TAG, "capture: $captureMills blur: $blurMills")
                processing = false
                invalidateDraw()
            }
        }
    }

    override fun ContentDrawScope.draw() {
        // Skip drawing any content for this component while blurring,
        // as we only need the content behind it.
        if (processing) return
        // Draw the blurred image on the canvas.
        if (this@RsBlurNode::bitmap.isInitialized)
            drawImage(
                bitmap.asImageBitmap(),
                dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt())
            )
        // Log the drawing of the blurred image for debugging purposes.
        Log.d(TAG, "draw: bitmap")
        // Draw any additional content on top of the blurred image.
        drawContent()
    }
}