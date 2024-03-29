@file:Suppress("NOTHING_TO_INLINE")

package com.primex.core.shadow


import androidx.annotation.ColorInt
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.DrawModifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.primex.core.ExperimentalToolkitApi


private inline val Offset.mirror get() = copy(-x, -y)

private class ShadowModifierImpl(
    val shape: Shape,
    val offset: Offset,
    val radius: Float,
    val lightShadowColor: Color,
    val darkShadowColor: Color,
    val strokeWidth: Float = Float.NaN
) : DrawModifier {
    override fun ContentDrawScope.draw() {

        require(shape is CornerBasedShape)

        val lightColorInt = lightShadowColor.toArgb()
        val darkShadowInt = darkShadowColor.toArgb()

        // requires same corner radius
        // force requires same.

        val topStartPx = shape.topStart.toPx(size, this)
        val topEndPx = shape.topEnd.toPx(size, this)
        val bottomStartPx = shape.bottomStart.toPx(size, this)
        val bottomEndPx = shape.bottomEnd.toPx(size, this)

        require(
            topStartPx == topEndPx
                    && topEndPx == bottomEndPx
                    && bottomEndPx == bottomStartPx
                    && bottomStartPx == topStartPx
        )


        val radiusPx = topStartPx

        val drawAsFill = strokeWidth.isNaN()

        when (drawAsFill) {
            true -> background(
                offset = offset,
                corners = radiusPx,
                lightShadowColor = lightColorInt,
                darkShadowColor = darkShadowInt,
                radius = radius
            )
            else -> foreground(
                offset = offset,
                corners = radiusPx,
                lightShadowColor = lightColorInt,
                darkShadowColor = darkShadowInt,
                radius = radius,
                strokeWidth = strokeWidth
            )
        }
    }
}


internal inline fun ContentDrawScope.background(
    offset: Offset,
    corners: Float,
    @ColorInt lightShadowColor: Int,
    @ColorInt darkShadowColor: Int,
    radius: Float,
) {
    drawIntoCanvas { canvas ->
        // light
        canvas.shadow(
            offset = offset,
            corners = corners,
            color = lightShadowColor,
            radius = radius,
            size = size
        )
        val mirrored = offset.mirror
        // dark
        canvas.shadow(
            offset = mirrored,
            corners = corners,
            color = darkShadowColor,
            radius = radius,
            size = size
        )
    }
    drawContent()
}


internal inline fun ContentDrawScope.foreground(
    offset: Offset,
    corners: Float,
    @ColorInt lightShadowColor: Int,
    @ColorInt darkShadowColor: Int,
    strokeWidth: Float,
    radius: Float,
) {
    drawContent()

    drawIntoCanvas { canvas ->
        // draw light shadow
        val mirror = offset.mirror
        canvas.shadow(
            offset = mirror,
            color = lightShadowColor,
            corners = corners,
            radius = radius,
            strokeWidth = strokeWidth,
            size = size
        )

        // draw dar shadow
        canvas.shadow(
            offset = offset,
            color = darkShadowColor,
            corners = corners,
            radius = radius,
            strokeWidth = strokeWidth,
            size = size
        )
    }
}

private const val POINT_60 = 0.6f
private const val POINT_95 = 0.95f

/**
 * Applies a customizable shadow effect to the content decorated by this modifier.
 *
 *
 * @param shape The shape of the shadow, defining its outline and corners.
 * @param lightShadowColor The color of the lighter part of the shadow.
 * @param darkShadowColor The color of the darker part of the shadow.
 * @param elevation The apparent distance between the content and the surface casting the shadow.
 * @param intensity Controls the contrast between the light and dark parts of the shadow. A value
 *                  of 0.0 produces a faint, subtle shadow, while a value of 1.0 produces a strong,
 *                  high-contrast shadow. Default value is NaN, which uses a reasonable default
 *                  intensity based on the elevation.
 * @param spotLight Configures a spotlight for directional lighting effects.
 * @param border An optional border stroke to apply around the content, visually enhancing the
 *                shadow effect.
 *
 * @throws IllegalArgumentException If [intensity] is not within the valid range of 0.0 to 1.0.
 * @see SpotLight
 *
 * **Example:**
 * ```kotlin
 * Text(
 *     modifier = Modifier.shadow(
 *         shape = MaterialTheme.shapes.small,
 *         lightShadowColor = Color.Gray.withOpacity(0.5f),
 *         darkShadowColor = Color.Black.withOpacity(0.3f),
 *         elevation = 4.dp,
 *     )
 * )
 * ```
 */
@ExperimentalToolkitApi
fun Modifier.shadow(
    shape: CornerBasedShape,
    lightShadowColor: Color,
    darkShadowColor: Color,
    elevation: Dp,
    intensity: Float = Float.NaN,
    spotLight: SpotLight,
    border: BorderStroke? = null
): Modifier = composed {
    val elevationPx = kotlin.math.abs(with(LocalDensity.current) { elevation.toPx() })
    val elevated = elevation > 0.dp

    val multiplier = elevationPx * if (elevated) POINT_60 else POINT_95

    val blurRadius =
        (elevationPx * if (elevated) POINT_95 else POINT_60)

    val offset =
        spotLight.toOffset(multiplier, elevated)

    shadow(
        shape = shape,
        offset = offset,
        radius = blurRadius,
        lightShadowColor = if (elevated) lightShadowColor else darkShadowColor,
        darkShadowColor = if (elevated) darkShadowColor else lightShadowColor,
        border = border,
        strokeWidth = if (elevated) Float.NaN else multiplier,
        intensity = intensity
    )
}


/**
 * @see shadow
 */
@ExperimentalToolkitApi
private fun Modifier.shadow(
    shape: CornerBasedShape,
    offset: Offset,
    radius: Float,
    strokeWidth: Float = Float.NaN,
    lightShadowColor: Color,
    darkShadowColor: Color,
    intensity: Float = Float.NaN,
    border: BorderStroke? = null
): Modifier {

    val lightShadowColorM =
        if (intensity.isNaN()) lightShadowColor else lightShadowColor.copy(intensity)
    val darkShadowColorM =
        if (intensity.isNaN()) darkShadowColor else darkShadowColor.copy(intensity)

    val shadow = when (offset) {
        Offset.Zero -> Modifier
        else -> ShadowModifierImpl(
            shape = shape,
            lightShadowColor = lightShadowColorM,
            darkShadowColor = darkShadowColorM,
            offset = offset,
            radius = radius,
            strokeWidth = strokeWidth
        )
    }


    return this
        .then(shadow)
        .then(if (border != null) Modifier.border(border, shape) else Modifier)
        .clip(shape)
}