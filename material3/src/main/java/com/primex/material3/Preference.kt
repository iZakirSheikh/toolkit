package com.primex.material3

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.primex.core.rememberState

/**
 * This is the container for some defaults of the preference composable.
 */
object PreferenceDefaults {
    /**
     * The padding required to save oif user wants leading icon space to be reserved.
     */
    val IconSpaceReserved = 12.dp
}

private val DefaultPreferenceShape = RoundedCornerShape(0.dp)

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
 */
@Composable
@NonRestartableComposable
fun Preference(
    title: CharSequence,
    modifier: Modifier = Modifier,
    singleLineTitle: Boolean = true,
    iconSpaceReserved: Boolean = true,
    icon: ImageVector? = null,
    summery: CharSequence? = null,
    enabled: Boolean = true, // no-op
    shape: Shape = DefaultPreferenceShape,
    color: Color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
    widget: @Composable (() -> Unit)? = null,
) {
    ListTile(
        shape = shape,
        color = color,
        enabled = enabled,
        headline = {
            Text(
                text = title,
                maxLines = if (singleLineTitle) 1 else 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold
            )
        },
        subtitle = composableOrNull(!summery.isNullOrBlank()) {
            Text(
                text = summery ?: "",
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        },
        trailing = widget,
        modifier = modifier,
        leading = composableOrNull(icon != null || iconSpaceReserved) {
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
        },
    )
}

@Composable
@NonRestartableComposable
fun SwitchPreference(
    title: CharSequence,
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit),
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = DefaultPreferenceShape,
    color: Color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
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
        color = color,
        shape = shape,
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
    shape: Shape = DefaultPreferenceShape,
    color: Color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
    summery: CharSequence? = null,
    onCheckedChange: ((Boolean) -> Unit)
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
        shape = shape,
        color = color,
        widget = {
            Checkbox(enabled = enabled, checked = checked, onCheckedChange = null)
        }
    )
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
    shape: Shape = DefaultPreferenceShape,
    color: Color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
    onColor: Color = MaterialTheme.colorScheme.onSurface,
    icon: ImageVector? = null,
    entries: List<Pair<String, T>>,
) {
    // ensure that entries are non-emoty.
    require(entries.isNotEmpty())

    // control the expanded state of the dropDownMenu.
    var expanded by rememberState(initial = false)
    // calculate eh default value of these entries
    val default = remember(defaultValue) {
        entries.find { (_, value) -> value == defaultValue }!!.first
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
        shape = shape,
        color = color,
        widget = {
            Box {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                )

                // compose the dropdown
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    // create corresponding composable for
                    // each entry.
                    content = {
                        entries.forEach { (placeHolder, value) ->
                            // handle click on Item.
                            val onEntryClick = {
                                if (value != defaultValue) {
                                    onRequestChange(value)
                                    expanded = false
                                }
                            }
                            // compose item
                            val checked = value == defaultValue
                            val color =
                                if (checked) MaterialTheme.colorScheme.secondary else onColor
                            DropdownMenuItem(
                                onClick = onEntryClick,
                                text = {
                                    Text(
                                        text = placeHolder,
                                        fontWeight = if (value != defaultValue) FontWeight.SemiBold else FontWeight.Bold,
                                        color = color
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        contentDescription = null,
                                        tint = color,
                                        painter = painterResource(
                                            if (checked)
                                                R.drawable.radio_button_checked
                                            else
                                                R.drawable.radio_button_unchecked_24
                                        )
                                    )
                                }
                            )
                        }
                    },
                )
            }
        }
    )
}
