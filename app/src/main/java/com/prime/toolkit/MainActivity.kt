package com.prime.toolkit

import android.os.Bundle
import android.preference.CheckBoxPreference
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.zs.compose.theme.AlertDialog
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.Button
import com.zs.compose.theme.Chip
import com.zs.compose.theme.ContentAlpha
import com.zs.compose.theme.DropDownPreference
import com.zs.compose.theme.ExperimentalThemeApi
import com.zs.compose.theme.Icon
import com.zs.compose.theme.IconButton
import com.zs.compose.theme.ListItem
import com.zs.compose.theme.LocalContentColor
import com.zs.compose.theme.OutlinedButton
import com.zs.compose.theme.SelectableChip
import com.zs.compose.theme.SliderPreference
import com.zs.compose.theme.Surface
import com.zs.compose.theme.Switch
import com.zs.compose.theme.SwitchPreference
import com.zs.compose.theme.TextFieldPreference
import com.zs.compose.theme.darkColors
import com.zs.compose.theme.lightColors
import com.zs.compose.theme.rememberDismissState
import com.zs.compose.theme.text.Text


private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalThemeApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme(colors = lightColors()) {
                Surface {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .padding(6.dp)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.weight(1f))

                        val info = buildAnnotatedString {
                            appendLine("Title")
                            withStyle(SpanStyle(color = LocalContentColor.current.copy(ContentAlpha.medium))){
                                append("This represents the summery of the preference. This can be of more than one line in length.")
                            }
                        }
                        SwitchPreference(
                            info,
                            true,
                            onCheckedChange = {},
                            icon = Icons.Default.Feedback
                        )

                        com.zs.compose.theme.CheckBoxPreference(
                            info,
                            true,
                            onCheckedChange = {},
                            icon = Icons.Default.Feedback
                        )


                        TextFieldPreference(
                            info,
                            rememberTextFieldState(""),
                            onConfirmClick = {},
                            icon = Icons.Default.Feedback
                        )

                        DropDownPreference(
                            1,
                            info,
                            values = arrayOf(1, 2, 3),
                            entries = arrayOf("One", "Two", "Three"),
                            onRequestChange = {},
                            icon = Icons.Default.Feedback
                        )

                        var (value , ch) = remember { mutableFloatStateOf(0f) }
                        SliderPreference(
                            info,
                            value,
                            onRequestChange = ch,
                            icon = Icons.Default.Feedback
                        )

                        var expanded by remember { mutableStateOf(false) }

                        Chip(
                            onClick = { expanded = true },
                        ) {
                            Icon(Icons.Filled.Sort, null)
                        }

                        SelectableChip(
                            onClick = {},
                            selected = true
                        ) {
                            Text("Chip")
                        }

                        val state = rememberDismissState(

                        )

                        ListItem(
                            heading = {Text("Headline")},
                            subheading = {Text("Subheading")},
                            leading = {Icon(Icons.Outlined.Person, null)},
                            trailing = { Switch(true, null) },
                            overline = {Text("Overline")},
                            modifier = Modifier.clickable(){}
                        )

                        AlertDialog(
                            expanded,
                            onDismissRequest = { expanded = false },
                            icon = {
                                IconButton(
                                    Icons.Filled.Feedback,
                                    onClick = {},
                                    contentDescription = null
                                )
                            },
                            title = { Text("Settings") },
                            actions = {
                                Row {
                                    IconButton(
                                        Icons.Outlined.Edit,
                                        onClick = {},
                                        contentDescription = null
                                    )
                                    IconButton(
                                        Icons.Outlined.Settings,
                                        onClick = {},
                                        contentDescription = null
                                    )
                                    IconButton(
                                        Icons.Outlined.MoreVert,
                                        onClick = {},
                                        contentDescription = null
                                    )
                                }
                            },
                            footer = {
                                Button(onClick = {}) {
                                    Text("Discard")
                                }
                                OutlinedButton(onClick = {}) {
                                    Text("Cancel")
                                }
                            }
                        ) {
                            Text("The last saved draft will be moved to deleted items folder.")
                        }


                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

