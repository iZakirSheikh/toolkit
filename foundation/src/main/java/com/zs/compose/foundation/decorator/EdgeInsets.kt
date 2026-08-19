package com.zs.compose.foundation.decorator

import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt


/**
 * An immutable, memory-efficient representation of four-sided edge insets (start, top, end, bottom).
 *
 * This is implemented as a `@JvmInline` value class wrapping a [Constraints] object. It packs
 * four integer-rounded [Dp] values into a single primitive long to avoid object allocations
 * while providing semantic padding values.
 *
 * Note: *Because this uses [Constraints] as the internal storage, [Dp] values are rounded
 * to their nearest integer during construction.*
 *
 * @property value The underlying [Constraints] used to store start, top, end, and bottom
 * values in the minWidth, minHeight, maxWidth, and maxHeight slots respectively.
 */
@JvmInline
value class EdgeInsets private constructor(private val value: Constraints) {

    /**
     * Creates an [EdgeInsets] with explicit start, top, end, and bottom padding.
     */
    constructor(start: Dp, top: Dp = 0.dp, end: Dp = 0.dp, bottom: Dp = 0.dp) : this(
        Constraints(start.value.roundToInt(), top.value.roundToInt(), end.value.roundToInt(), bottom.value.roundToInt())
    )

    /**
     * Creates an [EdgeInsets] with symmetric horizontal and vertical padding.
     */
    constructor(horizontal: Dp = 0.dp, vertical: Dp = 0.dp) : this(
        horizontal, vertical, horizontal, vertical
    )

    /**
     * Apply [all] dp of additional space along each edge of the content, left, top, right and bottom.
     */
    constructor(all: Dp):this(horizontal = all, vertical = all)


    fun calculateLeftPaddingInset(layoutDirection: LayoutDirection) =
        if (layoutDirection == LayoutDirection.Ltr) value.minWidth.dp else value.maxWidth.dp
    fun calculateTopInset() = value.minHeight.dp
    fun calculateRightInset(layoutDirection: LayoutDirection) =
        if (layoutDirection == LayoutDirection.Ltr) value.maxWidth.dp else value.minWidth.dp
    fun calculateBottomInset() = value.maxHeight.dp
}