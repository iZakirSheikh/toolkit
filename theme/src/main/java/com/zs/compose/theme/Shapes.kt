/*
 * Copyright 2025 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 27-01-2025.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zs.compose.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

// source - https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/Shapes.kt;bpv=0
// last commit date - 2025-01-17 00:33

/**
 * AppTheme surfaces can be displayed in different shapes. Shapes direct attention, identify
 * components, communicate state, and express brand.
 *
 * The shape scale defines the style of container corners, offering a range of roundedness from
 * square to fully circular.
 *
 * There are different sizes of shapes:
 * - Extra Small
 * - Small
 * - Medium
 * - Large, Large Increased
 * - Extra Large, Extra Large Increased
 * - Extra Extra Large
 *
 * You can customize the shape system for all components in the [AppTheme] or you can do it on
 * a per component basis.
 *
 * You can change the shape that a component has by overriding the shape parameter for that
 * component. For example, by default, buttons use the shape style “full.” If your product requires
 * a smaller amount of roundedness, you can override the shape parameter with a different shape
 * value like [AppTheme.shapes.small].
 *
 * To learn more about shapes, see
 * [Material Design shapes](https://m3.material.io/styles/shape/overview).
 *
 * @param compact A shape style with 4 same-sized corners whose size are bigger than
 *   [RectangleShape] and smaller than [Shapes.small]. By default autocomplete menu, select menu,
 *   snackbars, standard menu, and text fields use this shape.
 * @param small A shape style with 4 same-sized corners whose size are bigger than
 *   [Shapes.extraSmall] and smaller than [Shapes.medium]. By default chips use this shape.
 * @param medium A shape style with 4 same-sized corners whose size are bigger than [Shapes.small]
 *   and smaller than [Shapes.large]. By default cards and small FABs use this shape.
 * @param large A shape style with 4 same-sized corners whose size are bigger than [Shapes.medium]
 *   and smaller than [Shapes.extraLarge]. By default extended FABs, FABs, and navigation drawers
 *   use this shape.
 * @param xLarge A shape style with 4 same-sized corners whose size are bigger than
 *   [Shapes.large] and smaller than [CircleShape]. By default large FABs use this shape.
 */
// source:
@Immutable
class Shapes(
    // Shapes None and Full are omitted as None is a RectangleShape and Full is a CircleShape.
    val xSmall: CornerBasedShape = RoundedCornerShape(4.0.dp),
    val small: CornerBasedShape = RoundedCornerShape(8.0.dp),
    val medium: CornerBasedShape = RoundedCornerShape(12.0.dp),
    val large: CornerBasedShape = RoundedCornerShape(16.0.dp),
    val xLarge: CornerBasedShape = RoundedCornerShape(28.0.dp),
) {
    /** Returns a copy of this Shapes, optionally overriding some of the values. */
    fun copy(
        xSmall: CornerBasedShape = this.xSmall,
        small: CornerBasedShape = this.small,
        medium: CornerBasedShape = this.medium,
        large: CornerBasedShape = this.large,
        xLarge: CornerBasedShape = this.xLarge,
    ): Shapes = Shapes(
        xSmall = xSmall,
        small = small,
        medium = medium,
        large = large,
        xLarge = xLarge,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Shapes) return false
        if (xSmall != other.xSmall) return false
        if (small != other.small) return false
        if (medium != other.medium) return false
        if (large != other.large) return false
        if (xLarge != other.xLarge) return false
        return true
    }

    override fun hashCode(): Int {
        var result = xSmall.hashCode()
        result = 31 * result + small.hashCode()
        result = 31 * result + medium.hashCode()
        result = 31 * result + large.hashCode()
        result = 31 * result + xLarge.hashCode()
        return result
    }

    override fun toString(): String {
        return "Shapes(extraSmall=$xSmall, small=$small, medium=$medium, large=$large, extraLarge=$xLarge)"
    }
}

/** CompositionLocal used to specify the default shapes for the surfaces. */
internal val LocalShapes = staticCompositionLocalOf { Shapes() }