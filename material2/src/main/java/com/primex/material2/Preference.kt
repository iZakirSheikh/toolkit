package com.primex.material2

import android.R
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Checkbox
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Slider
import androidx.compose.material.Switch
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.primex.core.acquireFocusOnInteraction
import com.primex.core.composableOrNull
import com.primex.core.padding
import com.primex.core.rememberState

private const val TAG = "Preference"

/**
 * This is the container for some defaults of the preference composable.
 */
object PreferenceDefaults {
    /**
     * The padding required to save oif user wants leading icon space to be reserved.
     */
    val IconSpaceReserved = 12.dp
}

/**
 * A General [Preference(title = )] representation.
 * The basic building block that represents an individual setting displayed to a user in the preference hierarchy.
 * @param modifier [Modifier] allows to modify the outer wrapper of this preference
 * @param enabled  [Boolean]  Sets whether this preference should disable its view when it gets disabled.
 * @param singleLineTitle  [Boolean] Sets whether to constrain the title of this preference to a single line instead of letting it wrap onto multiple lines.
 * @param iconSpaceReserved  [Boolean] Sets whether to reserve the space of this preference icon view when no icon is provided. If set to true, the preference will be offset as if it would have the icon and thus aligned with other preferences having icons.
 * @param icon -[ImageVector] Sets the icon for this preference with a [ImageVector].
 * @param summery [String] Sets the summary for this preference with a [String].
 * @param title  Sets the title for this preference with a [String].
 * @param widget Sets the layout for the controllable widget portion of this preference.
 * @param revealable The content that is hide/show on users request.
 * @param forceVisible if true make [revealable] content show/hide.
 */
@Composable
fun Preference(
    title: CharSequence,
    modifier: Modifier = Modifier,
    singleLineTitle: Boolean = true,
    iconSpaceReserved: Boolean = true,
    icon: ImageVector? = null,
    summery: CharSequence? = null,
    enabled: Boolean = true,
    widget: @Composable (() -> Unit)? = null,
    revealable: (@Composable () -> Unit)? = null,
    forceVisible: Boolean = false,
) {
    // The only difference is that we are using the new ListTile.
    val interactionSource = remember(::MutableInteractionSource)
    val listModifier = when (forceVisible || !enabled) {
        true -> modifier
        else -> Modifier
            .acquireFocusOnInteraction(
                interactionSource, indication = LocalIndication.current
            )
            .then(modifier)
            .animateContentSize()
    }
    ListTile(color = Color.Transparent, trailing = widget, enabled = enabled, headline = {
        Text(
            text = title,
            maxLines = if (singleLineTitle) 1 else 2,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.SemiBold
        )
    }, summery = composableOrNull(!summery.isNullOrBlank()) {
        Text(
            text = summery ?: "", maxLines = 4, overflow = TextOverflow.Ellipsis
        )
    }, leading = composableOrNull(icon != null || iconSpaceReserved) {
        when {
            // non-null write the icon.
            icon != null -> Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.padding(top = 4.dp)
            )
            // preserve the space in case user requested.
            iconSpaceReserved -> Spacer(Modifier.padding(PreferenceDefaults.IconSpaceReserved))
        }
    }, footer = composableOrNull(revealable != null) {
        val expanded by when (forceVisible) {
            true -> rememberState(initial = true)
            else -> interactionSource.collectIsFocusedAsState()
        }
        Crossfade(targetState = expanded, label = TAG) { value ->
            if (value) revealable?.invoke()
        }
    }, modifier = listModifier
    )
}

@Composable
fun SwitchPreference(
    title: CharSequence,
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit),
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    singleLineTitle: Boolean = true,
    iconSpaceReserved: Boolean = true,
    icon: ImageVector? = null,
    summery: CharSequence? = null,
) {
    Preference(
        modifier = modifier.clickable(enabled = enabled) {
            onCheckedChange(!checked)
        },
        title = title,
        enabled = enabled,
        singleLineTitle = singleLineTitle,
        iconSpaceReserved = iconSpaceReserved,
        icon = icon,
        summery = summery,
        widget = {
            Switch(enabled = enabled, checked = checked, onCheckedChange = null)
        },
    )
}

@Composable
fun CheckBoxPreference(
    title: CharSequence,
    checked: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    singleLineTitle: Boolean = true,
    iconSpaceReserved: Boolean = true,
    icon: ImageVector? = null,
    summery: CharSequence? = null,
    onCheckedChange: ((Boolean) -> Unit)
) {
    Preference(modifier = modifier.clickable(enabled = enabled) {
        onCheckedChange(!checked)
    },
        title = title,
        enabled = enabled,
        singleLineTitle = singleLineTitle,
        iconSpaceReserved = iconSpaceReserved,
        icon = icon,
        summery = summery,
        widget = {
            Checkbox(enabled = enabled, checked = checked, onCheckedChange = null)
        })
}

