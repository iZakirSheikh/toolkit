@file:OptIn(ExperimentalThemeApi::class)

package com.prime.toolkit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhotoAlbum
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.zs.compose.theme.AlertDialog
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.Button
import com.zs.compose.theme.Chip
import com.zs.compose.theme.ChipDefaults
import com.zs.compose.theme.ContentAlpha
import com.zs.compose.theme.DropDownPreference
import com.zs.compose.theme.ExperimentalThemeApi
import com.zs.compose.theme.FloatingActionButton
import com.zs.compose.theme.Icon
import com.zs.compose.theme.IconButton
import com.zs.compose.theme.ListItem
import com.zs.compose.theme.LocalContentColor
import com.zs.compose.theme.LocalWindowSize
import com.zs.compose.theme.OutlinedButton
import com.zs.compose.theme.SelectableChip
import com.zs.compose.theme.SliderPreference
import com.zs.compose.theme.Switch
import com.zs.compose.theme.SwitchPreference
import com.zs.compose.theme.TextFieldPreference
import com.zs.compose.theme.adaptive.FabPosition
import com.zs.compose.theme.adaptive.NavigationSuiteScaffold
import com.zs.compose.theme.adaptive.Scaffold
import com.zs.compose.theme.adaptive.contentInsets
import com.zs.compose.theme.appbar.AppBarDefaults
import com.zs.compose.theme.appbar.BottomAppBar
import com.zs.compose.theme.appbar.BottomNavigationItem
import com.zs.compose.theme.appbar.LargeTopAppBar
import com.zs.compose.theme.calculateWindowSizeClass
import com.zs.compose.theme.lightColors
import com.zs.compose.theme.rememberDismissState
import com.zs.compose.theme.snackbar.SnackbarDuration
import com.zs.compose.theme.snackbar.SnackbarHostState
import com.zs.compose.theme.text.Label
import com.zs.compose.theme.text.Text
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.zs.compose.theme.WindowSize.Category

private const val TAG = "MainActivity"

@Composable
fun Content(modifier: Modifier = Modifier) {
    val navInsets = WindowInsets.contentInsets
    val behaviour = AppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        primary = {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .nestedScroll(behaviour.nestedScrollConnection)
                    .verticalScroll(rememberScrollState(), )
                    .padding(navInsets)
                    .padding(WindowInsets.contentInsets)
                    .padding(6.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.weight(1f))

                val info = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)){
                        appendLine("Title")
                    }
                    withStyle(SpanStyle(color = LocalContentColor.current.copy(ContentAlpha.medium))){
                        append("This represents the summery of the preference. This can be of more than one line in length.")
                    }
                }
                var shown by remember { mutableStateOf(false) }
                SwitchPreference(
                    info,
                    shown,
                    onCheckedChange = {shown = it},
                    icon = Icons.Default.Feedback
                )

                com.zs.compose.theme.CheckBoxPreference(
                    info,
                    false,
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
                    steps = 9,
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
                    trailing = { Switch(false, null) },
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
        },
        topBar = {
            LargeTopAppBar(
                title = { Text("Settings") },
                windowInsets = AppBarDefaults.topAppBarWindowInsets,
                navigationIcon = {
                    IconButton(Icons.Default.Menu, onClick = {}, contentDescription = null)
                },
                actions = {
                    IconButton(Icons.Outlined.Settings, onClick = {}, contentDescription = null)
                },
                scrollBehavior = behaviour
            )
        },
        modifier = modifier
    )
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalThemeApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set up the window
        // Window settings are likely handled in AppTheme already, but we ensure it here.
        val state = SnackbarHostState()
        Icons.Outlined.KeyboardArrowDown
        var progress by mutableFloatStateOf(Float.NaN)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.Transparent.toArgb(), Color.Transparent.toArgb()),
            navigationBarStyle = SystemBarStyle.light(Color.Transparent.toArgb(), Color.Transparent.toArgb())
        )
        val message = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)){
                append("Progress")
            }
            withStyle(SpanStyle(color = Color.Gray)){
                append("\nThe progress completed. Here you can put content of any size. This is a lng very long info for a dialog. i mena or a snackbar. wht is a snack bar in the forst place, it is a small bar shaped item at the bottom of the screen that displays info to the user. the forst place, it is a small bar shaped item at the bottom of the screen that displays info to the user.the forst place, it is a small bar shaped item at the bottom of the screen that displays info to the user.")
            }
        }


        setContent {
            val sizeClass = calculateWindowSizeClass(this)
            LaunchedEffect(Unit) {
                delay(1000)
                progress = -1f
                delay(2000)
                progress = 0f
                while (progress < 1f){
                    delay(300)
                    progress += 0.05f
                }
                state.showSnackbar(
                    message,
                    "Action",
                    icon = Icons.Default.Feedback,
                    duration = SnackbarDuration.Long
                )
            }
            var light by remember { mutableStateOf(false) }
            AppTheme(isLight = light) {
                CompositionLocalProvider(LocalWindowSize provides sizeClass) {

                    NavigationSuiteScaffold(
                        sizeClass.width < Category.Medium,
                        snackbarHostState = state,
                        progress = progress,
                        hideNavigationBar = false,
                        fabPosition = FabPosition.Start,
                        floatingActionButton = {
                            FloatingActionButton(onClick = {lifecycleScope.launch(){
                                light = !light
                                state.showSnackbar(message)} }
                            ) {
                                Icon(Icons.Filled.Feedback, null)
                            }
                        },
                        content = { Content(Modifier.padding(WindowInsets.contentInsets)) }
                    ) {
                        val accent = AppTheme.colors.accent
                        val background = AppTheme.colors.background
                        BottomAppBar(
                            //windowInsets = WindowInsets.None,
                            /* modifier = Modifier
                                 .padding(horizontal = 22.dp).windowInsetsPadding(
                                 AppBarDefaults.bottomAppBarWindowInsets,
                             )*/
//                            .border(1.dp, AppTheme.colors.background(5.dp), RoundedCornerShape(20))

                            //shape = RoundedCornerShape(25),
                        ){
                            val colors = ChipDefaults.chipColors(backgroundColor = LocalContentColor.current.copy(
                                0.3f), contentColor = LocalContentColor.current)
                            var selected by remember { mutableIntStateOf(0) }
                            BottomNavigationItem(selected == 0,
                                onClick = {selected = 0},
                                label = {Label("Home")},
                                icon = {Icon(Icons.Outlined.Home, null)}
                            )
                            BottomNavigationItem(selected == 1,
                                onClick = {selected = 1},
                                label = {Label("Photos")},
                                icon = {Icon(Icons.Outlined.PhotoAlbum, null)}
                            )

                            BottomNavigationItem(selected == 3,
                                onClick = {selected = 3},
                                label = {Label("Videos")},
                                icon = {Icon(Icons.Outlined.VideoLibrary, null)}
                            )

                            BottomNavigationItem(selected == 4,
                                onClick = {selected = 4},
                                label = {Label("Settings")},
                                icon = {Icon(Icons.Outlined.Settings, null)}
                            )
                        }
                    }
                }
            }
        }
    }
}

