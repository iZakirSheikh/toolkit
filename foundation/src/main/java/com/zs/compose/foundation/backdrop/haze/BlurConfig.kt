package com.zs.compose.foundation.backdrop.haze


import androidx.annotation.FloatRange
import androidx.compose.ui.util.packFloats
import androidx.compose.ui.util.unpackFloat1
import androidx.compose.ui.util.unpackFloat2

/**
 * Defines the specifications for a performance-optimized blur effect.
 *
 * @param raw The packed 64-bit representation of the blur specifications.
 * @property downsample The factor by which content is scaled down before applying the blur.
 * @property radiusPx The actual radius of the blur effect in pixels.
 */
@JvmInline
value class BlurConfig(private val raw: Long) {

    /**
     * Creates a new [BlurConfig].
     *
     * @param downsample The factor by which content is scaled down before applying the blur.
     *                   For example, `0.5f` reduces the resolution by half, improving rendering performance.
     * @param radiusPx The actual radius of the blur effect in pixels.
     */
    constructor(
        @FloatRange(0.0, 1.0) downsample: Float,
        @FloatRange(from = 0.0) radiusPx: Float
    ) : this(
        packFloats(
            downsample.also {
                require(it in 0f..1f) {
                    "downsample must be between 0.0 and 1.0, was $it"
                }
            },
            radiusPx.coerceAtLeast(0f)
        )
    )


    val downsample: Float
        get() = unpackFloat1(raw)
    val radiusPx: Float
        get() = unpackFloat2(raw)


    operator fun component1(): Float = downsample
    operator fun component2(): Float = radiusPx

    override fun toString(): String = "BlurConfig(downsample=$downsample, radiusPx=$radiusPx)"
}