@Composable
fun <T> DropDownPreference(
    title: CharSequence,
    defaultValue: T,
    onRequestChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    singleLineTitle: Boolean = true,
    iconSpaceReserved: Boolean = true,
    icon: ImageVector? = null,
    entries: List<Pair<String, T>>,
) {
    require(entries.isNotEmpty())
    var expanded by rememberState(initial = false)
    val default = remember(defaultValue) {
        entries.find { (_, value) -> value == defaultValue }!!.first
    }


    val widget = @Composable {
        Box {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
            )

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                entries.forEach { (placeHolder, value) ->
                    val onEntryClick = {
                        if (value != defaultValue) {
                            onRequestChange(value)
                            expanded = false
                        }
                    }
                    DropdownMenuItem(onClick = onEntryClick) {
                        RadioButton(
                            selected = value == defaultValue,
                            // = null,
                            enabled = enabled, onClick = null
                        )

                        Text(
                            text = placeHolder,
                            style = MaterialTheme.typography.body1,
                            fontWeight = if (value != defaultValue) FontWeight.SemiBold else FontWeight.Bold,
                            modifier = Modifier
                                .padding(start = 16.dp, end = 16.dp)
                                .fillMaxSize(),
                            maxLines = 2,
                            color = if (value == defaultValue) MaterialTheme.colors.secondary else LocalContentColor.current
                        )
                    }
                }
            }
        }
    }

    Preference(
        modifier = modifier.clickable(enabled = enabled) {
            expanded = true
        },
        title = title,
        enabled = enabled,
        singleLineTitle = singleLineTitle,
        iconSpaceReserved = iconSpaceReserved,
        icon = icon,
        summery = default,
        widget = widget
    )
}

@Composable
private inline fun TextButtons(
    modifier: Modifier = Modifier,
    noinline onConfirmClick: () -> Unit,
    noinline onCancelClick: () -> Unit
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

@Composable
fun ColorPickerPreference(
    title: CharSequence,
    defaultEntry: Color,
    entries: List<Color>,
    onRequestValueChange: (Color) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    singleLineTitle: Boolean = true,
    iconSpaceReserved: Boolean = true,
    icon: ImageVector? = null,
    summery: CharSequence? = null,
    forceVisible: Boolean = false,
) {
    val widget = @Composable {
        val color by animateColorAsState(targetValue = defaultEntry)
        Spacer(
            modifier = Modifier
                .background(color = color, shape = CircleShape)
                .requiredSize(40.dp)
        )
    }

    var checked by rememberState(initial = defaultEntry)

    val revealable = @Composable {

        val startPadding = (if (iconSpaceReserved) 24.dp else 0.dp) + 8.dp

        Column(
            modifier = Modifier
                .padding(start = startPadding)
                .fillMaxWidth()
        ) {

            ColorPicker(
                entries = entries,
                checked = checked,
                onColorChecked = { checked = it },
                modifier = Modifier.padding(vertical = 10.dp)
            )

            val manager = LocalFocusManager.current
            val onCancelClick = {
                if (!forceVisible) manager.clearFocus(true)
            }
            val onConfirmClick = {
                if (!forceVisible) manager.clearFocus(true)
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
        title = title,
        icon = icon,
        summery = summery,
        singleLineTitle = singleLineTitle,
        enabled = enabled,
        modifier = modifier,
        forceVisible = forceVisible,
        widget = widget,
        revealable = revealable
    )
}


@Composable
fun SliderPreference(
    title: CharSequence,
    defaultValue: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    steps: Int = 0,
    enabled: Boolean = true,
    singleLineTitle: Boolean = true,
    iconSpaceReserved: Boolean = true,
    icon: ImageVector? = null,
    summery: CharSequence? = null,
    forceVisible: Boolean = false,
    iconChange: ImageVector? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
) {
    val revealable = @Composable {
        val startPadding = (if (iconSpaceReserved) 24.dp + 16.dp else 0.dp) + 8.dp
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = startPadding)
        ) {
            // place slider
            var value by rememberState(initial = defaultValue)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (iconChange != null) Icon(
                    imageVector = iconChange,
                    contentDescription = null,
                )
                Slider(
                    value = value, onValueChange = {
                        value = it
                    }, valueRange = valueRange, steps = steps, modifier = Modifier.weight(1f)
                )
                if (iconChange != null) {
                    Icon(
                        imageVector = iconChange,
                        contentDescription = null,
                        modifier = Modifier.scale(1.5f)
                    )
                }
            }

            val manager = LocalFocusManager.current
            val onCancelClick = {
                if (!forceVisible) manager.clearFocus(true)
            }
            val onConfirmClick = {
                if (!forceVisible) manager.clearFocus(true)
                onValueChange(value)
            }
            TextButtons(
                onCancelClick = onCancelClick,
                onConfirmClick = onConfirmClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    Preference(
        modifier = modifier,
        title = title,
        enabled = enabled,
        singleLineTitle = singleLineTitle,
        iconSpaceReserved = iconSpaceReserved,
        icon = icon,
        forceVisible = forceVisible,
        summery = summery,
        widget = null,
        revealable = revealable
    )
}

