/*
 * Copyright 2025 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 26-01-2025.
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

@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.zs.compose.theme

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.OverlayClip
import androidx.compose.animation.SharedTransitionScope.PlaceHolderSize
import androidx.compose.animation.SharedTransitionScope.ResizeMode
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.ScaleToBounds
import androidx.compose.animation.SharedTransitionScope.SharedContentState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.Spring.StiffnessMediumLow
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.zs.compose.foundation.OrientRed
import com.zs.compose.foundation.SepiaBrown
import com.zs.compose.foundation.SignalWhite
import com.zs.compose.foundation.TrafficYellow
import com.zs.compose.foundation.UmbraGrey
import com.zs.compose.theme.text.ProvideTextStyle

// source: https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/MaterialTheme.kt;bpv=0
// commit date: 2024-09-26 23:43
@Composable
/*@VisibleForTesting*/
internal fun rememberTextSelectionColors(colorScheme: Colors): TextSelectionColors {
    val primaryColor = colorScheme.accent
    return remember(primaryColor) {
        TextSelectionColors(
            handleColor = primaryColor,
            backgroundColor = primaryColor.copy(alpha = TextSelectionBackgroundOpacity),
        )
    }
}

/*@VisibleForTesting*/
internal const val TextSelectionBackgroundOpacity = 0.4f

/**
 * Provides a [CompositionLocal] to access the current [SharedTransitionScope].
 *
 * This CompositionLocal should be provided bya parent composable that manages shared transitions.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
internal val LocalSharedTransitionScope =
    staticCompositionLocalOf<SharedTransitionScope> {
        error("CompositionLocal LocalSharedTransition not present")
    }

/**
 * Provides a[CompositionLocal] to access the current [AnimatedVisibilityScope].
 *
 * This CompositionLocal should be provided by a parent composable that manages animated visibility.
 */
val LocalNavAnimatedVisibilityScope =
    staticCompositionLocalOf<AnimatedVisibilityScope> { error("CompositionLocal LocalSharedTransition not present") }

/**
 * Contains functions to access the current theme values provided at the call site's position in the
 * hierarchy.
 */
object AppTheme {
    /**
     * Retrieves the current [Colors] at the call site's position in the hierarchy.
     *
     * @sample androidx.compose.material3.samples.ThemeColorSample
     */
    val colors: Colors
        @Composable @ReadOnlyComposable get() = LocalColors.current

    /**
     * Retrieves the current [Typography] at the call site's position in the hierarchy.
     *
     * @sample androidx.compose.material3.samples.ThemeTextStyleSample
     */
    val typography: Typography
        @Composable @ReadOnlyComposable get() = LocalTypography.current

    /**
     * Retrieves the current [Shapes] at the call site's position in the hierarchy.
     *
     * @sample androidx.compose.material3.samples.ThemeShapeSample
     */
    val shapes: Shapes
        @Composable @ReadOnlyComposable get() = LocalShapes.current

    /** Retrieves the current [SharedTransitionScope] at the call site's position in the hierarchy. */
    @OptIn(ExperimentalSharedTransitionApi::class)
    val sharedTransitionScope
        @Composable
        @ReadOnlyComposable
        get() = LocalSharedTransitionScope.current

    @OptIn(ExperimentalSharedTransitionApi::class)
    @Composable
    operator fun invoke(
        colors: Colors = AppTheme.colors,
        shapes: Shapes = AppTheme.shapes,
        typography: Typography = AppTheme.typography,
        content: @Composable () -> Unit
    ) {
        val rippleIndication = ripple()
        val selectionColors = rememberTextSelectionColors(colors)
        SharedTransitionLayout {
            CompositionLocalProvider(
                LocalColors provides colors,
                LocalIndication provides rippleIndication,
                LocalShapes provides shapes,
                LocalTextSelectionColors provides selectionColors,
                LocalTypography provides typography,
                LocalSharedTransitionScope provides this
            ) {
                ProvideTextStyle(value = typography.body1, content = content)
            }
        }
    }


    private val DefaultColorSpec = tween<Color>(AnimationConstants.DefaultDurationMillis)

    /**
     * Provides a composable function to set up the application's theme using the provided
     * colors, typography, and shapes.
     *
     * @param isLight  if true, applies the light theme.
     * @param fontFamily  the font family to be used in the theme.
     * @param accent  the accent color to be used in the theme.
     * @param content  the composable content to be displayed within the theme.
     */
    @Composable
    @Deprecated("Replace this with another version of AppTheme")
    operator fun invoke(
        isLight: Boolean,
        accent: Color = if (!isLight) Color.TrafficYellow else Color.SepiaBrown,
        fontFamily: FontFamily = FontFamily.Default,
        content: @Composable () -> Unit
    ) {
        val background by animateColorAsState(
            targetValue = when {
                !isLight -> Color(0xFF0E0E0F)
                else -> applyTonalElevation(accent, Color.White, 0.8.dp)
            },
            animationSpec = DefaultColorSpec, label = "background"
        )
        val primary by animateColorAsState(accent, DefaultColorSpec, "accent")
        val colors = Colors(
            accent = primary,
            background = background,
            onBackground = if (isLight) Color.UmbraGrey else Color.SignalWhite,
            onAccent = if (primary.luminance() > 0.45f) Color.Black else Color.SignalWhite,
            error = Color.OrientRed,
            onError = Color.SignalWhite,
        )

        invoke(
            colors = colors,
            typography = Typography(defaultFontFamily = fontFamily),
            content = content
        )
    }
}

