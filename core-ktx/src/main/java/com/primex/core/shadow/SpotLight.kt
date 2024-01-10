package com.primex.core.shadow

import androidx.compose.ui.geometry.Offset
import com.primex.core.ExperimentalToolkitApi

/**
 * Represents the possible positions of a spotlight, defining its directional behavior.
 *
 * Each value represents a distinct direction from which a spotlight can originate,
 * influencing shadow casting and visual effects.
 */

@ExperimentalToolkitApi
enum class SpotLight {

    /**
     * Spotlight positioned directly above an element.
     */
    TOP,

    /**
     * Spotlight positioned directly below an element.
     */
    BOTTOM,

    /**
     * Spotlight positioned to the left of an element.
     */
    LEFT,

    /**
     * Spotlight positioned to the right of an element.
     */
    RIGHT,

    /**
     * Spotlight positioned diagonally at the top-left corner of an element.
     */
    TOP_LEFT,

    /**
     * Spotlight positioned diagonally at the bottom-left corner of an element.
     */
    BOTTOM_LEFT,

    /**
     * Spotlight positioned diagonally at the top-right corner of an element.
     */
    TOP_RIGHT,

    /**
     * Spotlight positioned diagonally at the bottom-right corner of an element.
     */
    BOTTOM_RIGHT;

    /**
     * Returns the spotlight position that is diametrically opposite to the current one.
     *
     * @return The mirror position of this spotlight.
     */
    val mirror: SpotLight
        get() = when (this) {
            TOP -> BOTTOM
            BOTTOM -> TOP
            LEFT -> RIGHT
            RIGHT -> LEFT
            TOP_LEFT -> BOTTOM_RIGHT
            TOP_RIGHT -> BOTTOM_LEFT
            BOTTOM_LEFT -> TOP_RIGHT
            BOTTOM_RIGHT -> TOP_LEFT
        }

    /**
     * Calculates the offset required to position a shadow based on the spotlight's direction.
     *
     * @param value The magnitude of the shadow offset.
     * @param elevated Whether the shadow should appear elevated or sunken.
     * @return The calculated offset as an [Offset] instance.
     */
    internal fun toOffset(
        value: Float,
        elevated: Boolean = true
    ): Offset =
        when (this) {
            TOP -> if (elevated) Offset(0f, -value) else Offset(0f, value)
            BOTTOM -> if (elevated) Offset(0f, value) else Offset(0f, -value)
            LEFT -> if (elevated) Offset(-value, 0f) else Offset(value, 0f)
            RIGHT -> if (elevated) Offset(value, 0f) else Offset(-value, 0f)
            TOP_LEFT -> if (elevated) Offset(-value, -value) else Offset(value, value)
            BOTTOM_LEFT -> if (elevated) Offset(-value, value) else Offset(value, -value)
            TOP_RIGHT -> if (elevated) Offset(value, -value) else Offset(-value, value)
            BOTTOM_RIGHT -> if (elevated) Offset(value, value) else Offset(-value, -value)
        }
}
