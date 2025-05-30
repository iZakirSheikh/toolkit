/*
 * Copyright 2025 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 30-05-2025.
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

@file:OptIn(ExperimentalFoundationApi::class)

package com.zs.compose.theme.menu

import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toComposeIntRect
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.PopupProperties
import com.zs.compose.foundation.Background
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.ExperimentalThemeApi
import com.zs.compose.theme.contentColorFor
import com.zs.compose.theme.internal.rememberAccessibilityServiceState
import kotlin.math.max
import kotlin.math.roundToInt
import android.graphics.Rect as ViewRect

/**
 * [Material Design exposed dropdown menu](https://m3.material.io/components/menus/overview)
 *
 * Menus display a list of choices on a temporary surface. They appear when users interact with a
 * button, action, or other control.
 *
 * Exposed dropdown menus, sometimes also called "spinners" or "combo boxes", display the currently
 * selected item in a text field to which the menu is anchored. In some cases, it can accept and
 * display user input (whether or not it’s listed as a menu choice), in which case it may be used to
 * implement autocomplete.
 *
 * ![Exposed dropdown menu
 * image](https://developer.android.com/images/reference/androidx/compose/material3/exposed-dropdown-menu.png)
 *
 * The [ExposedDropdownMenuBox] is expected to contain a [TextField] (or [OutlinedTextField]) and
 * [ExposedDropdownMenu][ExposedDropdownMenuBoxScope.ExposedDropdownMenu] as content. The
 * [menuAnchor][ExposedDropdownMenuBoxScope.menuAnchor] modifier should be passed to the text field.
 *
 * An example of a read-only Exposed Dropdown Menu:
 *
 * @sample androidx.compose.material3.samples.ExposedDropdownMenuSample
 *
 * An example of an editable Exposed Dropdown Menu:
 *
 * @sample androidx.compose.material3.samples.EditableExposedDropdownMenuSample
 *
 * An example of an editable Exposed Dropdown Menu used like a MultiAutoCompleteTextView:
 *
 * @sample androidx.compose.material3.samples.MultiAutocompleteExposedDropdownMenuSample
 * @param expanded whether the menu is expanded or not
 * @param onExpandedChange called when the exposed dropdown menu is clicked and the expansion state
 *   changes.
 * @param modifier the [Modifier] to be applied to this ExposedDropdownMenuBox
 * @param content the content of this ExposedDropdownMenuBox, typically a [TextField] and an
 *   [ExposedDropdownMenu][ExposedDropdownMenuBoxScope.ExposedDropdownMenu].
 */