private val DefaultSpring = spring(
    stiffness = StiffnessMediumLow,
    visibilityThreshold = Rect.VisibilityThreshold
)

@ExperimentalSharedTransitionApi
private val DefaultBoundsTransform = BoundsTransform { _, _ -> DefaultSpring }

@ExperimentalSharedTransitionApi
private val ParentClip: OverlayClip =
    object : OverlayClip {
        override fun getClipPath(
            state: SharedContentState,
            bounds: Rect,
            layoutDirection: LayoutDirection,
            density: Density
        ): Path? {
            return state.parentSharedContentState?.clipPathInOverlay
        }
    }

private val DefaultClipInOverlayDuringTransition: (LayoutDirection, Density) -> Path? =
    { _, _ -> null }

/**
 * @param renderInOverlay pass null to make this fun handle with default strategy.
 * @see androidx.compose.animation.SharedTransitionScope.renderInSharedTransitionScopeOverlay
 */
fun Modifier.renderInSharedTransitionScopeOverlay(
    zIndexInOverlay: Float = 0f,
    renderInOverlay: (() -> Boolean)? = null,
    clipInOverlayDuringTransition: (LayoutDirection, Density) -> Path? = DefaultClipInOverlayDuringTransition
) = composed {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    with(sharedTransitionScope) {
        renderInSharedTransitionScopeOverlay(
            renderInOverlay = renderInOverlay ?: { isTransitionActive },
            zIndexInOverlay = zIndexInOverlay,
            clipInOverlayDuringTransition = clipInOverlayDuringTransition
        )
    }
}

/**
 * @return the state of shared contnet corresponding to [key].
 * @see androidx.compose.animation.SharedTransitionScope.rememberSharedContentState
 */
@Composable
private inline fun rememberSharedContentState(key: Any) =
    with(AppTheme.sharedTransitionScope) {
        rememberSharedContentState(key = key)
    }

/**
 * A shared bounds modifier that uses scope from [AppTheme]'s [AppTheme.sharedTransitionScope] and
 * [AnimatedVisibilityScope] from [LocalNavAnimatedVisibilityScope]
 */
fun Modifier.sharedBounds(
    key: Any,
    enter: EnterTransition = fadeIn(),
    exit: ExitTransition = fadeOut(),
    boundsTransform: BoundsTransform = DefaultBoundsTransform,
    resizeMode: ResizeMode = ScaleToBounds(ContentScale.FillWidth, Alignment.Center),
    placeHolderSize: PlaceHolderSize = PlaceHolderSize.contentSize,
    renderInOverlayDuringTransition: Boolean = true,
    zIndexInOverlay: Float = 0f,
    clipInOverlayDuringTransition: OverlayClip = ParentClip
) = composed {
    val navAnimatedVisibilityScope = LocalNavAnimatedVisibilityScope.current
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val sharedContentState = rememberSharedContentState(key)
    with(sharedTransitionScope) {
        Modifier.sharedBounds(
            sharedContentState = sharedContentState,
            animatedVisibilityScope = navAnimatedVisibilityScope,
            enter = enter,
            exit = exit,
            boundsTransform = boundsTransform,
            resizeMode = resizeMode,
            placeHolderSize = placeHolderSize,
            renderInOverlayDuringTransition = renderInOverlayDuringTransition,
            zIndexInOverlay = zIndexInOverlay,
            clipInOverlayDuringTransition = clipInOverlayDuringTransition
        )
    }
}

/**
 * @see androidx.compose.animation.SharedTransitionScope.sharedElement
 */
@OptIn(ExperimentalSharedTransitionApi::class)
fun Modifier.sharedElement(
    key: Any,
    boundsTransform: BoundsTransform = DefaultBoundsTransform,
    placeHolderSize: PlaceHolderSize = PlaceHolderSize.contentSize,
    renderInOverlayDuringTransition: Boolean = true,
    zIndexInOverlay: Float = 0f,
    clipInOverlayDuringTransition: OverlayClip = ParentClip
) = composed {
    val sharedContentState = rememberSharedContentState(key = key)
    val navAnimatedVisibilityScope = LocalNavAnimatedVisibilityScope.current
    val sharedTransitionScope = LocalSharedTransitionScope.current
    with(sharedTransitionScope) {
        sharedElement(
            sharedContentState = sharedContentState,
            placeHolderSize = placeHolderSize,
            renderInOverlayDuringTransition = renderInOverlayDuringTransition,
            zIndexInOverlay = zIndexInOverlay,
            animatedVisibilityScope = navAnimatedVisibilityScope,
            boundsTransform = boundsTransform,
            clipInOverlayDuringTransition = clipInOverlayDuringTransition
        )
    }
}