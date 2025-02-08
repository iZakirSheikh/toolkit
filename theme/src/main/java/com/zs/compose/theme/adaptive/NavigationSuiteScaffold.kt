/*
 * Copyright 2025 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 08-02-2025.
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

package com.zs.compose.theme.adaptive

import androidx.annotation.FloatRange
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.LinearProgressIndicator
import com.zs.compose.theme.LocalContentColor
import com.zs.compose.theme.internal.Slot
import com.zs.compose.theme.internal.ZeroPadding
import com.zs.compose.theme.internal.component1
import com.zs.compose.theme.internal.component2
import com.zs.compose.theme.internal.component3
import com.zs.compose.theme.internal.component4
import com.zs.compose.theme.snackbar.SnackbarHost
import com.zs.compose.theme.snackbar.SnackbarHostState

private const val TAG = "NavigationSuitScaffold"

/** The standard spacing (in dp) applied between two consecutive items.*/
private val STANDARD_SPACING = 8.dp

/** The space around a fab*/
private val FAB_SPACING = 16.dp

/** Checks if this [Placeable] has zero width or zero height.*/
private val Placeable.isZeroSized get() = width == 0 || height == 0

private const val INDEX_CONTENT = 0
private const val INDEX_NAV_BAR = 1
private const val INDEX_SNACK_BAR = 2
private const val INDEX_FAB = 3
private const val INDEX_PROGRESS_BAR = 4

/**
 * A [MeasurePolicy] for the [NavigationSuiteScaffold] that arranges the content, navigation bar,
 * fab, and snackbar in a vertical layout.
 *
 * This policy positions the navigation bar at the bottom of the screen. Content is displayed above the
 * navigation bar and consumes the remaining available space. A fab is positioned above the
 * navigation bar according to [FabPosition]. Snackbar is displayed on top of all other elements,
 * ensuring they are always visible.
 *
 * @param fabPosition The [FabPosition] to determine the position of the fab.
 * @param insets The [WindowInsets] to be applied to the toast and floating widget to prevent them
 * from being obscured when the navigation bar is not visible.
 * @param onNewInsets A lambda function that receives the [PaddingValues] to be applied to the content,
 * ensuring it is not hidden behind the navigation bar.
 */
private class VerticalMeasurePolicy(
    private val fabPosition: FabPosition,
    private val insets: WindowInsets,
    private val onNewInsets: (PaddingValues) -> Unit
) : MeasurePolicy {
    override fun MeasureScope.measure(measurables: List<Measurable>, c: Constraints): MeasureResult {
        val (_, width, _, height) = c
        // Measure the size requirements of each child element, allowing
        // them to use the full width
        var constraints = c.copy(minHeight = 0)
        val snackBarPlaceable = measurables[INDEX_SNACK_BAR].measure(constraints)
        val progressBarPlaceable = measurables[INDEX_PROGRESS_BAR].measure(constraints)
        val navBarPlaceable = measurables[INDEX_NAV_BAR].measure(constraints)
        // Allow the pixel element to have a custom size by not constraining its minimum height
        constraints = c.copy(0, minHeight = 0)
        val fabPlaceable = measurables[INDEX_FAB].measure(constraints)
        constraints = c
        // Measure content against original constraints.
        // the content must fill the entire screen.
        val contentPlaceable = measurables[INDEX_CONTENT].measure(constraints)
        // Calculate the insets for the content.
        // and report through onNewIntent
        onNewInsets(PaddingValues(bottom = navBarPlaceable.height.toDp()))
        // Place the children in the parent layout
        return layout(width, height){
            // Place the main content at the top, filling the space up to the navigation bar
            contentPlaceable.placeRelative(0, 0)
            // Place navbar at the bottom of the screen.
            navBarPlaceable.placeRelative(0, height - navBarPlaceable.height)
            // Place progress bar at the very bottom of the screen, ignoring system insets
            // (it might overlap system bars if they are not colored)
            var x = width / 2 - progressBarPlaceable.width / 2
            var y = (height - progressBarPlaceable.height)
            progressBarPlaceable.placeRelative(x, y)
            // Add insets to pixel only if nav bar is hidden.
            val isNavBarHidden = navBarPlaceable.isZeroSized
            val insets =
                if (isNavBarHidden) insets.getBottom(density = this@measure) else 0
            // Place Toast at the centre bottom of the screen
            // remove nav bar offset from it.
            x = width / 2 - snackBarPlaceable.width / 2   // centre
            y =
                (height - navBarPlaceable.height - snackBarPlaceable.height - STANDARD_SPACING.roundToPx() - insets)
            // the snack-bar must be top of every composable.
            snackBarPlaceable.placeRelative(x, y, 1f)
            // place fab according to fabPosition.
            val fabSpacingPx = FAB_SPACING.roundToPx()
            fabPlaceable.placeRelative(
                y = contentPlaceable.height - navBarPlaceable.height - fabPlaceable.height - fabSpacingPx / 2,
                x = when (fabPosition) {
                    FabPosition.End -> contentPlaceable.width - fabPlaceable.width - fabSpacingPx
                    FabPosition.Center -> (contentPlaceable.width - fabPlaceable.width) / 2
                    FabPosition.Start -> fabSpacingPx
                    else -> error("Invalid fab position")
                }
            )
        }
    }
}

