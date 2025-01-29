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

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.runtime.staticCompositionLocalOf

// source: https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/MotionScheme.kt
// commit date: 2022-08-01 20:47

/**
 * A motion provides all the [FiniteAnimationSpec]s for a [AppTheme].
 *
 * Motion schemes are designed to create a harmonious motion for components in the app.
 *
 * There are two built-in schemes, a [standard] and an [expressive], that can be used as-is or
 * customized.
 *
 * You can customize the motion scheme for all components in the [AppTheme].
 */
interface Motion {
    /**
     * A default spatial motion [FiniteAnimationSpec].
     *
     * This motion spec is designed to be applied to animations that may change the shape or bounds
     * of the component. For color or alpha animations use the `effects` equivalent which ensures a
     * "non-spatial" motion.
     *
     * [T] is the generic data type that will be animated by the system, as long as the appropriate
     * [TwoWayConverter] for converting the data to and from an [AnimationVector] is supplied.
     */
    fun <T> defaultSpatialSpec(): FiniteAnimationSpec<T>

    /**
     * A fast spatial motion [FiniteAnimationSpec].
     *
     * This motion spec is designed to be applied to animations that may change the shape or bounds
     * of the component. For color or alpha animations use the `effects` equivalent which ensures a
     * "non-spatial" motion.
     *
     * [T] is the generic data type that will be animated by the system, as long as the appropriate
     * [TwoWayConverter] for converting the data to and from an [AnimationVector] is supplied.
     */
    fun <T> fastSpatialSpec(): FiniteAnimationSpec<T>

    /**
     * A slow spatial motion [FiniteAnimationSpec].
     *
     * This motion spec is designed to be applied to animations that may change the shape or bounds
     * of the component. For color or alpha animations use the `effects` equivalent which ensures a
     * "non-spatial" motion.
     *
     * [T] is the generic data type that will be animated by the system, as long as the appropriate
     * [TwoWayConverter] for converting the data to and from an [AnimationVector] is supplied.
     */
    fun <T> slowSpatialSpec(): FiniteAnimationSpec<T>

    /**
     * A default effects motion [FiniteAnimationSpec].
     *
     * This motion spec is designed to be applied to animations that do not change the shape or
     * bounds of the component. For example, color animation.
     *
     * [T] is the generic data type that will be animated by the system, as long as the appropriate
     * [TwoWayConverter] for converting the data to and from an [AnimationVector] is supplied.
     */
    fun <T> defaultEffectsSpec(): FiniteAnimationSpec<T>

    /**
     * A fast effects motion [FiniteAnimationSpec].
     *
     * This motion spec is designed to be applied to animations that do not change the shape or
     * bounds of the component. For example, color animation.
     *
     * [T] is the generic data type that will be animated by the system, as long as the appropriate
     * [TwoWayConverter] for converting the data to and from an [AnimationVector] is supplied.
     */
    fun <T> fastEffectsSpec(): FiniteAnimationSpec<T>

    /**
     * A slow effects motion [FiniteAnimationSpec].
     *
     * This motion spec is designed to be applied to animations that do not change the shape or
     * bounds of the component. For example, color animation.
     *
     * [T] is the generic data type that will be animated by the system, as long as the appropriate
     * [TwoWayConverter] for converting the data to and from an [AnimationVector] is supplied.
     */
    fun <T> slowEffectsSpec(): FiniteAnimationSpec<T>

    companion object {

        val EasingEmphasizedCubicBezier = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
        val EasingEmphasizedAccelerateCubicBezier = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
        val EasingEmphasizedDecelerateCubicBezier = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
        val EasingLegacyCubicBezier = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
        val EasingLegacyAccelerateCubicBezier = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)
        val EasingLegacyDecelerateCubicBezier = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
        val EasingLinearCubicBezier = CubicBezierEasing(0.0f, 0.0f, 1.0f, 1.0f)
        val EasingStandardCubicBezier = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
        val EasingStandardAccelerateCubicBezier = CubicBezierEasing(0.3f, 0.0f, 1.0f, 1.0f)
        val EasingStandardDecelerateCubicBezier = CubicBezierEasing(0.0f, 0.0f, 0.0f, 1.0f)
        
        /**
         * Returns a standard Material motion scheme.
         *
         * The standard scheme is Material's basic motion scheme for utilitarian UI elements and
         * recurring interactions. It provides a linear motion feel.
         */
        @Suppress("UNCHECKED_CAST")
        fun standard(): Motion =
            object : Motion {
                private val defaultSpatialSpec =
                    spring<Any>(
                        dampingRatio = 0.9f,
                        stiffness = 700.0f
                    )

