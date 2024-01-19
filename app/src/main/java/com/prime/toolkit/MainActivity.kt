@file:OptIn(ExperimentalToolkitApi::class)

package com.prime.toolkit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import com.prime.toolkit.preview.BlurPreview
import com.prime.toolkit.ui.theme.ToolkitTheme
import com.primex.core.ExperimentalToolkitApi
import com.primex.core.rememberState
import com.primex.material2.Preference
import com.primex.material2.SwitchPreference

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ToolkitTheme(false) { // A surface container using the 'background' color from the theme
                BlurPreview()
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}


@Preview
@Composable
fun PLaceholder() {
    Scaffold {
       Column(modifier = Modifier.padding(it)) {
           Preference(
               title = "App Version",
               summery = buildAnnotatedString {
                   append("2.2.5-debug")
                   append("\nHave feedback we would like to here, but please dont share sensitive information.\nTap to open feedback dialog.")
               },
               icon = Icons.Outlined.Info,

               )

           var checked by remember { mutableStateOf(value = false) }
           SwitchPreference(
               title = "Color Status Bar",
               summery = "Force Color Status Bar.",
               checked = checked,
               onCheckedChange = { checked =!checked},
               icon = Icons.Outlined.Settings
           )
       }
    }
}