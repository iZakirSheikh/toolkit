package com.prime.toolkit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.prime.toolkit.m2.M2
import com.prime.toolkit.m3.M3
import com.primex.core.ExperimentalToolkitApi
import com.primex.material3.Button2
import com.primex.material3.Label

private const val TAG = "MainActivity"


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Manually handle decor.
        // I think I am handling this in AppTheme Already.
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // Obtain the controller for managing the insets of the window.
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        window.navigationBarColor = Color.Transparent.toArgb()
        window.statusBarColor = Color.Transparent.toArgb()
        // Set the color of the navigation bar and the status bar to the determined color.
        controller.isAppearanceLightStatusBars = true
        controller.isAppearanceLightNavigationBars = true
        var selected by mutableIntStateOf(0)
        val onNavigateBack = { selected = 0 }
        setContent {
            val isDark = isSystemInDarkTheme()
            Crossfade(targetState = selected, label = "", modifier = Modifier.fillMaxSize()) {
                when (it) {
                    0 -> Launcher(isDark) { selected = it }
                    1 -> M2(dark = isDark, onBack = onNavigateBack)
                    else -> M3(dark = isDark, onNavigateBack)
                }
            }
        }
    }
}


@OptIn(ExperimentalToolkitApi::class)
@Composable
private fun Launcher(
    dark: Boolean,
    onLaunch: (id: Int) -> Unit
) {
    MaterialTheme(
        //colorScheme = if (dark) MaterialTheme.colorScheme
    ) {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize()
            ) {
                Button2(onClick = { onLaunch(1) }, shape = MaterialTheme.shapes.large) {
                    Icon(imageVector = Icons.Outlined.Info, contentDescription = "")
                    Label(text = "Material 2")
                }
                Spacer(modifier = Modifier.padding(16.dp))
                Button2(onClick = { onLaunch(2) }, shape = MaterialTheme.shapes.large) {
                    Icon(imageVector = Icons.Outlined.Info, contentDescription = "")
                    Label(text = "Material 3")
                }
            }
        }
    }
}