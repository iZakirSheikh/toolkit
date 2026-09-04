@file:OptIn(ExperimentalThemeApi::class, ExperimentalFoundationApi::class)

package com.prime.toolkit

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DesignServices
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Textsms
import androidx.compose.material.icons.filled.Weekend
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.prime.toolkit.core.background
import com.prime.toolkit.core.observe
import com.prime.toolkit.core.rememberBackgroundProvider
import com.prime.toolkit.games.Games
import com.prime.toolkit.m3.M3
import com.prime.toolkit.settings.Settings
import com.prime.toolkit.text.Strings
import com.zs.compose.foundation.Background
import com.zs.compose.foundation.BlueLilac
import com.zs.compose.foundation.MetroGreen
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.Badge
import com.zs.compose.theme.BadgedBox
import com.zs.compose.theme.ExperimentalThemeApi
import com.zs.compose.theme.FloatingActionButton
import com.zs.compose.theme.Icon
import com.zs.compose.theme.LocalWindowSize
import com.zs.compose.theme.adaptive.NavigationSuiteScaffold
import com.zs.compose.theme.appbar.FloatingBottomNavigationBar
import com.zs.compose.theme.appbar.NavigationItem
import com.zs.compose.theme.appbar.SideBar
import com.zs.compose.theme.calculateWindowSizeClass
import com.zs.compose.theme.dynamicAccentColor
import com.zs.compose.theme.snackbar.SnackbarDuration
import com.zs.compose.theme.snackbar.SnackbarHostState
import com.zs.compose.theme.text.Label
import com.zs.preferences.Preferences
import com.zs.preferences.stringPreferenceKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.provider.Settings as System

private const val TAG = "MainActivity"

private val SampleTestMessage = buildAnnotatedString {
    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
        append("Network Status")
    }
    withStyle(SpanStyle(color = Color.Gray)) {
        append("\nYou're currently offline. Please check your internet connection or try again later. This message can be as detailed as necessary, giving users clear context about the issue. Whether it’s a toast, dialog, or snackbar, the goal is to keep them informed in a non-intrusive way. Consider adding specific troubleshooting steps or a link to a help page if appropriate. The key is to be both informative and reassuring, letting the user know that you are aware of the problem and are working to resolve it, or providing them with the means to do so themselves. This proactive approach can significantly enhance user experience, even when things go wrong. Ensure the message is easily dismissible so it doesn't obstruct other functionalities if the user chooses to ignore it for the moment.")
    }
}

@Composable
@NonRestartableComposable
fun ToolkitNavBar(
    showBottomNav: Boolean,
    background: Background,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) = when (showBottomNav) {
    true -> FloatingBottomNavigationBar(
        modifier = modifier,
        content = { content() },
        background = background,
        border = androidx.compose.foundation.BorderStroke(
            0.6.dp,
            AppTheme.colors.background(5.dp)
        )
    )

    else -> SideBar(
        modifier = modifier,
        content = { content() },
        background = background
    )
}

@OptIn(ExperimentalFoundationApi::class)
class MainActivity : ComponentActivity() {
    private val snackbar = SnackbarHostState()
    var progress by mutableFloatStateOf(Float.NaN)
    var darkMode by mutableStateOf(false)
    lateinit var preferences: Preferences

    private val content = @Composable {
        var showColorPicker by remember { mutableStateOf(false) }
        var selected by remember { androidx.compose.runtime.mutableIntStateOf(0) }
        val (width, height) = LocalWindowSize.current
        val vertical = width < height
        val surface = rememberBackgroundProvider()
        val colors = AppTheme.colors
        NavigationSuiteScaffold(
            vertical = vertical,
            snackbarHostState = snackbar,
            progress = progress,
            content = {
                Crossfade(selected, modifier = Modifier.observe(surface)) { value ->
                    when (value) {
                        0 -> Games()
                        3 -> System()
                        2 -> Strings()
                        1 -> M3()
                        4 -> PreviewNode()
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {darkMode = !darkMode},
                    shapes = AppTheme.shapes.large to AppTheme.shapes.xLarge,
                    content = {
                        Icon(Icons.Default.LightMode, null)
                    },
                )
            },
            navBar = {
                ToolkitNavBar(
                    vertical,
                    background = if (vertical) colors.background(surface) else Background(colors.background(10.dp)),
                    content = {
                        // Home
                        BadgedBox(
                            badge = {
                                Badge(backgroundColor = Color.MetroGreen, content = {
                                    Label("Pro")
                                })
                            }
                        ) {
                            NavigationItem(
                                icon = { Icon(Icons.Default.Weekend, null) },
                                label = { Label("Home") },
                                selected = selected == 0,
                                onClick = { selected = 0 },
                                isBottomNav = vertical
                            )
                        }

                        // @nd
                        NavigationItem(
                            icon = { Icon(Icons.Default.DesignServices, null) },
                            label = { Label("M3 Catelog") },
                            selected = selected == 1,
                            onClick = { selected = 1 },
                            isBottomNav = vertical
                        )

                        // Strings
                        NavigationItem(
                            icon = { Icon(Icons.Default.Textsms, null) },
                            label = { Label("Strings") },
                            selected = selected == 2,
                            onClick = { selected = 2 },
                            isBottomNav = vertical
                        )

                        // Settings
                        NavigationItem(
                            icon = { Icon(Icons.Default.Settings, null) },
                            label = { Label("Settings") },
                            selected = selected == 3,
                            onClick = { selected = 3 },
                            isBottomNav = vertical
                        )

                        // Preview
                        NavigationItem(
                            icon = { Icon(Icons.Default.Preview, null) },
                            label = { Label("Preview") },
                            selected = selected == 4,
                            onClick = { selected = 4 },
                            isBottomNav = vertical
                        )
                    }
                )
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!::preferences.isInitialized){
            preferences = Preferences.get(context = this)
            val key = stringPreferenceKey("test")
            preferences[key] = "new string pref test"
            Log.d(TAG, "onCreate: ${preferences[key]}")
        }
        val style = SystemBarStyle.auto(
            Color.Transparent.toArgb(),
            Color.Transparent.toArgb(),
            detectDarkMode = {
                darkMode
            }
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
                SampleTestMessage,
                "Action",
                icon = Icons.Default.Feedback,
                duration = SnackbarDuration.Indefinite
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(
                    System.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            }
        }

        // Content
        setContent {

            enableEdgeToEdge(
                statusBarStyle = style,
                navigationBarStyle = style
            )
            val accent by com.zs.compose.foundation.backdrop.dynamicAccentColor()
            AppTheme(
                isLight = !darkMode,
                //accent = if (!darkMode) /*Color.ClaretViolet*/ Color.BlueLilac else Color(0xFFD8A25E),
                accent = Color(accent?.tertiaryColor?.toArgb() ?: Color.MetroGreen.toArgb())/*dynamicAccentColor(LocalContext.current, darkMode)*/,
                content = {
                    val clazz = calculateWindowSizeClass(this)
                    CompositionLocalProvider(
                        LocalWindowSize provides clazz,
                        content = content
                    )
                }
            )
        }
    }
}