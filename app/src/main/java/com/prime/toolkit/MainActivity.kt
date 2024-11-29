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
import com.primex.core.ExperimentalToolkitApi

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
        setContent {
            val isDark = isSystemInDarkTheme()
            PreviewM2()
        }
    }
}