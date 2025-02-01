/*
 * Copyright 2025 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 31-01-2025.
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

import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zs.compose.foundation.composableIf
import com.zs.compose.theme.internal.Icons
import com.zs.compose.theme.menu.DropDownMenu
import com.zs.compose.theme.menu.DropdownMenuItem
import com.zs.compose.theme.text.Label
import com.zs.compose.theme.text.OutlinedTextField
import com.zs.compose.theme.text.Text

private const val TAG = "Preference"

/**
 * The padding required to save oif user wants leading icon space to be reserved.
 */
val IconSpaceReserved = 12.dp

/**
 * A General [Preference] representation.
 * The basic building block that represents an individual setting displayed to a user in the preference hierarchy.
 *
 * @param text The text to be displayed as the title of this preference. It is allowed up to 4 lines, with the first line representing the title. Use bold for the first line and gray color for the summary.
 * @param modifier [Modifier] allows to modify the outer wrapper of this preference.
 * @param iconSpaceReserved [Boolean] Sets whether to reserve the space of this preference icon view when no icon is provided. If set to true, the preference will be offset as if it would have the icon and thus aligned with other preferences having icons.
 * @param icon [ImageVector] Sets the icon for this preference with an [ImageVector].
 * @param enabled [Boolean] Sets whether this preference should disable its view when it gets disabled. It also triggers the text to lose alpha.
 * @param widget Sets the layout for the controllable widget portion of this preference.
 * @param footer The content that is hide/show on user's request.
 * @param forceVisible If true, makes [footer] content, if available, always shown.
 */
@OptIn(ExperimentalThemeApi::class)
@Composable
fun Preference(
    text: CharSequence,
    modifier: Modifier = Modifier,
    iconSpaceReserved: Boolean = true,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    widget: @Composable (() -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
    forceVisible: Boolean = false,
) {
    // State to manage the focusable state of the footer content
    var focusable by remember { mutableStateOf(forceVisible) }
    val manager = LocalFocusManager.current
    // Determine the appropriate modifier based on the state of forceVisible, enabled, and footer
    val listModifier = when (forceVisible || !enabled || footer == null) {
        true -> modifier
        else -> {
            val requester = remember(::FocusRequester)
            Modifier
                .focusRequester(requester)
                .onFocusChanged { focusable = it.hasFocus }
                .focusTarget()
                // .pointerInput(Unit) { detectTapGestures {  } }
                .onPreviewKeyEvent {
                    Log.d(TAG, "Preference: $it")
                    if (it.key != Key.Back)
                        return@onPreviewKeyEvent false
                    manager.clearFocus()
                    true
                }
                .clickable { requester.requestFocus() }
                .then(modifier)
                .animateContentSize()
        }
    }
    // Main layout for the preference item
    BaseListItem(
        modifier = listModifier,
        trailing = widget,
        contentColor = AppTheme.colors.onBackground.copy(if (enabled) ContentAlpha.high else ContentAlpha.disabled),
        heading = {
            Text(
                text = text,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
                style = AppTheme.typography.body2
            )
        },
        // Display the icon if provided or
        // Preserve the space for the icon if requested
        leading = composableIf(icon != null || iconSpaceReserved) {
            when {
                // non-null write the icon.
                icon != null -> Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(top = 4.dp)
                )
                // preserve the space in case user requested.
                iconSpaceReserved -> Spacer(Modifier.padding(IconSpaceReserved))
            }
        },
        // Show footer content if available and focusable
        footer = composableIf(footer != null) {
            Crossfade(targetState = focusable, label = TAG) { value ->
                if (value) footer?.invoke()
            }
        }
    )
}

/**
 * Represents a switch [Preference]
 */
@Composable
@NonRestartableComposable
fun SwitchPreference(
    text: CharSequence,
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit),
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconSpaceReserved: Boolean = true,
    icon: ImageVector? = null,
) {
    Preference(
        modifier = modifier.clickable(enabled = enabled) {
            onCheckedChange(!checked)
        },
        text = text,
        enabled = enabled,
        iconSpaceReserved = iconSpaceReserved,
        icon = icon,
        widget = { Switch(enabled = enabled, checked = checked, onCheckedChange = null) },
    )
}

/**
 * Represents a checkbox [Preference]
 */
@Composable
@NonRestartableComposable
fun CheckBoxPreference(
    text: CharSequence,
    checked: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconSpaceReserved: Boolean = true,
    icon: ImageVector? = null,
    onCheckedChange: ((Boolean) -> Unit)
) {
    Preference(
        modifier = modifier.clickable(enabled = enabled) {
            onCheckedChange(!checked)
        },
        text = text,
        enabled = enabled,
        iconSpaceReserved = iconSpaceReserved,
        icon = icon,
        widget = {
            Checkbox(enabled = enabled, checked = checked, onCheckedChange = null)
        }
    )
}

/**
 * Represents a dropdown [Preference].
 * @param value  current selected value.
 * @param values  list of values.
 * @param entries  list of corresponding text entries. must be equal in size to [values]
 */