                private val fastSpatialSpec =
                    spring<Any>(
                        dampingRatio = 0.9f,
                        stiffness = 1400.0f
                    )

                private val slowSpatialSpec =
                    spring<Any>(
                        dampingRatio = 0.9f,
                        stiffness = 300.0f
                    )

                private val defaultEffectsSpec =
                    spring<Any>(
                        dampingRatio = 1.0f,
                        stiffness = 1600.0f
                    )

                private val fastEffectsSpec =
                    spring<Any>(
                        dampingRatio = 1.0f,
                        stiffness = 3800.0f
                    )

                private val slowEffectsSpec =
                    spring<Any>(
                        dampingRatio = 1.0f,
                        stiffness = 800.0f
                    )

                override fun <T> defaultSpatialSpec(): FiniteAnimationSpec<T> {
                    return defaultSpatialSpec as FiniteAnimationSpec<T>
                }

                override fun <T> fastSpatialSpec(): FiniteAnimationSpec<T> {
                    return fastSpatialSpec as FiniteAnimationSpec<T>
                }

                override fun <T> slowSpatialSpec(): FiniteAnimationSpec<T> {
                    return slowSpatialSpec as FiniteAnimationSpec<T>
                }

                override fun <T> defaultEffectsSpec(): FiniteAnimationSpec<T> {
                    return defaultEffectsSpec as FiniteAnimationSpec<T>
                }

                override fun <T> fastEffectsSpec(): FiniteAnimationSpec<T> {
                    return fastEffectsSpec as FiniteAnimationSpec<T>
                }

                override fun <T> slowEffectsSpec(): FiniteAnimationSpec<T> {
                    return slowEffectsSpec as FiniteAnimationSpec<T>
                }
            }

        /**
         * Returns an expressive Material motion scheme.
         *
         * The expressive scheme is Material's recommended motion scheme for prominent UI elements
         * and hero interactions. It provides a visually engaging motion feel.
         */
        @Suppress("UNCHECKED_CAST")
        fun expressive(): Motion =
            object : Motion {

                private val defaultSpatialSpec =
                    spring<Any>(
                        dampingRatio = 0.8f,
                        stiffness = 380.0f
                    )

                private val fastSpatialSpec =
                    spring<Any>(
                        dampingRatio = 0.6f,
                        stiffness = 800.0f
                    )

                private val slowSpatialSpec =
                    spring<Any>(
                        dampingRatio = 0.8f,
                        stiffness = 200.0f
                    )

                private val defaultEffectsSpec =
                    spring<Any>(
                        dampingRatio = 1.0f,
                        stiffness = 1600.0f
                    )

                private val fastEffectsSpec =
                    spring<Any>(
                        dampingRatio = 1.0f,
                        stiffness = 3800.0f
                    )

                private val slowEffectsSpec =
                    spring<Any>(
                        dampingRatio = 1.0f,
                        stiffness = 800.0f
                    )

                override fun <T> defaultSpatialSpec(): FiniteAnimationSpec<T> {
                    return defaultSpatialSpec as FiniteAnimationSpec<T>
                }

                override fun <T> fastSpatialSpec(): FiniteAnimationSpec<T> {
                    return fastSpatialSpec as FiniteAnimationSpec<T>
                }

                override fun <T> slowSpatialSpec(): FiniteAnimationSpec<T> {
                    return slowSpatialSpec as FiniteAnimationSpec<T>
                }

                override fun <T> defaultEffectsSpec(): FiniteAnimationSpec<T> {
                    return defaultEffectsSpec as FiniteAnimationSpec<T>
                }

                override fun <T> fastEffectsSpec(): FiniteAnimationSpec<T> {
                    return fastEffectsSpec as FiniteAnimationSpec<T>
                }

                override fun <T> slowEffectsSpec(): FiniteAnimationSpec<T> {
                    return slowEffectsSpec as FiniteAnimationSpec<T>
                }
            }
    }
}

/**
 * CompositionLocal used to pass [Motion] down the tree.
 *
 * Setting the value here is typically done as part of [AppTheme]. To retrieve the current
 * value of this CompositionLocal, use [AppTheme.motion].
 */
internal val LocalMotion = staticCompositionLocalOf { Motion.standard() }