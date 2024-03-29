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

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.view.View
import android.view.ViewTreeObserver.OnPreDrawListener
import androidx.annotation.FloatRange
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.platform.inspectable
import androidx.compose.ui.unit.IntSize
import androidx.core.graphics.withSave
import com.primex.core.ExperimentalToolkitApi
import com.primex.core.IsRunningInPreview
import com.primex.core.debug.loggers.logD
import com.primex.core.findActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.system.measureTimeMillis

private const val TAG = "LegacyBackgroundBlur"

/**
 * Calculates the offset of this view relative to another view.
 *
 * @receiver the view whose offset is being calculated
 * @param other the other view to calculate offset relative to
 * @return an [Offset] representing the offset of this view relative to the other view
 */
internal infix fun View.relativeTo(other: View): Offset {
    // Temporary array to store coordinates
    val temp = IntArray(2)

    // Get absolute coordinates of this view on the screen
    getLocationOnScreen(temp)
    val (x1, y1) = temp

    // Get absolute coordinates of the other view on the screen
    other.getLocationOnScreen(temp)
    val (x2, y2) = temp

    // Calculate relative offset by subtracting coordinates
    val xOffset = (x1 - x2).toFloat()
    val yOffset = (y1 - y2).toFloat()

    // Create and return the Offset object
    return Offset(xOffset, yOffset)
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
    val debugLogger: Boolean,
) : Modifier.Node(),
    DrawModifierNode,
    CompositionLocalConsumerModifierNode,
    LayoutAwareModifierNode,
    OnPreDrawListener {
    override val shouldAutoInvalidate: Boolean get() = false

    // Required global values for blurring the bitmap
    // A RenderScript instance used to create and execute the blur script
    private lateinit var rs: RenderScript
    private lateinit var rsBlurScript: ScriptIntrinsicBlur

    // An Allocation that holds the output/input bitmap of the blur effect,
    // initialized with the changing bounds of the bitmap.
    private lateinit var outAllocation: Allocation
    private lateinit var inAllocation: Allocation

    // The bitmap used to draw on the screen;
    // this bitmap is not initialized if [bounds] is zero.
    private lateinit var bitmap: Bitmap

    // The associated canvas for drawing on the bitmap
    private var canvas = NativeCanvas()

    // The view associated with this node
    private lateinit var view: View

    // The root window for of this app
    private val window get() = view.context.findActivity().window

    // The Bounds of this composable wrt to Backdrop.
    private lateinit var bounds: Rect

    /**
     * Flag indicating weather the dependencies of this node are initialized.
     */
    val isReady get() = this@RsBlurNode::bounds.isInitialized && !bounds.isEmpty

    // The job to blur the bitmap
    private var job: Job? = null

    /**
     * Initializes the RenderScript generic dependencies.
     */
    @SuppressLint("SuspiciousCompositionLocalModifierRead")
    override fun onAttach() {
        // Get the current view from the local composition.
        // TODO: Maybe use the observable way to capture this.
        view = currentValueOf(LocalView)
        // Create a RenderScript instance from the current composition's context.
        rs = RenderScript.create(view.context)

        // Create a ScriptIntrinsicBlur instance using the RenderScript instance and the element type.
        rsBlurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
    }

    /**
     * Resets the state of the blur effect, preparing for a new blur application.
     *
     * This function performs the following actions:
     *
     * 1. Cancels any ongoing blur processing tasks.
     * 2. Detaches the pre-draw listener from the backdrop view to avoid unnecessary calls.
     * 4. Recreates the bitmap used for the blur effect, adjusting its dimensions based on the current configuration.
     * 5. Creates necessary allocation objects from the bitmap for rendering the blur effect using RenderScript.
     * 6. Reattaches the pre-draw listener to the backdrop view, ready for the next blur process.
     *
     * **Important Notes:**
     *
     * - This function does not directly apply the blur effect. It prepares the necessary resources for a subsequent blur application.
     * - The function assumes that the `factor` parameter, which controls the blur intensity, has already been set to the desired value before calling `reset()`.
     * - Hardware bitmap creation is currently not implemented.
     *
     * @see RenderScript
     */
    fun reset() {
        view.viewTreeObserver.removeOnPreDrawListener(this)
        job?.cancel()
        // If bounds are empty, no need to continue.
        if (bounds.isEmpty)
            return
        logD(tag = TAG, message = "reset: $bounds $downscale", isDebug = debugLogger)

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
        // Reattach the pre-draw listener for the next blur process.
        view.viewTreeObserver.addOnPreDrawListener(this)
    }


    override fun onPlaced(coordinates: LayoutCoordinates) {
        // calculate new bound
        val modifierRectBounds = coordinates.boundsInWindow()
        // Get coordinates of the view relative to the backdrop offset.
        val (x: Float, y: Float) = view relativeTo window.decorView
        // calculate bounds relative to root
        val relative = Rect(
            x + modifierRectBounds.left,
            y + modifierRectBounds.top,
            x + modifierRectBounds.right,
            y + modifierRectBounds.bottom
        )
        if (this::bounds.isInitialized && relative == bounds)
            return
        bounds = relative
        // since bounds have changed this causes reset.
        reset()
    }

    /**
     * Captures the view on [bitmap]'s [canvas] at [bounds] using [downscale] as scaling, only if less than 1.
     */
    // TODO: Create another method that captures the view with PixelCopy because this doesnt work
    //  when coil is used with allowHardware = true
    private fun capture() {
        canvas.setBitmap(bitmap)
//        bitmap.eraseColor(Color.TRANSPARENT)
        canvas.withSave {
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
            logD(tag = TAG, message = "captured: ", isDebug = debugLogger)
        }
    }

    /**
     * [Blurs] the bitmap using [ScriptIntrinsicBlur] and copies it into [bitmap]; which can be painted
     * on composae canvus using [paint]
     */
    private fun blur() {
        // Set the radius and the input of the blur script
        inAllocation.copyFrom(bitmap)
        // If the bitmap size has changed, create a new output allocation
        rsBlurScript.setRadius(radius)
        rsBlurScript.setInput(inAllocation)
        // Apply the blur script to the output allocation
        // Note: Do not use input Allocation in forEach. it will cause visual artifacts on blurred Bitmap
        rsBlurScript.forEach(outAllocation);
        // Copy the output allocation to the output bitmap
        outAllocation.copyTo(this@RsBlurNode.bitmap)
        logD(tag = TAG, message = "bulred: ", isDebug = debugLogger)
    }

    private val runnable: suspend CoroutineScope.() -> Unit = launch@{
        // Skip if the dependencies are not ready.
        if (!isReady) return@launch
        // Remove the listener temporarily to avoid recursive calls.
        view.viewTreeObserver.removeOnPreDrawListener(this@RsBlurNode)
        // Measure the time taken for capturing the content.
        val captureMills = measureTimeMillis(::capture)
        // Measure the time taken for applying the blur effect.
        val blurMills = measureTimeMillis(::blur)
        logD(tag = TAG, message = "onPreDraw: CaptureMills: $captureMills BlurMills: $blurMills", isDebug = debugLogger)
        // source - https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/foundation/foundation/src/androidMain/kotlin/androidx/compose/foundation/Magnifier.android.kt
        withFrameMillis { }
        // Add the listener back for the next draw pass.
        view.viewTreeObserver.addOnPreDrawListener(this@RsBlurNode)
    }

    /**
     * Callback triggered before the view is drawn.
     * This is used to capture and blur the content efficiently.
     *
     * @return `true` to proceed with the drawing of this pass, `false` otherwise.
     */
    // TODO - Find something that is good for compose
    override fun onPreDraw(): Boolean {
        logD(tag = TAG, message = "onPreDraw: ", isDebug = debugLogger)
        // Just return if the blur algorithm is already running.
        if (job?.isActive == true) return false
        job?.cancel()
        job = coroutineScope.launch(block = runnable)
        // Indicate whether to proceed with the drawing of this pass.
        return true
    }

    override fun ContentDrawScope.draw() {
        // Skip drawing any content for this component while blurring,
        // as we only need the content behind it.
        if (!isReady /*|| job?.isActive == true*/) return
        // Draw the blurred image on the canvas.
        drawImage(
            bitmap.asImageBitmap(),
            dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt())
        )
        // Log the drawing of the blurred image for debugging purposes.
        logD(tag = TAG, message = "draw: bitmap", isDebug = debugLogger)
        // Draw any additional content on top of the blurred image.
        drawContent()
    }

    /**
     * Detaches the OnPreDrawListener and releases RenderScript resources when the node is detached.
     */
    override fun onDetach() {
        job?.cancel()
        view.viewTreeObserver.removeOnPreDrawListener(this)
        rs.destroy()
        rsBlurScript.destroy()
        if (this::outAllocation.isInitialized)
            outAllocation.destroy()
        if (this::inAllocation.isInitialized)
            inAllocation.destroy()
    }
}