@ExperimentalThemeApi
@Composable
fun ExposedDropdownMenuBox(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ExposedDropdownMenuBoxScope.() -> Unit,
) {
    val windowBoundsCalculator = platformWindowBoundsCalculator()
    val density = LocalDensity.current

    val verticalMargin = with(density) { MenuVerticalMargin.roundToPx() }

    var anchorCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var anchorWidth by remember { mutableIntStateOf(0) }
    var menuMaxHeight by remember { mutableIntStateOf(0) }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val expandedDescription = /*getString(Strings.MenuExpanded)*/ "Menu expanded"
    val collapsedDescription = /*getString(Strings.MenuCollapsed)*/ "Menu collapsed"
    val toggleDescription = /*getString(Strings.ToggleDropdownMenu)*/ "Toggle dropdown menu"
    val anchorTypeState = remember {
        mutableStateOf(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
    }

    val scope =
        remember(expanded, onExpandedChange, windowBoundsCalculator, density) {
            object : ExposedDropdownMenuBoxScopeImpl() {
                override fun Modifier.menuAnchor(
                    type: ExposedDropdownMenuAnchorType,
                    enabled: Boolean,
                ): Modifier =
                    this.focusRequester(focusRequester)
                        .then(
                            ExposedDropdownMenuAnchorElement {
                                if (type.hasGreaterOrEqualPriorityThan(anchorTypeState.value)) {
                                    anchorTypeState.value = type
                                }
                            }
                        )
                        .then(
                            if (!enabled) Modifier
                            else
                                Modifier.expandable(
                                    expanded = expanded,
                                    onExpandedChange = {
                                        anchorTypeState.value = type
                                        onExpandedChange(!expanded)
                                    },
                                    anchorType = type,
                                    expandedDescription = expandedDescription,
                                    collapsedDescription = collapsedDescription,
                                    toggleDescription = toggleDescription,
                                    keyboardController = keyboardController,
                                )
                        )

                override val anchorType: ExposedDropdownMenuAnchorType
                    get() = anchorTypeState.value

                override fun Modifier.exposedDropdownSize(matchAnchorWidth: Boolean): Modifier =
                    layout { measurable, constraints ->
                        val menuWidth = constraints.constrainWidth(anchorWidth)
                        val menuConstraints =
                            constraints.copy(
                                maxHeight = constraints.constrainHeight(menuMaxHeight),
                                minWidth =
                                    if (matchAnchorWidth) menuWidth else constraints.minWidth,
                                maxWidth = if (matchAnchorWidth) menuWidth else constraints.maxWidth,
                            )
                        val placeable = measurable.measure(menuConstraints)
                        layout(placeable.width, placeable.height) { placeable.place(0, 0) }
                    }
            }
        }

    Box(
        modifier.onGloballyPositioned {
            anchorCoordinates = it
            anchorWidth = it.size.width
            menuMaxHeight =
                calculateMaxHeight(
                    windowBounds = windowBoundsCalculator.getVisibleWindowBounds(),
                    anchorBounds = anchorCoordinates.getAnchorBounds(),
                    verticalMargin = verticalMargin,
                )
        }
    ) {
        scope.content()
    }

    if (expanded) {
        OnPlatformWindowBoundsChange {
            menuMaxHeight =
                calculateMaxHeight(
                    windowBounds = windowBoundsCalculator.getVisibleWindowBounds(),
                    anchorBounds = anchorCoordinates.getAnchorBounds(),
                    verticalMargin = verticalMargin,
                )
        }
    }

    SideEffect { if (expanded) focusRequester.requestFocus() }

    // Back events are handled in the Popup layer if the menu is focusable.
    // If it's not focusable, we handle them here.
    //BackHandler(enabled = expanded) { onExpandedChange(false) }
}

/** Scope for [ExposedDropdownMenuBox]. */
@ExperimentalThemeApi
sealed class ExposedDropdownMenuBoxScope {
    /**
     * Modifier which should be applied to an element inside the [ExposedDropdownMenuBoxScope],
     * typically a text field or an icon within the text field. It's responsible for requesting
     * focus, and if [enabled], expanding/collapsing the menu on click and applying semantics.
     *
     * @param type the type of element that is anchoring the menu. See
     *   [ExposedDropdownMenuAnchorType].
     * @param enabled whether controlling the menu via this anchor is enabled. When `false`, the
     *   component will not expand or collapse the menu in response to user input, and menu
     *   semantics will be invisible to accessibility services. Note that this value only controls
     *   interactions with the menu. It does not affect the enabled state of other kinds of
     *   interactions, such as [TextField]'s `enabled` parameter.
     */
    abstract fun Modifier.menuAnchor(
        type: ExposedDropdownMenuAnchorType,
        enabled: Boolean = true,
    ): Modifier

    /**
     * Modifier which should be applied to a menu placed inside the [ExposedDropdownMenuBoxScope].
     * It will set constraints on the width and height of the menu so it will not overlap the text
     * field or software keyboard.
     *
     * [ExposedDropdownMenu] applies this modifier automatically, so this is only needed when using
     * custom menu components.
     *
     * @param matchAnchorWidth whether the menu's width should be forcefully constrained to match
     *   the width of the text field to which it's attached.
     */
    abstract fun Modifier.exposedDropdownSize(matchAnchorWidth: Boolean = true): Modifier

    internal abstract val anchorType: ExposedDropdownMenuAnchorType

    /**
     * Popup which contains content for Exposed Dropdown Menu. Should be used inside the content of
     * [ExposedDropdownMenuBox].
     *
     * @param expanded whether the menu is expanded
     * @param onDismissRequest called when the user requests to dismiss the menu, such as by tapping
     *   outside the menu's bounds
     * @param modifier the [Modifier] to be applied to this menu
     * @param scrollState a [ScrollState] used by the menu's content for items vertical scrolling
     * @param matchAnchorWidth whether the menu's width should be forcefully constrained to match
     *   the width of the text field to which it's attached.
     * @param shape the shape of the menu
     * @param containerColor the container color of the menu
     * @param tonalElevation when [containerColor] is [ColorScheme.surface], a translucent primary
     *   color overlay is applied on top of the container. A higher tonal elevation value will
     *   result in a darker color in light theme and lighter color in dark theme. See also:
     *   [Surface].
     * @param shadowElevation the elevation for the shadow below the menu
     * @param border the border to draw around the container of the menu. Pass `null` for no border.
     * @param content the content of the menu
     */
    @Composable
    @NonRestartableComposable
    fun ExposedDropdownMenu(
        expanded: Boolean,
        onDismissRequest: () -> Unit,
        modifier: Modifier = Modifier,
        scrollState: ScrollState? = null,
        matchAnchorWidth: Boolean = true,
        shape: Shape = AppTheme.shapes.small,
        containerColor: Color = AppTheme.colors.background(1.dp),
        contentColor: Color = contentColorFor(containerColor),
        elevation: Dp = Dp.Unspecified,
        border: BorderStroke? = null,
        content: @Composable ColumnScope.() -> Unit,
    ) = DropDownMenu(
        expanded = expanded, onDismissRequest = onDismissRequest,
        scrollState = scrollState,
        elevation = elevation,
        background = Background(containerColor),
        shape = shape,
        contentColor = contentColor,
        border = border,
        modifier = modifier.exposedDropdownSize(matchAnchorWidth),
        properties = popupPropertiesForAnchorType(anchorType),
        content = content
    )
}

// Sealed classes don't allow dynamic anonymous subclasses.
@OptIn(ExperimentalThemeApi::class)
private abstract class ExposedDropdownMenuBoxScopeImpl : ExposedDropdownMenuBoxScope()

/** The type of element that can serve as a dropdown menu anchor. */
@JvmInline
value class ExposedDropdownMenuAnchorType private constructor(private val name: String) {
    companion object {
        /**
         * A non-editable primary anchor of the dropdown menu, such as a read-only text field.
         *
         * An anchor of this type will open the menu with focus.
         */
        val PrimaryNotEditable = ExposedDropdownMenuAnchorType("PrimaryNotEditable")

        /**
         * An editable primary anchor of the dropdown menu, such as a text field that allows user
         * input.
         *
         * An anchor of this type will open the menu without focus in order to preserve focus on the
         * soft keyboard (IME).
         */
        val PrimaryEditable = ExposedDropdownMenuAnchorType("PrimaryEditable")

        /**
         * A secondary anchor of the dropdown menu that lives alongside an editable primary anchor,
         * such as an icon within an editable text field.
         *
         * If accessibility services are enabled, an anchor of this type will open the menu with
         * focus. Otherwise, the menu is opened without focus in order to preserve focus on the soft
         * keyboard (IME).
         */
        val SecondaryEditable = ExposedDropdownMenuAnchorType("SecondaryEditable")
    }

    override fun toString(): String = name
}

private fun ExposedDropdownMenuAnchorType.hasGreaterOrEqualPriorityThan(
    that: ExposedDropdownMenuAnchorType
): Boolean =
    when (this) {
        ExposedDropdownMenuAnchorType.PrimaryNotEditable,
        ExposedDropdownMenuAnchorType.PrimaryEditable -> true
        ExposedDropdownMenuAnchorType.SecondaryEditable ->
            that == ExposedDropdownMenuAnchorType.SecondaryEditable
        else -> false
    }

private class ExposedDropdownMenuAnchorElement(val updateStateOnAttach: () -> Unit) :
    ModifierNodeElement<ExposedDropdownMenuAnchorNode>() {
    override fun create() = ExposedDropdownMenuAnchorNode(updateStateOnAttach)

    override fun update(node: ExposedDropdownMenuAnchorNode) {
        node.updateStateOnAttach = updateStateOnAttach
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "exposedDropdownMenuAnchorType"
        properties["updateStateOnAttach"] = updateStateOnAttach
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ExposedDropdownMenuAnchorElement) return false

        return updateStateOnAttach === other.updateStateOnAttach
    }

    override fun hashCode(): Int {
        return updateStateOnAttach.hashCode()
    }
}