@Composable
@NonRestartableComposable
fun <T> DropDownPreference(
    value: T,
    text: CharSequence,
    modifier: Modifier = Modifier,
    onRequestChange: (T) -> Unit,
    enabled: Boolean = true,
    iconSpaceReserved: Boolean = true,
    icon: ImageVector? = null,
    values: Array<T>,
    entries: Array<CharSequence>,
) {
    require(entries.isNotEmpty() && values.size == entries.size)
    val (expanded, onDismissRequest) = remember { mutableStateOf(false) }
    val widget = @Composable {
        Box {
            // Icon
            Icon(
                imageVector = Icons.ArrowDropDown,
                contentDescription = null,
            )

            //
            val topValue = value
            DropDownMenu(
                expanded = expanded,
                onDismissRequest = { onDismissRequest(false) },
                content = {
                    values.forEachIndexed { index, value ->
                        val selected = value == topValue
                        DropdownMenuItem(
                            onClick = {
                                if (selected)
                                    return@DropdownMenuItem
                                onRequestChange(value);
                                onDismissRequest(false)
                            },
                            content = {
                                RadioButton(
                                    selected = selected,
                                    enabled = enabled,
                                    onValueChange = null
                                )

                                Text(
                                    text = entries[index],
                                    fontWeight = if (!selected) FontWeight.Normal else FontWeight.Bold,
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .weight(1f),
                                    maxLines = 2,
                                    color = if (selected) AppTheme.colors.accent else LocalContentColor.current
                                )
                            },
                            modifier = Modifier.defaultMinSize(minWidth = 180.dp, minHeight = 36.dp)
                        )
                    }
                },
               // elevation = 10.dp,
            )
        }
    }

    Preference(
        modifier = modifier.toggleable(
            expanded,
            enabled = enabled,
            onValueChange = onDismissRequest
        ),
        text = text,
        enabled = enabled,
        iconSpaceReserved = iconSpaceReserved,
        icon = icon,
        widget = widget
    )
}

@Composable
private fun TextButtons(
    modifier: Modifier = Modifier,
    onConfirmClick: () -> Unit,
    onCancelClick: () -> Unit
) {

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onCancelClick) {
            Label(text = stringResource(id = android.R.string.cancel))
        }
        TextButton(onClick = onConfirmClick) {
            Label(text = stringResource(id = android.R.string.ok))
        }
    }
}

/**
 * Represents a slider [Preference].
 */
@Composable
fun SliderPreference(
    text: CharSequence,
    value: Float,
    onRequestChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    steps: Int = 0,
    enabled: Boolean = true,
    iconSpaceReserved: Boolean = true,
    icon: ImageVector? = null,
    forceVisible: Boolean = false,
    preview: (@Composable (Float) -> Unit)? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
) {
    // This composable function defines a Preference that includes a Slider.
    // revealable is a composable function that displays the slider and buttons.
    // It is only invoked when the preference is expanded.
    // Place the Slider composable.
    val (state, onChange) = remember { mutableFloatStateOf(value) }
    val revealable =
        @Composable {
            val startPadding = (if (iconSpaceReserved) 24.dp + 16.dp else 0.dp) + 8.dp
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = startPadding)
            ) {
                Slider(
                    value = state,
                    onValueChange = onChange,
                    valueRange = valueRange,
                    steps = steps,
                )

                // Get the focus manager to control focus.
                // Display confirmation and cancellation buttons.
                // Clear focus when cancel/confirm is clicked, unless forceVisible is true.
                val manager = LocalFocusManager.current
                TextButtons(
                    onCancelClick = {
                        if (!forceVisible)
                            manager.clearFocus(true)
                    },
                    onConfirmClick = {
                        if (!forceVisible)
                            manager.clearFocus(true)
                        onRequestChange(state)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
// Display the main Preference composable.
    Preference(
        modifier = modifier,
        text = text,
        enabled = enabled,
        iconSpaceReserved = iconSpaceReserved,
        icon = icon,
        forceVisible = forceVisible,
        widget = composableIf(preview != null){
            preview?.invoke(state)
        },
        footer = revealable
    )
}

private val TextFieldShape = RoundedCornerShape(10)

/**
 * Represents a text field [Preference].
 */
@Composable
fun TextFieldPreference(
    text: CharSequence,
    state: TextFieldState,
    onConfirmClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconSpaceReserved: Boolean = true,
    icon: ImageVector? = null,
    forceVisible: Boolean = false,
    leadingFieldIcon: ImageVector? = null,
    label: CharSequence? = null,
    placeholder: CharSequence? = null,
    preview: (@Composable () -> Unit)? = null,
) {
    val manager = LocalFocusManager.current
    val revealable = @Composable {
        OutlinedTextField(
            state = state,
            modifier = Modifier
                .padding(start = (if (iconSpaceReserved) 24.dp + 8.dp else 8.dp) + 8.dp, end = 8.dp, top = 6.dp)
                .fillMaxWidth(),
            enabled = enabled,
            label = composableIf(label != null) {
                Label(text = label ?: "")
            },
            placeholder = composableIf(placeholder != null) {
                Text(text = placeholder ?: "")
            },
            shape = TextFieldShape,
            lineLimits = TextFieldLineLimits.SingleLine,
            leadingIcon = composableIf(leadingFieldIcon != null) {
                Icon(
                    imageVector = leadingFieldIcon!!,
                    contentDescription = null
                )
            },
            trailingIcon = {
                IconButton(
                    icon = Icons.Close,
                    contentDescription = null,
                    onClick = {
                        when {
                            state.text.isNotEmpty() -> state.clearText()
                            !forceVisible -> manager.clearFocus(true)
                        }
                    },
                    tint = AppTheme.colors.accent
                )
            },
            onKeyboardAction = { perform ->
                onConfirmClick(state.text.toString())
                if (!forceVisible)
                    manager.clearFocus(true)
                perform()
            },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
        )
    }
    // Actual Host
    Preference(
        modifier = modifier,
        text = text,
        enabled = enabled,
        iconSpaceReserved = iconSpaceReserved,
        icon = icon,
        forceVisible = forceVisible,
        widget = preview,
        footer = revealable
    )
}