/**
 * A [MeasurePolicy] for the [NavigationSuiteScaffold] that arranges the content, navigation bar,
 * fab, and [SnackbarHost] in a horizontal layout.
 *
 * This policy positions the navigation bar to the side of the screen. The content is displayed next
 * to the navigation bar. The navigation bar can be a navigation rail or a wide navigation bar
 * depending on the user's configuration.
 *
 * In this layout, the [onNewInsets] lambda returns [ZeroPadding] because the width of the navigation
 * bar is already accounted for and deducted from the available space for the content.
 *
 * The floating widget and toast are positioned at the bottom center of the screen.
 *
 * @param fabPosition The [FabPosition] to determine the position of the fab.
 * @param insets The [WindowInsets] to be applied to the [Snackbar] and floating widget to prevent them
 * from being obscured when the navigation bar is not visible.
 * @param onNewInsets A lambda function that provides [PaddingValues] to the content. In this case,
 * it returns [EmptyInsets] as the navigation bar width is already excluded from the content's available space.
 */
private class HorizontalMeasurePolicy(
    private val fabPosition: FabPosition,
    private val insets: WindowInsets,
    private val onNewInsets: (PaddingValues) -> Unit
) : MeasurePolicy{
    override fun MeasureScope.measure(measurables: List<Measurable>, c: Constraints): MeasureResult {
        val (_, width, _, height) = c
        // Measure the size requirements of each child element
        // Allow the elements to have a custom size by not constraining them.
        var constraints = c.copy(minHeight = 0)
        val progressBarPlaceable = measurables[INDEX_PROGRESS_BAR].measure(constraints)
        constraints = c.copy(minHeight = 0, minWidth = 0)
        val snackBarPlaceable = measurables[INDEX_SNACK_BAR].measure(constraints)
        val navBarPlaceable = measurables[INDEX_NAV_BAR].measure(constraints)
        val fabPlaceable = measurables[INDEX_FAB].measure(constraints)
        // Calculate the width available for the main content, excluding the navigation bar
        val contentWidth = width - navBarPlaceable.width
        constraints = c.copy(minWidth = contentWidth, maxWidth = contentWidth)
        val contentPlaceable = measurables[INDEX_CONTENT].measure(constraints)
        // reset new insets
        onNewInsets(WindowInsets.ZeroPadding)
        // Place the children in the parent layout
        return layout(width, height){
            var x = 0
            var y = 0
            // place nav_bar from top at the start of the screen
            navBarPlaceable.placeRelative(x, y)
            x = navBarPlaceable.width
            // Place the main content at the top, after nav_bar width
            contentPlaceable.placeRelative(x, y)
            // Place progress bar at the very bottom of the screen, ignoring system insets
            // (it might overlap system bars if they are not colored)
            x = width / 2 - progressBarPlaceable.width / 2
            y = (height - progressBarPlaceable.height)
            progressBarPlaceable.placeRelative(x, y)
            // Place pixel above the system navigationBar at the centre of the screen.
            val insetBottom = insets.getBottom(density = this@measure)
            // Place SnackBar at the centre bottom of the screen
            // remove nav bar offset from it.
            x = width / 2 - snackBarPlaceable.width / 2   // centre
            // full height - toaster height - navbar - 16dp padding + navbar offset.
            y = (height - snackBarPlaceable.height - STANDARD_SPACING.roundToPx() - insetBottom)
            snackBarPlaceable.placeRelative(x, y, 1f)
            // place fab according to fabPosition.
            val fabSpacingPx = FAB_SPACING.roundToPx()
            fabPlaceable.placeRelative(
                y = height - fabPlaceable.height - fabSpacingPx,
                x = when (fabPosition) {
                    FabPosition.End -> width - fabPlaceable.width - fabSpacingPx
                    FabPosition.Center -> (width - fabPlaceable.width) / 2
                    FabPosition.Start -> navBarPlaceable.width + fabSpacingPx
                    else -> error("Invalid fab position")
                }
            )
        }
    }
}

