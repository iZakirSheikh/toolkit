package com.primex.core.blur

import android.view.View
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.platform.inspectable

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
 * The modifier that is used in case of preview mode for blurs.
 */
internal fun Modifier.ScrimModifier(radius: Float, downsample: Float) = inspectable(
    inspectorInfo = debugInspectorInfo {
        name = "legacyBackgroundBlur"
        properties["radius"] = radius
        properties["downsample"] = downsample
    },
    factory = {
        background(androidx.compose.ui.graphics.Color.Blue.copy(0.4f))
    }
)

/**
 * Extension function to **invoke** the internal draw method of GraphicsLayer using ****reflection****.
 *
 * @param canvas The Canvas on which to draw the contents.
 * @param parentLayer The parent GraphicsLayer, if any.
 * @throws RuntimeException if there's an error invoking the draw method.
 */
internal fun GraphicsLayer.draw1(canvas: Canvas, parentLayer: GraphicsLayer?) {
    try {
        // Obtain the draw method via reflection.
        // getDeclaredMethod allows accessing even private or internal methods.
        val impl = GraphicsLayer::class.java.getDeclaredField("impl")
        impl.isAccessible = true
       val implref = impl.get(this)
        val drawMethod  =implref::class.java.getDeclaredMethod("draw", Canvas::class.java)

        // Make the method accessible as it's internal and not normally available.
       // drawMethod.isAccessible = true
        // Invoke the draw method on the current GraphicsLayer instance with the provided parameters.
        drawMethod.invoke(implref, canvas)
    } catch (e: Exception) {
        // If any exception occurs during reflection or method invocation, wrap it in a RuntimeException.
        throw RuntimeException("Error invoking draw method", e)
    }
}
