/*
 * Copyright 2025 sheik
 *
 * Created by sheik on 03-05-2025.
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

package com.zs.compose.foundation

import android.annotation.SuppressLint
import androidx.annotation.FloatRange
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.isUnspecified


// **Rationale for Background Value Class**
//
// This value class is introduced to provide a dedicated and controlled way to manage background styling
// for composable components. The primary reason behind this design is to address the limitations
// encountered when directly manipulating the background properties of components that already have
// pre-defined behaviors (e.g., clipping, click actions).
//
// **Problem Addressed:**
// Directly modifying the background of such components would require users to manually re-apply these
// behaviors after changing the background, which is cumbersome and error-prone.
//
// **Solution:**
// By encapsulating background modifications within this value class, we can ensure that the correct
// order of operations is maintained. Using a value class ensures efficiency as there is no performance
// implications.
//
// **Why not pass a Modifier directly?**
// While it's technically feasible to pass a Modifier directly, it could lead to an awkward API. Having
// separate Modifiers for structural and visual concerns could be counterintuitive.
//
// **Future Possibilities:**
// This approach provides a solid foundation for introducing more advanced background effects, such as blur
// through haze. We will introduce these incrementally to avoid overwhelming users.

/**
 * A value class that wraps a [Modifier] and provides different ways to customize the appearance of a Composable.
 *
 * This class is used to encapsulate and optimize modifier chains for background and drawing operations.
 * It provides several convenient ways to create `Background` instances with different configuration options.
 *
 * @param modifier The [Modifier] to be applied to the Composable.
 *
 * @see Modifier
 * @see Color
 * @see Shape
 * @see DrawScope
 */
@ExperimentalFoundationApi
@JvmInline
@Stable
value class Background @PublishedApi internal constructor(val modifier: Modifier) {

    /**
     * Companion object to provide several overloads for constructing a [Background] instance.
     */
    @SuppressLint("ModifierFactoryExtensionFunction")
    companion object {

        /**
         * Creates a [Background] using a custom [Modifier] builder.
         *
         * This allows you to construct a [Background] modifier dynamically using the provided lambda block.
         *
         * @param builder A lambda function with a receiver of [Modifier] to build the desired modifier.
         * @return A new [Background] instance that wraps the generated [Modifier].
         */
        inline operator fun invoke(builder: Modifier.() -> Modifier) =
            Background(Modifier.builder())

        /**
         * Creates a [Background] with a solid [color] and an optional [shape].
         *
         * This overload allows you to specify a background color and a shape. By default, the shape is a
         * [RectangleShape], but it can be replaced with any other [Shape] like [CircleShape].
         *
         * @param color The [Color] to be used as the background color.
         * @param shape The [Shape] to be applied as the background shape. Defaults to [RectangleShape].
         * @return A new [Background] instance that applies the background with the specified color and shape.
         *
         */
        operator fun invoke(color: Color, shape: Shape = RectangleShape) = Background(
            builder = {
                if (color == Color.Transparent || color.isUnspecified) this else background(
                    color,
                    shape
                )
            }
        )

        operator fun invoke(
            brush: Brush,
            shape: Shape = RectangleShape,
            @FloatRange(from = 0.0, to = 1.0) alpha: Float = 1.0f
        ) = Background(
            builder = { background(brush, shape, alpha) }
        )
        /*
                */
        /**
         * Creates a [Background] that draws custom content behind the Composable.
         *
         * This allows you to use the [DrawScope] to custom-draw elements behind the Composable,
         * offering full flexibility for drawing operations, such as gradients or patterns.
         *
         * @param onDraw A lambda function with a receiver of [DrawScope] that is used to custom-draw behind the Composable.
         * @return A new [Background] instance that applies the custom drawing operation.
         *//*
        operator fun invoke(onDraw: DrawScope.() -> Unit) = Background(
            Modifier.drawBehind(onDraw)
        )*/
    }
}