/**
 * Represents a modifier element for applying an RS (RenderScript) blur effect.
 *
 * @see RsBlurNode
 */
private data class RsBlurElement(
    var radius: Float,
    var downscale: Float,
    val debugLogger: Boolean
) : ModifierNodeElement<RsBlurNode>() {

    /**
     * Creates a new instance of [RsBlurNode] with the specified radius and factor.
     *
     * @return The created [RsBlurNode] instance.
     */
    override fun create(): RsBlurNode =
        RsBlurNode(radius, downscale, debugLogger)

    /**
     * Updates the properties of the given [RsBlurNode].
     *
     * @param node The [RsBlurNode] to be updated.
     */
    override fun update(node: RsBlurNode) {
        node.radius = radius
        if (node.downscale != downscale) {
            node.downscale = downscale
            node.reset()
        }
        //TODO -  Call reset manually maybe.
        logD(tag = TAG, message = "onUpdate: $node", isDebug = debugLogger)
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
@RequiresApi(Build.VERSION_CODES.LOLLIPOP, Build.VERSION_CODES.R)
@ExperimentalToolkitApi
//@Deprecated("Use backgroundBlur modifier for API 33 and above")
fun Modifier.legacyBackgroundBlur(
    @FloatRange(from = 0.0, to = 25.0) radius: Float = 25f,
    @FloatRange(from = 0.0, to = 1.0, fromInclusive = false) downsample: Float = 1.0f,
    debugLogger: Boolean = true
) = this then if (IsRunningInPreview) ScrimModifier(radius, downsample) else RsBlurElement(
    radius,
    downsample,
    debugLogger
)

private fun Modifier.ScrimModifier(radius: Float, downsample: Float) = inspectable(
    inspectorInfo = debugInspectorInfo {
        name = "legacyBackgroundBlur"
        properties["radius"] = radius
        properties["downsample"] = downsample
    },
    factory = {
        background(androidx.compose.ui.graphics.Color.Blue.copy(0.4f))
    }
)
