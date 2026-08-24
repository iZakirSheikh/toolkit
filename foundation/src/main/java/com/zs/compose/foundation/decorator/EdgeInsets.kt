package com.zs.compose.foundation.decorator

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import com.zs.compose.foundation.requirePrecondition
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
@Immutable
value class EdgeInsets internal constructor(private val value: Constraints) {

    init {
        requirePrecondition(
            (start.value >= 0f) and (top.value >= 0f) and (end.value >= 0f) and (bottom.value >= 0f)
        ) {
            "EdgeInsets must be non-negative"
        }
    }

    private inline val start get() = value.minWidth.dp
    private inline val end get() = value.maxWidth.dp
    private inline val top get() = value.minHeight.dp
    private inline val bottom get() = value.maxHeight.dp

    fun calculateLeftPaddingInset(layoutDirection: LayoutDirection) =
        if (layoutDirection == LayoutDirection.Ltr) start else end

    fun calculateTopInset() = top
    fun calculateRightInset(layoutDirection: LayoutDirection) =
        if (layoutDirection == LayoutDirection.Ltr) end else start

    fun calculateBottomInset() = bottom

    /** Adds two [EdgeInsets] together. */
    @Stable
    operator fun plus(other: EdgeInsets) =
        EdgeInsets(start = start + other.start, end = end + other.end, top = top + other.top, bottom = bottom + other.bottom)

    /** Subtracts [other]  from this [EdgeInsets]. */
    @Stable
    operator fun minus(other: EdgeInsets) =
        EdgeInsets(
            start = (start - other.start).coerceAtLeast(0.dp),
            end = (end - other.end).coerceAtLeast(0.dp),
            top = (top - other.top).coerceAtLeast(0.dp),
            bottom = (bottom - other.bottom).coerceAtLeast(0.dp)
        )

    @Stable
    override fun toString() = "EdgeInsets(start=$start, top=$top, end=$end, bottom=$bottom)"
}

/**
 * Creates an [EdgeInsets] with explicit start, top, end, and bottom padding.
 */
@Stable
fun EdgeInsets(start: Dp = 0.dp, top: Dp = 0.dp, end: Dp = 0.dp, bottom: Dp= 0.dp) =
    EdgeInsets(
        Constraints(
            start.value.roundToInt(),
            end.value.roundToInt(),
            top.value.roundToInt(),
            bottom.value.roundToInt()
        )
    )

/**
 * Creates an [EdgeInsets] with symmetric horizontal and vertical padding.
 */
@Stable
fun EdgeInsets(horizontal: Dp = 0.dp, vertical: Dp = 0.dp) =
    EdgeInsets(start = horizontal, end = horizontal, top = vertical, bottom = vertical)

/**
 * Creates an [EdgeInsets] with fields set to [all]
 */
@Stable
fun EdgeInsets(all: Dp) =
    EdgeInsets(start = all, end = all, top = all, bottom = all)