private class ExposedDropdownMenuAnchorNode(var updateStateOnAttach: () -> Unit) : Modifier.Node() {
    override fun onAttach() {
        updateStateOnAttach()
    }
}

private fun Modifier.expandable(
    expanded: Boolean,
    onExpandedChange: () -> Unit,
    anchorType: ExposedDropdownMenuAnchorType,
    expandedDescription: String,
    collapsedDescription: String,
    toggleDescription: String,
    keyboardController: SoftwareKeyboardController?,
) =
    pointerInput(onExpandedChange) {
        awaitEachGesture {
            // Modifier.clickable doesn't work for text fields, so we use Modifier.pointerInput
            // in the Initial pass to observe events before the text field consumes them
            // in the Main pass.
            val downEvent = awaitFirstDown(pass = PointerEventPass.Initial)
            if (anchorType == ExposedDropdownMenuAnchorType.SecondaryEditable) {
                downEvent.consume()
            }
            val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
            if (upEvent != null) {
                onExpandedChange()
            }
        }
    }
        .semantics {
            if (anchorType == ExposedDropdownMenuAnchorType.SecondaryEditable) {
                role = Role.Button
                stateDescription = if (expanded) expandedDescription else collapsedDescription
                contentDescription = toggleDescription
            } else {
                role = Role.DropdownList
            }
            onClick {
                onExpandedChange()
                if (anchorType == ExposedDropdownMenuAnchorType.PrimaryEditable) {
                    keyboardController?.show()
                }
                true
            }
        }

