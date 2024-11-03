/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 03-11-2024.
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
import android.graphics.HardwareRenderer
import android.graphics.Picture
import android.graphics.PixelFormat
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.hardware.HardwareBuffer
import android.media.ImageReader
import android.os.Build
import android.util.Log
import android.view.Window
import androidx.annotation.FloatRange
import androidx.annotation.RequiresApi
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.toAndroidTileMode
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.core.graphics.record
import com.primex.core.ExperimentalToolkitApi
import com.primex.core.IsRunningInPreview
import com.primex.core.findActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.system.measureTimeMillis

private const val TAG = "NewBackgroundBlur"

/**
 * Implementation of a blur modifier using RenderEffect.
 *
 * @property radiusX Horizontal blur radius in Dp.
 * @property radiusY Vertical blur radius in Dp.
 * @property downscale Factor to scale down the captured bitmap.
 * @property edgeTreatment How the edge of the blur effect should be treated (TileMode).
 */
@RequiresApi(Build.VERSION_CODES.S)
private class ReBlurNode(
    var radiusX: Dp,
    var radiusY: Dp,
    var downscale: Float,
    var edgeTreatment: TileMode,
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

    // The cached bitmap and canvas; used to capture the background content
    // The bitmap used to draw on the screen;
    // this bitmap is not initialized if [bounds] is zero.
    // The associated canvas for drawing on the bitmap
    private lateinit var bitmap: Bitmap
    private val picture = Picture()

    // The root window for of this app
    private lateinit var window: Window
    private lateinit var bounds: Rect

    //
    private val renderNode = RenderNode("BlurEffect")
    private val hardwareRenderer = HardwareRenderer()
    private lateinit var imageReader: ImageReader

    override fun onAttach() {
        // Get the current view from the local composition.
        val ctx = currentValueOf(LocalContext)
        window = ctx.findActivity().window
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

    // Clean up resources when detached
    override fun onDetach() {
        job?.cancel()
        if (this::bitmap.isInitialized) bitmap.recycle()
        if (this::imageReader.isInitialized) imageReader.close()
        renderNode.discardDisplayList()
        hardwareRenderer.destroy()
    }

    /**
     * Captures the current view at [bounds] on [bitmap].
     */
    private fun capture() {
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
        bitmap.recycle()
        bitmap = Bitmap.createBitmap(picture)
    }

    /**
     *  Blurs the current [bitmap]
     */
    private fun blur() {
        val renderCanvas = renderNode.beginRecording()
        renderCanvas.drawBitmap(bitmap, 0f, 0f, null)
        renderNode.endRecording()
        hardwareRenderer.createRenderRequest()
            .setWaitForPresent(true)
            .syncAndDraw()
        val image = imageReader.acquireNextImage() ?: throw RuntimeException("No Image")
        val hardwareBuffer = image.hardwareBuffer ?: throw RuntimeException("No HardwareBuffer")
        bitmap.recycle()
        bitmap = Bitmap.wrapHardwareBuffer(hardwareBuffer, null)
            ?: throw RuntimeException("Create Bitmap Failed")
        image.close()
    }


    // Resets the blur node, cancelling any ongoing job and reinitializing resources.
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
        imageReader = ImageReader.newInstance(
            bitmap.width, bitmap.height,
            PixelFormat.RGBA_8888, 1,
            HardwareBuffer.USAGE_GPU_SAMPLED_IMAGE or HardwareBuffer.USAGE_GPU_COLOR_OUTPUT
        )
        val density = currentValueOf(LocalDensity)
        hardwareRenderer.setSurface(imageReader.surface)
        hardwareRenderer.setContentRoot(renderNode)
        renderNode.setPosition(0, 0, imageReader.width, imageReader.height)
        val blurRenderEffect = RenderEffect.createBlurEffect(
            with(density) { radiusX.toPx() }, with(density) { radiusY.toPx() },
            edgeTreatment.toAndroidTileMode()
        )
        renderNode.setRenderEffect(blurRenderEffect)
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
        if (this@ReBlurNode::bitmap.isInitialized)
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

/**
 * Represents a modifier element for applying an RS (RenderScript) blur effect.
 *
 * @see RsBlurNode
 */
@RequiresApi(Build.VERSION_CODES.S)
private data class ReBlurElement(
    var radiusX: Dp,
    var radiusY: Dp,
    var downscale: Float,
    var edgeTreatment: TileMode,
) : ModifierNodeElement<ReBlurNode>() {

    /**
     * Creates a new instance of [RsBlurNode] with the specified radius and factor.
     *
     * @return The created [RsBlurNode] instance.
     */
    override fun create(): ReBlurNode =
        ReBlurNode(radiusX, radiusY, downscale, edgeTreatment)

    /**
     * Updates the properties of the given [RsBlurNode].
     *
     * @param node The [RsBlurNode] to be updated.
     */
    override fun update(node: ReBlurNode) {
        node.radiusX = radiusX
        node.radiusY = radiusY
        node.edgeTreatment = edgeTreatment
        node.downscale = downscale
        //TODO -  Call reset manually maybe.
        node.onReset()
        Log.d(TAG, "onUpdate: $node")
    }

    /**
     * Provides the inspectable properties for the [RsBlurElement].
     *
     * @receiver The [InspectorInfo] instance to populate with the properties.
     */
    override fun InspectorInfo.inspectableProperties() {
        name = "RsBlur"
        properties["radiusX"] = radiusX
        properties["radiusY"] = radiusY
        properties["downscale"] = downscale
        properties["edgeTreatment"] = edgeTreatment
    }
}

/**
 * Applies a new background blur effect to a Modifier.
 *
 * @param radius The radius of the blur effect.
 * @param edgeTreatment How the edges of the blur should be treated.
 * @param downsample The factor by which the image should be downsampled.
 * @return The modified Modifier with the blur effect applied.
 */
//@ChecksSdkIntAtLeast(Build.VERSION_CODES.R)
@RequiresApi(Build.VERSION_CODES.S)
@ExperimentalToolkitApi
fun Modifier.newBackgroundBlur(
    radius: Dp,
    edgeTreatment: TileMode = TileMode.Clamp,
    @FloatRange(from = 0.0, to = 1.0, fromInclusive = false) downsample: Float = 1.0f,
) = this then newBackgroundBlur(radius, radius, edgeTreatment, downsample)

/**
 * Applies a new background blur effect to a Modifier with separate horizontal and vertical radii.
 *
 * @param radiusX The horizontal radius of the blur effect.
 * @param radiusY The vertical radius of the blur effect.
 * @param edgeTreatment How the edges of the blur should be treated.
 * @param downsample The factor by which the image should be downsampled.
 * @return The modified Modifier with the blur effect applied.
 */
@RequiresApi(Build.VERSION_CODES.S)
@ExperimentalToolkitApi
fun Modifier.newBackgroundBlur(
    radiusX: Dp,
    radiusY: Dp,
    edgeTreatment: TileMode = TileMode.Clamp,
    @FloatRange(from = 0.0, to = 1.0, fromInclusive = false) downsample: Float = 1.0f,
) = this then when {
    // Use ScrimModifier during preview for better visual fidelity
    IsRunningInPreview -> ScrimModifier(radiusX.value, downsample)
    // Apply ReBlurElement for actual running environments
    else -> ReBlurElement(radiusX, radiusY, downsample, edgeTreatment)
}