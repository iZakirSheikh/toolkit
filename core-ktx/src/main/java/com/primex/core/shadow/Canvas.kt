@file:Suppress("NOTHING_TO_INLINE")

package com.primex.core.shadow

import android.graphics.BlurMaskFilter
import androidx.annotation.ColorInt
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*


private inline fun Canvas.drawRoundRect(
    size: Size,
    radius: Float,
    scale: Float = 0f,
    paint: Paint
) {
    val (width, height) = size
    drawRoundRect(-scale, -scale, width + scale, height + scale, radius, radius, paint)
}

private inline fun Canvas.clipRoundRect(
    size: Size,
    radius: Float,
    clipOp: ClipOp = ClipOp.Intersect
) {
    val (width, height) = size
    val roundRect = Path().apply {
        moveTo(0f, 0f)
        addRoundRect(RoundRect(0f, 0f, width, height, radius, radius))
    }
    clipPath(roundRect, clipOp = clipOp)
}

/**
 * Draws shadow of given [color] & [size] .
 * @param offset: The [Offset] to translate the canvas by
 * @param size: The side of the canvas/shadow.
 * @param corners: The radius of the shadow.
 * @param color: The color of the shadow.
 * @param radius: The radius to use with [BlurMaskFilter]
 * @param strokeWidth: The width of the stroke. if equal to zero shadow will be drawn as fill otherwise as stroke.
 */
internal inline fun Canvas.shadow(
    size: Size,
    corners: Float,
    offset: Offset,
    @ColorInt color: Int,
    radius: Float,
    strokeWidth: Float = Float.NaN
) {
    // if stroke width isnot NAN draw foreground shadow else draw background shadow
    val drawFill = strokeWidth.isNaN()

    val paint =
        Paint().also { paint ->
            paint.asFrameworkPaint().also { native ->
                native.isAntiAlias = true
                native.color = color
                native.maskFilter = BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL)
                // make paint for each shadow type separately
                when (drawFill) {
                    true -> native.isDither = true
                    else -> {
                        native.strokeWidth = strokeWidth
                        native.style = android.graphics.Paint.Style.STROKE
                    }
                }
            }
        }

    val scaleF = if (drawFill) 0f else strokeWidth

    val canvas = this
    canvas.save()
    // in case foreground clip it.
    if (!drawFill) canvas.clipRoundRect(size, corners)
    canvas.translate(offset.x, offset.y)
    canvas.drawRoundRect(size, corners, scaleF, paint)
    canvas.restore()
}