private fun calculateMaxHeight(
    windowBounds: IntRect,
    anchorBounds: Rect?,
    verticalMargin: Int,
): Int {
    anchorBounds ?: return 0

    val marginedWindowTop = windowBounds.top + verticalMargin
    val marginedWindowBottom = windowBounds.bottom - verticalMargin
    val availableHeight =
        if (anchorBounds.top > windowBounds.bottom || anchorBounds.bottom < windowBounds.top) {
            marginedWindowBottom - marginedWindowTop
        } else {
            val heightAbove = anchorBounds.top - marginedWindowTop
            val heightBelow = marginedWindowBottom - anchorBounds.bottom
            max(heightAbove, heightBelow).roundToInt()
        }

    return max(availableHeight, 0)
}

// Don't use `boundsInWindow()` because it can report 0 when the window is animating/resizing
private fun LayoutCoordinates?.getAnchorBounds(): Rect =
    if (this == null || !this.isAttached) Rect.Zero else Rect(positionInWindow(), size.toSize())

private class WindowBoundsCalculator(private val view: View) {
    fun getVisibleWindowBounds(): IntRect = view.getWindowBounds()
}

@Composable
private fun platformWindowBoundsCalculator(): WindowBoundsCalculator {
    val config = LocalConfiguration.current
    val view = LocalView.current
    return remember(config, view) { WindowBoundsCalculator(view) }
}

@Composable
private fun OnPlatformWindowBoundsChange(block: () -> Unit) {
    val view = LocalView.current
    val density = LocalDensity.current
    SoftKeyboardListener(view, density, block)
}

@Composable
private fun popupPropertiesForAnchorType(
    anchorType: ExposedDropdownMenuAnchorType
): PopupProperties {
    val a11yServicesEnabled by rememberAccessibilityServiceState()
    var flags =
        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM

    // In order for a11y focus to jump to the menu when opened, it needs to be
    // focusable and touch modal (NOT_FOCUSABLE and NOT_TOUCH_MODAL are *not* set).
    if (!a11yServicesEnabled) {
        flags = flags or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
    }
    // If typing on the IME is required, the menu should not be focusable
    // in order to prevent stealing focus from the input method.
    val imeRequired =
        anchorType == ExposedDropdownMenuAnchorType.PrimaryEditable ||
                (anchorType == ExposedDropdownMenuAnchorType.SecondaryEditable && !a11yServicesEnabled)
    if (imeRequired) {
        flags = flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
    }

    return PopupProperties(flags = flags)
}

@Composable
private fun SoftKeyboardListener(
    view: View,
    density: Density,
    onKeyboardVisibilityChange: () -> Unit,
) {
    // It would be easier to listen to WindowInsets.ime, but that doesn't work with
    // `setDecorFitsSystemWindows(window, true)`. Instead, listen to the view tree's global layout.
    DisposableEffect(view, density) {
        val listener =
            object : View.OnAttachStateChangeListener, ViewTreeObserver.OnGlobalLayoutListener {
                private var isListeningToGlobalLayout = false

                init {
                    view.addOnAttachStateChangeListener(this)
                    registerOnGlobalLayoutListener()
                }

                override fun onViewAttachedToWindow(p0: View) = registerOnGlobalLayoutListener()

                override fun onViewDetachedFromWindow(p0: View) = unregisterOnGlobalLayoutListener()

                override fun onGlobalLayout() = onKeyboardVisibilityChange()

                private fun registerOnGlobalLayoutListener() {
                    if (isListeningToGlobalLayout || !view.isAttachedToWindow) return
                    view.viewTreeObserver.addOnGlobalLayoutListener(this)
                    isListeningToGlobalLayout = true
                }

                private fun unregisterOnGlobalLayoutListener() {
                    if (!isListeningToGlobalLayout) return
                    view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    isListeningToGlobalLayout = false
                }

                fun dispose() {
                    unregisterOnGlobalLayoutListener()
                    view.removeOnAttachStateChangeListener(this)
                }
            }

        onDispose { listener.dispose() }
    }
}

private fun View.getWindowBounds(): IntRect =
    ViewRect().let {
        this.getWindowVisibleDisplayFrame(it)
        it.toComposeIntRect()
    }