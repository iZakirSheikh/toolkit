package com.zs.compose.foundation.backdrop

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import com.zs.compose.foundation.lerp
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Observes the device's tilt via the gravity sensor and returns the implied
 * position of a virtual overhead light source, expressed in spherical
 * coordinates around the phone.
 *
 * @return an [Offset] where:
 *   - `x` = **azimuth**, 0°–360°. The compass-style direction (in the phone's
 *     own XY plane) that the light is coming from. Rotates as you spin the
 *     phone's tilt direction around.
 *   - `y` = **elevation**, -90°–90°. How high the light sits above the
 *     phone's plane. `+90°` = light directly overhead (phone lying flat,
 *     screen up). `0°` = light at the horizon (phone standing on its edge).
 *     `-90°` = light directly below (phone flat, screen facing down).
 *     Using the full ±90° range (rather than clamping to 0°–90°) means this
 *     stays meaningful even when the phone is flipped upside down.
 *
 * Internally this reads [Sensor.TYPE_GRAVITY] (falls back to
 * [Sensor.TYPE_ACCELEROMETER] on devices without it) and applies exponential
 * smoothing so the result glides instead of jittering with sensor noise.
 */
@Composable
fun observeAzimuthElevation(): Offset {
    val context = LocalContext.current

    // Packed into a single Long (via Offset.packedValue) so we only need one
    // mutableLongStateOf — cheaper than two separate Float states, and lets
    // us return/hold azimuth+elevation as one Offset throughout.
    var _value by remember { mutableLongStateOf(Offset.Zero.packedValue) }

    DisposableEffect(Unit) {
        val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = manager.getDefaultSensor(Sensor.TYPE_GRAVITY)
            ?: manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val listener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return

                // Raw gravity vector components, in the phone's own coordinate
                // frame: x = right, y = up (sensor convention, NOT canvas),
                // z = out of the screen toward the user's face.
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val norm = sqrt(x * x + y * y + z * z).coerceAtLeast(0.0001f)

                // Azimuth: angle of the vector's projection onto the phone's
                // XY (screen) plane. y is negated here because sensor-space y
                // increases upward while Canvas/Compose y increases downward —
                // without this negation, a gradient angle driven by this value
                // would land on the wrong edge for vertical tilts.
                val rawAzimuth = (atan2(-y, x) * (180f / PI).toFloat() + 360f) % 360f

                // Elevation: angle of the vector above/below the phone's XY
                // plane, using asin(z) over the FULL -1..1 range of z (not
                // clamped to 0..90) so upside-down orientations (z < 0) map to
                // negative elevation instead of being indistinguishable from
                // right-side-up tilts.
                val rawElevation = Math.toDegrees(
                    asin((z / norm).coerceIn(-1f, 1f).toDouble())
                ).toFloat()

                // Exponential smoothing factor: how much each new reading
                // moves the stored value toward the fresh raw reading.
                // Lower = smoother/heavier feel, higher = snappier/jitterier.
                val alpha = 0.15f

                val old = Offset(_value)
                val lightPosition = Offset(
                    // Azimuth is an angle that wraps at 360°->0°, so it CANNOT
                    // be smoothed with a plain linear lerp: e.g. averaging
                    // 359° and 1° naively gives 180° (exactly wrong) instead
                    // of ~0° (the true shortest-path midpoint). lerpAngle
                    // takes the shortest signed direction around the circle
                    // before interpolating, avoiding that snap.
                    x = lerpAngle(old.x, rawAzimuth, alpha),
                    // Elevation has no wraparound (-90..90 is a bounded range,
                    // not circular), so a plain linear lerp is safe here.
                    y = old.y * (1f - alpha) + rawElevation * alpha
                )
                _value = lightPosition.packedValue
            }
        }

        manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        onDispose { manager.unregisterListener(listener) }
    }

    return Offset(_value)
}

/**
 * Linearly interpolates between two angles (in degrees), taking the
 * shortest path around the 0°–360° circle rather than a naive straight-line
 * interpolation. Needed for smoothing any angle that wraps (like azimuth),
 * since plain [lerp] breaks near the 359°->0° boundary.
 */
private fun lerpAngle(fromDegrees: Float, toDegrees: Float, fraction: Float): Float {
    val delta = ((toDegrees - fromDegrees + 540f) % 360f) - 180f // shortest signed distance, -180..180
    return (fromDegrees + delta * fraction + 360f) % 360f
}