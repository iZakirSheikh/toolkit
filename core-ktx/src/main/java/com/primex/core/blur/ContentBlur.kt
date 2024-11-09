/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 07-11-2024.
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
import android.graphics.PorterDuff
import android.os.Build
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Log
import androidx.annotation.DeprecatedSinceApi
import androidx.annotation.FloatRange
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalGraphicsContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toIntSize
import com.primex.core.ExperimentalToolkitApi
import com.primex.core.IsRunningInPreview
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.system.measureTimeMillis
import androidx.compose.ui.node.invalidateDraw as redraw

private const val TAG = "ContentBlur"

/**
 * Applies a blur effect to the content of the composable using RenderScript.
 * This `modifier` is useful for devices running below `Android 12`. On versions `12` and above, you can use
 * the official [androidx.compose.ui.draw.blur].
 *
 * @param radius The radius of the blur effect. The default value is `25f`. Acceptable range is from `0.0` to `25.0`.
 * @param downscale The factor by which the captured bitmap is downscaled before applying the blur effect. The maximum value is `1.0`.
 * Lower values can improve performance. The default value is `1.0f`.
 * @param blurRedundancy This parameter controls the number of extra frames to blur after the bitmaps are identical.
 * This is an experimental parameter. I noticed that ripples are not updated correctly, which is why I added this parameter.
 * This might be removed in the future. The default value is `25`.
 * - If the value is `0`, it means blur once and do not add any extra frames. This is not live and
 * - If the value is `-1`, it means blur endlessly. This is live; but this will invalidate the composable after every frame. Use this with caution.
 * - If the value is greater than `0`, it specifies the number of extra frames to blur after the bitmaps are identical.
 *
 * @return A [Modifier] with the blur effect applied.
 * @see androidx.compose.ui.draw.blur
 *
 * ### Usage Example:
 *
 * ```kotlin
 * Modifier.legacyContentBlur(
 *     radius = 25f,
 *     downscale = 0.5f,
 *     blurRedundancy = 15
 * )
 * ```
 */
@DeprecatedSinceApi(
    Build.VERSION_CODES.R,
    "This fun is deprecated from R onwards. please use official Modifier.blur after R"
)
@ExperimentalToolkitApi
fun Modifier.legacyContentBlur(
    @FloatRange(from = 0.0, to = 25.0) radius: Float,
    @FloatRange(from = 0.0, to = 1.0) downscale: Float = 1.0f,
    blurRedundancy: Int = 25
) = this then when {
    // Use ScrimModifier during preview for better visual fidelity
    IsRunningInPreview -> ScrimModifier(radius, downscale)
    // Apply ReBlurElement for actual running environments
    else -> ContentBlurElement(radius, downscale, blurRedundancy)
}


private class ContentBlurElement(
    val radius: Float,
    val downscale: Float,
    val blurRedundancy: Int
) : ModifierNodeElement<ContentBlurNode>() {

    override fun create(): ContentBlurNode = ContentBlurNode(radius, downscale, blurRedundancy)

    override fun update(node: ContentBlurNode) {
        node.radius = radius
        node.downscale = downscale
        node.blurRedundancy = blurRedundancy
        node.reset(true)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ContentBlurElement) return false

        if (radius != other.radius) return false
        if (downscale != other.downscale) return false
        if (blurRedundancy != other.blurRedundancy) return false

        return true
    }

    override fun hashCode(): Int {
        var result = radius.hashCode()
        result = 31 * result + downscale.hashCode()
        result = 31 * result + blurRedundancy
        return result
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "legacyContentBlur"
        properties["radius"] = radius
        properties["downscale"] = downscale
        properties["blurRedundancy"] = blurRedundancy
    }

    override fun toString(): String {
        return "ContentBlurElement(radius=$radius, downscale=$downscale, blurRedundancy=$blurRedundancy)"
    }
}

// The uninitialized int.
private const val UNINITIALIZED = -1

private const val STATE_RENDERING = 1
private const val STATE_DRAWING = 2
private const val STATE_INIT = -1

// The special values to blurRedundancy.
private const val BLUR_ONCE = 0
private const val BLUR_ENDLESSLY = -1

// A short-hand for a suspend lambda.
private typealias Task = suspend CoroutineScope.() -> Unit

