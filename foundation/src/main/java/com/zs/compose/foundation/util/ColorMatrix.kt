package com.zs.compose.foundation.util

import androidx.compose.ui.graphics.ColorMatrix

/**
 * Replaces the luma of the pixel with [luminance], preserving hue and saturation.
 * Matches BlendMode.Luminosity behavior with a fully-opaque tint.
 * @param luminance target luma, 0f (black) .. 1f (white)
 */
fun ColorMatrix.setToLuminance(luminance: Float) {
    if (values.size < 20) return
    reset()

    val lum = luminance.coerceIn(0f, 1f)
    val lr = 0.213f
    val lg = 0.715f
    val lb = 0.072f
    val t = lum * 255f

    this[0, 0] = 1 - lr
    this[0, 1] = -lg
    this[0, 2] = -lb
    this[0, 4] = t

    this[1, 0] = -lr
    this[1, 1] = 1 - lg
    this[1, 2] = -lb
    this[1, 4] = t

    this[2, 0] = -lr
    this[2, 1] = -lg
    this[2, 2] = 1 - lb
    this[2, 4] = t
}

/**
 * Applies a linear contrast adjustment to the pixel, scaling RGB channels around
 * the mid-gray pivot (0.5) and leaving alpha untouched.
 *
 * This is NOT a true Soft Light blend — Soft Light is a nonlinear, per-pixel
 * conditional operation that can't be represented by an affine ColorMatrix.
 * This is an approximation that produces a similar "gentle S-curve" visual
 * effect for simple dimming/contrast use cases, but will not match
 * BlendMode.SOFT_LIGHT pixel-for-pixel.
 *
 * @param contrast contrast multiplier. 1f = unchanged, < 1f reduces contrast
 * (pulls toward mid-gray), > 1f increases contrast. Values <= 0f or very large
 * values may clip heavily.
 */
fun ColorMatrix.setToContrast(contrast: Float) {
    if (values.size < 20) return
    reset()

    val t = (1 - contrast) / 2f * 255f

    this[0, 0] = contrast
    this[0, 4] = t

    this[1, 1] = contrast
    this[1, 4] = t

    this[2, 2] = contrast
    this[2, 4] = t
}