/*
 * Copyright 2025 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 25-05-2025.
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


import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Immutable
import com.zs.compose.theme.MotionScheme.Companion.expressive
import com.zs.compose.theme.MotionScheme.Companion.standard

// Copied
// https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/MotionScheme.kt;bpv=0
// on 25-05-2025

/**
 * A motion scheme provides all the [FiniteAnimationSpec]s for a [MaterialTheme].
 *
 * Motion schemes are designed to create a harmonious motion for components in the app.
 *
 * There are two built-in schemes, a [standard] and an [expressive], that can be used as-is or
 * customized.
 *
 * You can customize the motion scheme for all components in the [MaterialTheme].
 */
@ExperimentalThemeApi
@Immutable
interface MotionScheme {
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

        /**
         * Returns a standard Material motion scheme.
         *
         * The standard scheme is Material's basic motion scheme for utilitarian UI elements and
         * recurring interactions. It provides a linear motion feel.
         */
        @Suppress("UNCHECKED_CAST")
        @ExperimentalThemeApi
        fun standard(): MotionScheme = StandardMotionSchemeImpl

        /**
         * Returns an expressive Material motion scheme.
         *
         * The expressive scheme is Material's recommended motion scheme for prominent UI elements
         * and hero interactions. It provides a visually engaging motion feel.
         */
        @Suppress("UNCHECKED_CAST")
        @ExperimentalThemeApi
        fun expressive(): MotionScheme = ExpressiveMotionSchemeImpl
    }

    @ExperimentalThemeApi
    @Suppress("UNCHECKED_CAST")
    private object StandardMotionSchemeImpl : MotionScheme {
        // Copied from
        // https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/tokens/StandardMotionTokens.kt
        // on 25-05-2025
        private const val SpringDefaultSpatialDamping = 0.9f
        private const val SpringDefaultSpatialStiffness = 700.0f
        private const val SpringDefaultEffectsDamping = 1.0f
        private const val SpringDefaultEffectsStiffness = 1600.0f
        private const val SpringFastSpatialDamping = 0.9f
        private const val SpringFastSpatialStiffness = 1400.0f
        private const val SpringFastEffectsDamping = 1.0f
        private const val SpringFastEffectsStiffness = 3800.0f
        private const val SpringSlowSpatialDamping = 0.9f
        private const val SpringSlowSpatialStiffness = 300.0f
        private const val SpringSlowEffectsDamping = 1.0f
        private const val SpringSlowEffectsStiffness = 800.0f

        private val defaultSpatialSpec =
            spring<Any>(
                dampingRatio = SpringDefaultSpatialDamping,
                stiffness = SpringDefaultSpatialStiffness,
            )

        private val fastSpatialSpec =
            spring<Any>(
                dampingRatio = SpringFastSpatialDamping,
                stiffness = SpringFastSpatialStiffness,
            )

        private val slowSpatialSpec =
            spring<Any>(
                dampingRatio = SpringSlowSpatialDamping,
                stiffness = SpringSlowSpatialStiffness,
            )

        private val defaultEffectsSpec =
            spring<Any>(
                dampingRatio = SpringDefaultEffectsDamping,
                stiffness = SpringDefaultEffectsStiffness,
            )

        private val fastEffectsSpec =
            spring<Any>(
                dampingRatio = SpringFastEffectsDamping,
                stiffness = SpringFastEffectsStiffness,
            )

        private val slowEffectsSpec =
            spring<Any>(
                dampingRatio = SpringSlowEffectsDamping,
                stiffness = SpringSlowEffectsStiffness,
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

    @ExperimentalThemeApi
    @Suppress("UNCHECKED_CAST")
    private object ExpressiveMotionSchemeImpl : MotionScheme {

        // Copied from
        // https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/tokens/ExpressiveMotionTokens.kt
        // on 25-05-2025
        private const val SpringDefaultSpatialDamping = 0.8f
        private const val SpringDefaultSpatialStiffness = 380.0f
        private const val SpringDefaultEffectsDamping = 1.0f
        private const val SpringDefaultEffectsStiffness = 1600.0f
        private const val SpringFastSpatialDamping = 0.6f
        private const val SpringFastSpatialStiffness = 800.0f
        private const val SpringFastEffectsDamping = 1.0f
        private const val SpringFastEffectsStiffness = 3800.0f
        private const val SpringSlowSpatialDamping = 0.8f
        private const val SpringSlowSpatialStiffness = 200.0f
        private const val SpringSlowEffectsDamping = 1.0f
        private const val SpringSlowEffectsStiffness = 800.0f

        private val defaultSpatialSpec =
            spring<Any>(
                dampingRatio = SpringDefaultSpatialDamping,
                stiffness = SpringDefaultSpatialStiffness,

            )

        private val fastSpatialSpec =
            spring<Any>(
                dampingRatio = SpringFastSpatialDamping,
                stiffness = SpringFastSpatialStiffness,
            )

        private val slowSpatialSpec =
            spring<Any>(
                dampingRatio = SpringSlowSpatialDamping,
                stiffness = SpringSlowSpatialStiffness,
            )

        private val defaultEffectsSpec =
            spring<Any>(
                dampingRatio = SpringDefaultEffectsDamping,
                stiffness = SpringDefaultEffectsStiffness,
            )

        private val fastEffectsSpec =
            spring<Any>(
                dampingRatio = SpringFastEffectsDamping,
                stiffness = SpringFastEffectsStiffness,
            )

        private val slowEffectsSpec =
            spring<Any>(
                dampingRatio = SpringSlowEffectsDamping,
                stiffness = SpringSlowEffectsStiffness,
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
