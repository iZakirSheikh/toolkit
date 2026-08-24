package com.zs.compose.foundation

import androidx.annotation.FloatRange
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

@Immutable
class LinearGradient internal constructor(
    private val colors: List<Color>,
    private val stops: List<Float>? = null,
    private val tileMode: TileMode = TileMode.Clamp,
    angle: Float = 0f
) : ShaderBrush() {

    // Convert angle deg → radian angle
    private val radian = Math.toRadians((/*90f - */angle).mod(360f).toDouble())

    override fun createShader(size: Size): Shader {
        // Calculate the center point of the drawing area
        val center = Offset(size.width / 2f, size.height / 2f)
        // Calculate the half-diagonal length to ensure the gradient covers the entire area regardless of rotation
        val radius = hypot(size.width.toDouble(), size.height.toDouble()) / 2.0

        // Determine the horizontal and vertical distance from the center based on the angle
        val dx = (radius * cos(radian)).toFloat()
        val dy = (radius * sin(radian)).toFloat()

        // Calculate the start and end coordinates by shifting from the center along the angle vector
        val start = Offset(center.x - dx, center.y - dy)
        val end   = Offset(center.x + dx, center.y + dy)

        return LinearGradientShader(
            colors = colors,
            colorStops = stops,
            from = start,
            to = end,
            tileMode = tileMode
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LinearGradient

        if (radian != other.radian) return false
        if (colors != other.colors) return false
        if (stops != other.stops) return false
        if (tileMode != other.tileMode) return false

        return true
    }

    override fun hashCode(): Int {
        var result = radian.hashCode()
        result = 31 * result + colors.hashCode()
        result = 31 * result + (stops?.hashCode() ?: 0)
        result = 31 * result + tileMode.hashCode()
        return result
    }

    override fun toString(): String {
        return "LinearGradient(colors=$colors, stops=$stops, tileMode=$tileMode, radian=$radian)"
    }
}

/**
 * Creates a linear gradient [Brush] using a list of colors and a specified angle.
 *
 * The gradient starts from one side of the bounding box and ends at the opposite side,
 * rotating around the center based on the provided [angle].
 *
 * @param colors The colors to be distributed along the gradient line.
 * @param angle The angle in degrees at which the gradient should be oriented.
 * @param tileMode The strategy used to fill the area outside the gradient bounds.
 * @return A [Brush] that applies the linear gradient.
 */
@Stable
fun Brush.Companion.linearGradient(
    vararg colorStops: Pair<Float, Color>,
    @FloatRange(0.0) angle: Float = 0.0f,
    tileMode: TileMode = TileMode.Clamp,
): Brush = LinearGradient(
    colors = List(colorStops.size) { i -> colorStops[i].second },
    stops = List(colorStops.size) { i -> colorStops[i].first },
    angle = angle,
    tileMode = tileMode,
)

/**
 * Creates a linear gradient brush with the provided [colors] and a specific [angle].
 *
 * Unlike standard linear gradients that use start and end offsets, this version allows
 * specifying the direction of the gradient using an angle in degrees. The gradient
 * is centered within the drawing area.
 *
 */
@Stable
fun Brush.Companion.linearGradient(
    colors: List<Color>,
    @FloatRange(0.0) angle: Float = 0.0f,
    tileMode: TileMode = TileMode.Clamp,
): Brush =
    LinearGradient(
        colors = colors,
        stops = null,
        angle = angle,
        tileMode = tileMode,
    )

/**
 * Creates a linear gradient [Brush] with the given [colors] and a specific [angle].
 *
 * The gradient is centered within the drawing area, and the [angle] determines its direction.
 *
 * @param colors The colors to be distributed along the gradient line.
 * @param angle The angle in degrees at which the gradient should be applied.
 * @param tileMode The strategy used to fill the area outside the gradient's bounds.
 * @return A [Brush] that applies the linear gradient.
 */
@Stable
fun Brush.Companion.linearGradient(
    colors: List<Color>,
     stops: List<Float>? = null,
    @FloatRange(0.0) angle: Float = 0.0f,
    tileMode: TileMode = TileMode.Clamp,
): Brush =
    LinearGradient(
        colors = colors,
        stops = stops,
        angle = angle,
        tileMode = tileMode,
    )