/**
 * A flexible Scaffold for displaying content with a navigation bar, fab, a snackbar, and a
 * progress bar. Supports vertical and horizontal layouts, automatically managing window insets
 * to avoid overlap with system bars.
 *
 * **Vertical Layout:**
 * - Navigation bar at the bottom.
 * - "Widget", Snackbar, and ProgressBar also at the bottom.
 *
 * **Horizontal Layout:**
 * - Navigation rail (sidebar) instead of a bottom bar.
 * - "Window" and Snackbar at the bottom center.
 *
 * @param vertical `true` for vertical layout, `false` for horizontal.
 * @param content The screen's main content.
 * @param modifier Modifier for the Scaffold.
 * @param floatingActionButton A composable for the floating action button.
 * @param hideNavigationBar `true` to hide the navigation bar.
 * @param background Background color.
 * @param contentColor Content color.
 * @param snackbarHostState A [SnackbarHostState] for handling [com.zs.compose.theme.snackbar.Snackbar] messages.
 * @param progress Progress value (`Float.NaN` to hide, `-1f` for indeterminate, `0f - 1f` for determinate).
 * @param navBar Composable for the navigation bar or rail.
 */
@Composable
fun NavigationSuiteScaffold(
    vertical: Boolean,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    fabPosition: FabPosition = FabPosition.End,
    floatingActionButton: @Composable () -> Unit = {},
    background: Color = AppTheme.colors.background,
    contentColor: Color = AppTheme.colors.onBackground,
    snackbarHostState: SnackbarHostState = remember(::SnackbarHostState),
    @FloatRange(0.0, 1.0) progress: Float = Float.NaN,
    navBar: @Composable () -> Unit,
) {
    val (insets, onNewInsets) =
        remember { mutableStateOf(WindowInsets.ZeroPadding) }
    val navBarInsets = WindowInsets.navigationBars
    Layout(
        modifier = modifier
            .background(background)
            .fillMaxSize(),
        measurePolicy = remember(vertical, navBarInsets, fabPosition) {
            when {
                vertical -> VerticalMeasurePolicy(fabPosition, navBarInsets, onNewInsets)
                else -> HorizontalMeasurePolicy(fabPosition, navBarInsets, onNewInsets)
            }
        },
        content = {
            // Provide the content color for the main content
            CompositionLocalProvider(
                LocalContentColor provides contentColor,
                LocalContentInsets provides insets,
                content = content
            )
            // Conditionally display the navigation bar based on
            // 'hideNavigationBar'
            // Display the navigation bar (either bottom bar or navigation rail)
            // Don't show anything.
            Slot(navBar)
            // Display the SnackBar using the provided channel
            SnackbarHost(snackbarHostState)
            // Display the pixel element
            Slot(floatingActionButton)
            // Conditionally display the progress bar based on the 'progress' value
            // Show an indeterminate progress bar when progress is -1
            // Show a determinate progress bar when progress is between 0 and 1
            when {
                progress == -1f -> LinearProgressIndicator()
                !progress.isNaN() -> LinearProgressIndicator(progress = progress)
                else -> Spacer(modifier = Modifier)
            }
        }
    )
}