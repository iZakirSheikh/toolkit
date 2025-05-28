/*
 * Copyright 2025 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 28-05-2025.
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

package com.zs.compose.theme.internal

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

// Copied from
// https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/internal/AnimatedShape.kt

@Stable
internal class AnimatedShapeState(
    val shape: RoundedCornerShape,
    val spec: FiniteAnimationSpec<Float>,
) {
    var size: Size = Size.Zero
    var density: Density = Density(0f, 0f)

    private var topStart: Animatable<Float, AnimationVector1D>? = null

    private var topEnd: Animatable<Float, AnimationVector1D>? = null

    private var bottomStart: Animatable<Float, AnimationVector1D>? = null

    private var bottomEnd: Animatable<Float, AnimationVector1D>? = null

    fun topStart(size: Size = this.size, density: Density = this.density): Float {
        return (topStart ?: Animatable(shape.topStart.toPx(size, density)).also { topStart = it })
            .value
    }

    fun topEnd(size: Size = this.size, density: Density = this.density): Float {
        return (topEnd ?: Animatable(shape.topEnd.toPx(size, density)).also { topEnd = it }).value
    }

    fun bottomStart(size: Size = this.size, density: Density = this.density): Float {
        return (bottomStart
            ?: Animatable(shape.bottomStart.toPx(size, density)).also { bottomStart = it })
            .value
    }

    fun bottomEnd(size: Size = this.size, density: Density = this.density): Float {
        return (bottomEnd
            ?: Animatable(shape.bottomEnd.toPx(size, density)).also { bottomEnd = it })
            .value
    }

    suspend fun animateToShape(shape: CornerBasedShape) = coroutineScope {
        launch { topStart?.animateTo(shape.topStart.toPx(size, density), spec) }
        launch { topEnd?.animateTo(shape.topEnd.toPx(size, density), spec) }
        launch { bottomStart?.animateTo(shape.bottomStart.toPx(size, density), spec) }
        launch { bottomEnd?.animateTo(shape.bottomEnd.toPx(size, density), spec) }
    }
}

@Composable
internal fun rememberAnimatedShape(state: AnimatedShapeState): Shape {
    val density = LocalDensity.current
    state.density = density
    return remember(density, state) {
        object : Shape {
            var clampedRange = 0f..1f
            override fun createOutline(
                size: Size,
                layoutDirection: LayoutDirection,
                density: Density,
            ): Outline {
                state.size = size

                clampedRange = 0f..size.height / 2
                return RoundedCornerShape(
                    topStart = state.topStart().coerceIn(clampedRange),
                    topEnd = state.topEnd().coerceIn(clampedRange),
                    bottomStart = state.bottomStart().coerceIn(clampedRange),
                    bottomEnd = state.bottomEnd().coerceIn(clampedRange),
                )
                    .createOutline(size, layoutDirection, density)
            }
        }
    }
}