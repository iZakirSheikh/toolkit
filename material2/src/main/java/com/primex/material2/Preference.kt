@file:OptIn(ExperimentalToolkitApi::class, ExperimentalMaterialApi::class)

package com.primex.material2

import android.R
import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Checkbox
import androidx.compose.material.ContentAlpha
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.RadioButton
import androidx.compose.material.Slider
import androidx.compose.material.Switch
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.primex.core.ExperimentalToolkitApi
import com.primex.core.composableOrNull
import com.primex.material2.menu.DropDownMenu2

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
    ListTile(
        modifier = listModifier,
        trailing = widget,
        onColor = MaterialTheme.colors.onBackground.copy(if (enabled) ContentAlpha.high else ContentAlpha.disabled),
        headline = {
            Text(
                text = text,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        },
        // Display the icon if provided or
        // Preserve the space for the icon if requested
        leading = composableOrNull(icon != null || iconSpaceReserved) {
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
        footer = composableOrNull(footer != null) {
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
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
            )

            //
            val topValue = value
            DropDownMenu2(
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
                                    // = null,
                                    enabled = enabled,
                                    onClick = null
                                )

                                Text(
                                    text = entries[index],
                                    fontWeight = if (!selected) FontWeight.Normal else FontWeight.Bold,
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .fillMaxSize(),
                                    maxLines = 2,
                                    color = if (selected) MaterialTheme.colors.secondary else LocalContentColor.current
                                )
                            },
                            modifier = Modifier.defaultMinSize(minWidth = 180.dp, minHeight = 36.dp)
                        )
                    }
                },
                elevation = 10.dp,
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
fun ColorPickerPreference(
    text: CharSequence,
    value: Color,
    entries: List<Color>,
    onRequestValueChange: (Color) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconSpaceReserved: Boolean = true,
    icon: ImageVector? = null,
    forceVisible: Boolean = false,
) {
    val widget =
        @Composable {
            val color by animateColorAsState(targetValue = value)
            Spacer(
                modifier = Modifier
                    .background(color = color, shape = CircleShape)
                    .requiredSize(40.dp)
            )
        }

    val (checked, onToggleChecked) = remember { mutableStateOf(value) }

    val revealable =
        @Composable {

            val startPadding = (if (iconSpaceReserved) 24.dp else 0.dp) + 8.dp

            Column(
                modifier = Modifier
                    .padding(start = startPadding)
                    .fillMaxWidth()
            ) {

                ColorPicker(
                    entries = entries,
                    checked = checked,
                    onColorChecked = onToggleChecked,
                    modifier = Modifier.padding(vertical = 10.dp)
                )

                val manager = LocalFocusManager.current
                val onCancelClick = {
                    if (!forceVisible)
                        manager.clearFocus(true)
                }
                val onConfirmClick = {
                    if (!forceVisible)
                        manager.clearFocus(true)
                    onRequestValueChange(checked)
                }
                TextButtons(
                    onCancelClick = onCancelClick,
                    onConfirmClick = onConfirmClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    Preference(
        text = text,
        icon = icon,
        enabled = enabled,
        modifier = modifier,
        forceVisible = forceVisible,
        widget = widget,
        footer = revealable
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
            Label(text = stringResource(id = R.string.cancel))
        }
        TextButton(onClick = onConfirmClick) {
            Label(text = stringResource(id = R.string.ok))
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
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    steps: Int = 0,
    enabled: Boolean = true,
    iconSpaceReserved: Boolean = true,
    icon: ImageVector? = null,
    forceVisible: Boolean = false,
    preview: (@Composable () -> Unit)? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
) {
    // This composable function defines a Preference that includes a Slider.
    // revealable is a composable function that displays the slider and buttons.
    // It is only invoked when the preference is expanded.
    val revealable =
        @Composable {
            val startPadding = (if (iconSpaceReserved) 24.dp + 16.dp else 0.dp) + 8.dp
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = startPadding)
            ) {
                // Place the Slider composable.
                val (state, onChange) = remember { mutableFloatStateOf(value) }
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
                        onValueChange(value)
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
        widget = preview,
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
    value: String,
    onConfirmClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconSpaceReserved: Boolean = true,
    icon: ImageVector? = null,
    forceVisible: Boolean = false,
    maxLines: Int = 1,
    leadingFieldIcon: ImageVector? = null,
    label: CharSequence? = null,
    placeholder: CharSequence? = null,
    preview: (@Composable () -> Unit)? = null,
) {
    val manager = LocalFocusManager.current
    val (state, onChange) = remember { mutableStateOf(TextFieldValue(text = value)) }
    val revealable = @Composable {
        OutlinedTextField(
            value = state,
            onValueChange = onChange,
            modifier = Modifier
                .padding(start = (if (iconSpaceReserved) 24.dp + 8.dp else 8.dp) + 8.dp, end = 8.dp)
                .fillMaxWidth(),
            maxLines = maxLines,
            label = composableOrNull(label != null) {
                Label(text = label ?: "")
            },
            placeholder = composableOrNull(placeholder != null) {
                Text(text = placeholder ?: "")
            },
            shape = TextFieldShape,
            singleLine = maxLines == 1,
            leadingIcon = composableOrNull(leadingFieldIcon != null) {
                Icon(
                    imageVector = leadingFieldIcon ?: Icons.Default.Create,
                    contentDescription = null
                )
            },
            trailingIcon = {
                IconButton(
                    imageVector = Icons.Default.Close,
                    onClick = {
                        when {
                            state.text.isNotEmpty() -> onChange(TextFieldValue(""))
                            !forceVisible -> manager.clearFocus(true)
                        }
                    },
                    tint = MaterialTheme.colors.primary
                )
            },
            keyboardActions = KeyboardActions(
                onDone = {
                    onConfirmClick(state.text)
                    if (!forceVisible)
                        manager.clearFocus(true)
                }
            ),
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