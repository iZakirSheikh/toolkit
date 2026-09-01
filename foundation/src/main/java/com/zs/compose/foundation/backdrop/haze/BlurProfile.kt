package com.zs.compose.foundation.backdrop.haze

import androidx.annotation.FloatRange
import androidx.compose.ui.util.packFloats

/**
 * Defines the specifications for a performance-optimized blur effect.
 *
 * @param raw The packed 64-bit representation of the blur specifications.
 */
@JvmInline
value class BlurProfile(private val raw: Long) {

    /**
     * Creates a new [BlurProfile].
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
            radiusPx
        )
    )

    /**
     * The downscale ratio applied before the blur calculation.
     */
    val downsample: Float
        get() = Float.fromBits((raw shr 32).toInt())

    /**
     * The radius of the blur effect in pixels.
     */
    val radiusPx: Float
        get() = Float.fromBits((raw and 0xFFFFFFFFL).toInt())

    /**
     * Retrieves the [downsample] factor for destructuring declarations.
     */
    operator fun component1(): Float = downsample

    /**
     * Retrieves the [radiusPx] for destructuring declarations.
     */
    operator fun component2(): Float = radiusPx

    /**
     * Returns a string representation of the [BlurProfile].
     */
    override fun toString(): String = "BlurSpec(downsample=$downsample, radiusPx=$radiusPx)"
}