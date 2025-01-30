package com.prime.toolkit

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.More
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.Chip
import com.zs.compose.theme.ExperimentalThemeApi
import com.zs.compose.theme.ExtendedFloatingActionButton
import com.zs.compose.theme.FilledTonalButton
import com.zs.compose.theme.FloatingActionButton
import com.zs.compose.theme.HorizontalDivider
import com.zs.compose.theme.Icon
import com.zs.compose.theme.SelectableChip
import com.zs.compose.theme.menu.DropDownMenu
import com.zs.compose.theme.menu.DropDownMenuItem
import com.zs.compose.theme.text.Text
import com.zs.compose.theme.text.OutlinedTextField
import com.zs.compose.theme.text.SecureTextField
import com.zs.compose.theme.text.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.zs.compose.theme.AlertDialog
import com.zs.compose.theme.Button
import com.zs.compose.theme.IconButton
import com.zs.compose.theme.OutlinedButton


private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalThemeApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .padding(6.dp)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.weight(1f))

                    var expanded by remember { mutableStateOf(false) }

                    Chip(
                        onClick = { expanded = true },
                    ) {
                        Icon(Icons.Filled.Sort, null)
                    }

                    AlertDialog(
                        expanded,
                        onDismissRequest = { expanded = false },
                        icon = {
                            IconButton(Icons.Filled.Feedback, onClick = {}, contentDescription = null)
                        },
                        title = { Text("Settings") },
                        actions = {
                            Row {
                                IconButton(Icons.Outlined.Edit, onClick = {}, contentDescription = null)
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
                            Button (onClick = {}) {
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

