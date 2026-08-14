package com.zs.compose.foundation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.fastForEachIndexed
import kotlin.math.max

// SlotMeasurePolicy: A simple layout policy that centers its child(ren)
val SlotMeasurePolicy: MeasurePolicy = MeasurePolicy { measurables, constraints ->

    // Case 1: No children → return an empty layout with minimum constraints.
    if (measurables.isEmpty()) {
        return@MeasurePolicy layout(constraints.minWidth, constraints.minHeight) {}
    }

    // Case 2: Exactly one child → measure and center it inside the box.
    if (measurables.size == 1) {
        val placeable = measurables[0].measure(constraints)

        // Ensure layout respects minimum constraints.
        val width = max(constraints.minWidth, placeable.width)
        val height = max(constraints.minHeight, placeable.height)

        return@MeasurePolicy layout(width, height) {
            // Compute centered position using Alignment.Center.
            val pos = Alignment.Center.align(
                IntSize(placeable.width, placeable.height),
                IntSize(width, height),
                layoutDirection,
            )
            placeable.place(pos)
        }
    }

    // Case 3: More than one child → log a warning (not recommended).
    Log.w("Slot", "The recommended max children for Slot is 1")

    // Measure all children and track max width/height.
    val placeables = arrayOfNulls<Placeable>(measurables.size)
    var width = constraints.minWidth
    var height = constraints.minHeight

    measurables.fastForEachIndexed { index, measurable ->
        val placeable = measurable.measure(constraints)
        placeables[index] = placeable
        width = max(width, placeable.width)
        height = max(height, placeable.height)
    }

    // Layout size is determined by the largest child.
    return@MeasurePolicy layout(width, height) {
        // Position all children centered (same as single child).
        placeables.forEach { placeable ->
            placeable?.let {
                val pos = Alignment.Center.align(
                    IntSize(it.width, it.height),
                    IntSize(width, height),
                    layoutDirection,
                )
                it.place(pos)
            }
        }
    }
}

/**
 * A simple layout container that centers its child within the available space.
 *
 * ### Usage
 * - **Recommended children:** 0 or 1.
 *   - If no child is provided, the layout resolves to its minimum constraints.
 *   - If a single child is provided, it is measured and placed at the center.
 *   - If multiple children are provided, all are measured and centered, but this
 *     usage is discouraged and will log a warning.
 *
 * ### Behavior
 * - Respects minimum width/height constraints.
 * - Expands to fit the largest child when multiple children are present.
 * - Aligns children using [Alignment.Center].
 *
 * ### Notes
 * - This composable is intended as a lightweight, predictable container.
 * - Prefer using it for simple centering scenarios rather than complex layouts.
 *
 * @param modifier Optional [Modifier] for styling or layout adjustments.
 * @param content The child composable(s) to be placed inside the container.
 */
@Composable
inline fun Slot(content: @Composable () -> Unit) =
    Layout(content, Modifier, SlotMeasurePolicy)