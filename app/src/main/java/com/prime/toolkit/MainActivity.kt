package com.prime.toolkit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.CircularProgressIndicator
import com.zs.compose.theme.LinearProgressIndicator
import kotlinx.coroutines.delay
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.state.ToggleableState
import com.zs.compose.theme.Checkbox
import com.zs.compose.theme.ExperimentalThemeApi
import com.zs.compose.theme.RadioButton
import com.zs.compose.theme.RangeSlider
import com.zs.compose.theme.Slider
import com.zs.compose.theme.Switch
import com.zs.compose.theme.TriStateCheckbox

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalThemeApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(6.dp).fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.weight(1f))
                    var state by remember { mutableFloatStateOf(0.0f) }
                    Slider(state, onValueChange = {state = it}, steps = 9, enabled = true)
                    Spacer(Modifier.weight(1f), )
                }
            }
        }
    }
}