private class ContentBlurNode(
    var radius: Float,
    var downscale: Float,
    var blurRedundancy: Int
) : Modifier.Node(),
    DrawModifierNode,
    CompositionLocalConsumerModifierNode {
    // The Bitmap that shares memory with BlurScript; contains the content to be blurred
    // and is used to record the blurred bitmap along with its corresponding canvas.
    private var original: Bitmap? = null
    private lateinit var rendered: ImageBitmap
    private lateinit var canvas: Canvas

    // Represents the current state of this node:
    // - **Init**: Represents the state informing that everything needs to be initialized.
    // - **Rendering**: Denotes that we are creating a "blurred" version of the image as part of
    //                  the rendering process.
    // - **Drawing**: Implies that the rendered image is being drawn to the canvas and new is being
    //                recorded if next frame needs to be drawn.
    private var state: Int = STATE_INIT
    override val shouldAutoInvalidate: Boolean = false

    // Required for achieving the blur effect
    private lateinit var renderer: GraphicsLayer
    private lateinit var rs: RenderScript
    private lateinit var rsBlurScript: ScriptIntrinsicBlur
    private lateinit var outAllocation: Allocation
    private lateinit var inAllocation: Allocation

    // Represents the size of the content to be blurred.
    // If it changes, it will trigger a reset of the blur effect.
    private var size: Size = Size.Unspecified
        set(value) {
            if (value == field && state != STATE_INIT) return
            field = value
            reset(false)
        }

    private var work: Job? = null
    fun reset(invalidate: Boolean) {
        // return without doing anything.
        Log.d(TAG, "reset: $radius")
        require(!size.isUnspecified && !size.isEmpty())
        work?.cancel()
        // Recreate the bitmap with adjusted dimensions based on the current configuration.
        if (state != STATE_INIT) rendered.asAndroidBitmap().recycle()
        // Create a new bitmap with the adjusted dimensions.
        rendered = ImageBitmap(
            (size.width * downscale).roundToInt(),
            (size.height * downscale).roundToInt(),
        )
        canvas = Canvas(rendered)
        // destroy if something changed after init
        if (state != STATE_INIT && ::inAllocation.isInitialized) {
            inAllocation.destroy()
            outAllocation.destroy()
        }
        original?.recycle()
        // Create Allocation objects from the recreated bitmap for the blur effect.
        inAllocation = Allocation.createFromBitmap(
            rs,
            rendered.asAndroidBitmap(),
            Allocation.MipmapControl.MIPMAP_NONE,
            Allocation.USAGE_SCRIPT
        )
        outAllocation = Allocation.createTyped(rs, inAllocation.type)
        state = STATE_DRAWING
        if (invalidate) redraw()
    }

    override fun onAttach() {
        // Create a RenderScript instance from the current composition's context.
        val context = currentValueOf(LocalContext)
        rs = RenderScript.create(context)
        // Create a ScriptIntrinsicBlur instance using the RenderScript instance and the element type.
        rsBlurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        val ctx = currentValueOf(LocalGraphicsContext)
        renderer = ctx.createGraphicsLayer()
    }

    override fun onDetach() {
        work?.cancel()
        rs.destroy()
        original?.recycle()
        if (state != STATE_INIT) {
            rendered.asAndroidBitmap().recycle()
            outAllocation.destroy()
            inAllocation.destroy()
        }
        state = STATE_INIT // change state back to init
        val ctx = currentValueOf(LocalGraphicsContext)
        ctx.releaseGraphicsLayer(renderer)
    }

    // A task that is responsible for capturing the current composable content, comparing it with
    // the previous frame (to optimize blur redundancy), and applying the blur effect using RenderScript.
    private var frame = UNINITIALIZED // Frame counter, used to track and manage blur redundancy
    private val no_op: (frameTimeMillis: Long) -> Unit = {} // placeholder
    private val task: Task = task@{
        // `withFrameMillis(no_op)` is a way to wait for the next frame to start before proceeding
        // This ensures the task is synced with the frame timing of the composition.
        withFrameMillis(no_op) // Waits for the next frame to start.
        state = STATE_RENDERING
        // Captures the current composable content as a Bitmap.
        // TODO: For better performance on older Android versions,
        //  consider using reflection to directly render the composable
        //  onto the bitmap when software rendering available.
        val captured: Bitmap
        val captureTime = measureTimeMillis {
            captured = renderer.toImageBitmap().asAndroidBitmap()
        }
        // After capturing the content, we compare the new captured Bitmap with the original one
        // to check if there's any change. This is done to avoid redundant blur operations if the content hasn't changed.
        // If 'blurRedundancy' is set to BLUR_ENDLESSLY or BLUR_ONCE, skip comparison and always perform the blur.
        // Otherwise, check if the current captured Bitmap is the same as the original.
        val same: Boolean
        val compareTime = measureTimeMillis {
            same = when (blurRedundancy) {
                BLUR_ENDLESSLY, BLUR_ONCE -> false
                else -> original?.sameAs(captured) == true
            }
        }
        // If the bitmaps are identical and blur redundancy is enabled, check the frame count.
        // If the frame count exceeds the set threshold (blurRedundancy), skip the blur and return early.
        if (same && blurRedundancy > 0 && frame++ > blurRedundancy) {
            Log.d(TAG, "Bitmaps are identical on Frame(: $frame)")
            frame = UNINITIALIZED // Reset the frame count for future blur checks
            state = STATE_DRAWING
            return@task
        }
        // If the bitmaps are different or redundancy is disabled, create a software-backed Bitmap
        // if necessary
        // This ensures compatibility with older Android versions and devices that use hardware-backed Bitmaps.
        val copyTime = measureTimeMillis {
            val native =
                canvas.nativeCanvas  // Get the native canvas of the current drawing surface
            // Clear the canvas before drawing the new Bitmap
            native.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            // If the captured Bitmap is not hardware-backed or the device is using an older Android version,
            // draw the captured Bitmap directly onto the canvas.
            // If the captured Bitmap is hardware-backed (e.g., on newer devices), create a copy
            // with a software-backed config.
            when {
                Build.VERSION.SDK_INT <= Build.VERSION_CODES.S || captured.config != Bitmap.Config.HARDWARE ->
                    native.drawBitmap(
                        captured,
                        0f,
                        0f,
                        null
                    )  // Draw the captured bitmap directly onto the canvas
                else -> {
                    // For hardware-backed Bitmaps, create a copy with ARGB_8888 config and draw it to the canvas
                    val new = captured.copy(Bitmap.Config.ARGB_8888, true)
                    native.drawBitmap(new, 0f, 0f, null)
                    new.recycle()  // Recycle the newly created Bitmap to avoid memory leaks
                }
            }
        }
        // Depending on the blur redundancy setting, either discard the captured Bitmap or keep it for future comparison.
        when (blurRedundancy) {
            BLUR_ONCE, BLUR_ENDLESSLY -> captured.recycle()  // Recycle the Bitmap if blur is applied once or endlessly
            else -> original =
                captured  // Save the captured Bitmap for future comparisons if redundancy is enabled
        }
        // Apply the Gaussian blur effect to the bitmap.
        val blurTime = measureTimeMillis {
            // If the radius is 0, do nothing (skip blurring).
            if (radius == 0f) return@measureTimeMillis
            val native = rendered.asAndroidBitmap()
            inAllocation.copyFrom(native)
            rsBlurScript.setRadius(radius)
            rsBlurScript.setInput(inAllocation)
            // Apply the blur script to the output allocation.
            // Note: Do not use the input Allocation in forEach to avoid visual artifacts.
            rsBlurScript.forEach(outAllocation)
            // Copy the blurred output to the rendered bitmap.
            outAllocation.copyTo(native)
        }
        Log.d(
            TAG, "captureTime: $captureTime," +
                    " compareTime: $compareTime, " +
                    "copyTime: $copyTime, " +
                    "blurTime: $blurTime"
        )
        // Change the state back to "drawing" once the blur process is completed.
        state = STATE_DRAWING // Trigger redraw
        redraw()
    }

    override fun ContentDrawScope.draw() {
        // If the radius is 0 and downscale is 1, we skip drawing the blurred content and just draw the original content.
        // This can happen when no blur effect is applied or no downscale is needed.
        if (radius == 0f && downscale == 1f) return drawContent()
        // If the node is in the 'rendering' state (i.e., blur is currently being applied),
        // we immediately return to avoid interfering with the ongoing blur operation.
        if (state == STATE_RENDERING) return
        this@ContentBlurNode.size = size
        work?.cancel() // cancel the worker
        // Record the current composable content into the GraphicsLayer.
        // This captures the content as a bitmap that can be used for blurring.
        // Set the size of the recording area based on the downscale factor.
        // If downscale is 1.0f, draw the content at its original size.
        // Otherwise, scale the content down before drawing it.
        renderer.record(
            size = IntSize(
                (size.width * downscale).roundToInt(),
                (size.height * downscale).roundToInt()
            ),
            block = {
                when (downscale) {
                    1.0f -> this@draw.drawContent()
                    else -> scale(downscale, pivot = Offset.Zero) { this@draw.drawContent() }
                }
            }
        )
        // Draw the rendered (blurred) image to the screen.
        drawImage(
            rendered,
            dstSize = size.toIntSize(),
        )

        // If blur redundancy is set to BLUR_ONCE and this is the first frame,
        // skip blurring and return. This ensures that the blur effect is applied
        // only once.
        if (blurRedundancy == BLUR_ONCE && frame++ == 0) {
            frame = UNINITIALIZED
            return
        }
        Log.d(TAG, "draw: $frame")
        // Launch a coroutine to perform the blur operation asynchronously.
        work = coroutineScope.launch(block = task)
    }
}