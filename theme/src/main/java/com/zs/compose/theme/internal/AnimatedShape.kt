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
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.ExperimentalThemeApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
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

/**
 * The shapes that will be used in buttons. Button will morph between these shapes depending on the
 * interaction of the button, assuming all of the shapes are [CornerBasedShape]s.
 *
 * @property Pair.first is the active shape.
 * @property Pair.second is the pressed shape.
 */
internal typealias ActivePressedButtonShape = Pair<Shape, Shape>

/**
 * Composable function that returns a shape that animates based on interactions.
 *
 * This function observes the provided [InteractionSource] and animates the shape
 * of a button between its default and pressed states. The animation uses the
 * default effects specification from the [AppTheme.motionScheme].
 *
 * If both the default and pressed shapes are not [RoundedCornerShape], the default
 * shape is returned directly without animation.
 *
 * A small delay is introduced when the interaction is not a press, which can help
 * in scenarios where a quick release might not be visually noticeable.
 *
 * @param source The [InteractionSource] to observe for interactions.
 * @return A [Shape] that animates based on the interaction state.
 *         If the shapes are not [RoundedCornerShape], it returns the default shape.
 */
@OptIn(ExperimentalThemeApi::class)
@Composable
internal fun ActivePressedButtonShape.shapeByInteraction(source: InteractionSource): Shape {
    // If both shapes are not RoundedCornerShape, return the default shape directly.
    if (first !is RoundedCornerShape && second !is RoundedCornerShape) return first

    // Get the default animation specifications for Float values from the motion scheme.
    val specs = AppTheme.motionScheme.defaultEffectsSpec<Float>()

    // Remember the AnimatedShapeState, which holds the current shape and animation spec.
    // This state is re-created if the animation spec changes.
    val state = remember(specs) {
        AnimatedShapeState(shape = first as RoundedCornerShape, spec = specs)
    }

    // Launch an effect that collects interactions from the InteractionSource.
    LaunchedEffect(source) {
        source.interactions.collect {
            // Determine if the interaction is a press event.
            val pressed = it is PressInteraction.Press

            // If the interaction is not a press, introduce a small delay.
            // This can help in scenarios where a quick release might not be visually noticeable.
            // TODO - Find how old version of Material ripple in compose does this.
            if (!pressed) {
                delay(100)
            }

            // Launch a new coroutine to animate the shape change.
            launch {
                // Animate to the pressedShape if pressed, otherwise animate to the default shape.
                // Both shapes are cast to CornerBasedShape as the animation logic expects it.
                state.animateToShape((if (pressed) second else first) as CornerBasedShape)
            }
        }
    }
    // Return an animated shape that updates based on the AnimatedShapeState.
    return rememberAnimatedShape(state)
}
