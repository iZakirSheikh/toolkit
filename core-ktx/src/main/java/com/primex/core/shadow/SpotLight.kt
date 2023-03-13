package com.primex.core.shadow

import androidx.compose.ui.geometry.Offset

/**
 * The special value where a spotlight
 */
enum class SpotLight {

    TOP,

    BOTTOM,

    LEFT,

    RIGHT,

    TOP_LEFT,

    BOTTOM_LEFT,

    TOP_RIGHT,

    BOTTOM_RIGHT;

    /**
     * The spotlight opposite to the current one.
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
     * Returns the offset the the shadow must be translated.
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