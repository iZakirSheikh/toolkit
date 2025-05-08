@file:OptIn(ExperimentalThemeApi::class, ExperimentalFoundationApi::class)

package com.prime.toolkit

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.prime.toolkit.games.Games
import com.zs.compose.foundation.Background
import com.zs.compose.foundation.ClaretViolet
import com.zs.compose.foundation.MetroGreen
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.ColorPickerDialog
import com.zs.compose.theme.ExperimentalThemeApi
import com.zs.compose.theme.FloatingActionButton
import com.zs.compose.theme.Icon
import com.zs.compose.theme.LocalWindowSize
import com.zs.compose.theme.WindowSize.Category
import com.zs.compose.theme.adaptive.NavigationSuiteScaffold
import com.zs.compose.theme.appbar.BottomNavigationItem
import com.zs.compose.theme.appbar.FloatingBottomNavigationBar
import com.zs.compose.theme.appbar.SideBar
import com.zs.compose.theme.appbar.SideNavigationItem
import com.zs.compose.theme.calculateWindowSizeClass
import com.zs.compose.theme.snackbar.SnackbarDuration
import com.zs.compose.theme.snackbar.SnackbarHostState
import com.zs.compose.theme.text.Text
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "MainActivity"

private val SampleProgressCompleteMessage = buildAnnotatedString {
    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
        append("Progress")
    }
    withStyle(SpanStyle(color = Color.Gray)) {
        append("\nThe progress completed. Here you can put content of any size. This is a lng very long info for a dialog. i mena or a snackbar. wht is a snack bar in the forst place, it is a small bar shaped item at the bottom of the screen that displays info to the user. the forst place, it is a small bar shaped item at the bottom of the screen that displays info to the user.the forst place, it is a small bar shaped item at the bottom of the screen that displays info to the user.")
    }
}

@Composable
@NonRestartableComposable
fun NavigationItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isBottomNav: Boolean = false,
    checked: Boolean = false,
) = when (isBottomNav) {
    true -> BottomNavigationItem(
        selected = checked,
        icon = { Icon(icon, contentDescription = label) },
        label = { Text(label) },
        modifier = modifier,
        onClick = onClick,

        )

    else -> SideNavigationItem(
        selected = checked,
        icon = { Icon(icon, contentDescription = label) },
        label = { Text(label) },
        modifier = modifier,
        onClick = onClick
    )
}

@Composable
@NonRestartableComposable
fun ToolkitNavBar(
    showBottomNav: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) = when (showBottomNav) {
    true -> FloatingBottomNavigationBar(
        modifier = modifier,
        content = { content() },
        background = Background(AppTheme.colors.background(1.dp)),
        border = _root_ide_package_.androidx.compose.foundation.BorderStroke(
            0.6.dp,
            AppTheme.colors.background(5.dp)
        )
    )

    else -> SideBar(
        modifier = modifier,
        content = { content() },
        background = Background(AppTheme.colors.background(1.dp))
    )
}

@OptIn(ExperimentalFoundationApi::class)
class MainActivity : ComponentActivity() {

    private val snackbar = SnackbarHostState()

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set up the window
        // Window settings are likely handled in AppTheme already, but we ensure it here.
        val x = Icons.Outlined.ColorLens
        var progress by mutableFloatStateOf(Float.NaN)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.Transparent.toArgb(),
                Color.Transparent.toArgb()
            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.Transparent.toArgb(),
                Color.Transparent.toArgb()
            )
        )

        //
        lifecycleScope.launch {
            delay(1000)
            progress = -1f
            delay(2000)
            progress = 0f
            while (progress < 1f) {
                delay(300)
                progress += 0.05f
            }
            snackbar.showSnackbar(
                SampleProgressCompleteMessage,
                "Action",
                icon = Icons.Default.Feedback,
                duration = SnackbarDuration.Indefinite
            )
        }


        setContent {
            val clazz = calculateWindowSizeClass(this)
            var showColorPicker by remember { mutableStateOf(false) }



            val content = @Composable {

                ColorPickerDialog(showColorPicker, Color.MetroGreen) {
                    showColorPicker = false
                }

                NavigationSuiteScaffold(
                    vertical = clazz.width < Category.Medium,
                    snackbarHostState = snackbar,
                    progress = progress,
                    content = { Games() },
                    navBar = {
                        ToolkitNavBar(clazz.width < Category.Medium) {
                            NavigationItem(
                                icon = Icons.Default.Weekend,
                                label = "Home",
                                checked = true,
                                onClick = {}
                            )
                            NavigationItem(
                                icon = Icons.Default.Collections,
                                label = "Collections",
                                onClick = {}
                            )
                            NavigationItem(
                                icon = Icons.Default.Settings,
                                label = "Settings",
                                onClick = {}
                            )
                        }
                    },
                    floatingActionButton = {
                        FloatingActionButton(onClick = { showColorPicker = true }) {
                            Icon(Icons.Default.ColorLens, contentDescription = "Menu")
                        }
                    }
                )
            }


            val isLight = !isSystemInDarkTheme()
            AppTheme(
                isLight = isLight,
                accent = if (isLight) Color.ClaretViolet else Color(0xFFD8A25E),
                content = {
                    CompositionLocalProvider(
                        LocalWindowSize provides clazz,
                        content = content
                    )
                }
            )
        }
    }
}