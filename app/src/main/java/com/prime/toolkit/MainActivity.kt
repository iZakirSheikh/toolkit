package com.prime.toolkit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Phone
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.prime.toolkit.ui.theme.ToolkitTheme
import com.primex.material2.Label
import com.primex.material2.Placeholder
import com.primex.material2.Preference
import com.primex.preferences.Preferences

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val x = Preferences(this)
        setContent {
            ToolkitTheme {
                // A surface container using the 'background' color from the theme
                PLaceholder()


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
    Surface(modifier = Modifier.fillMaxSize()) {
       Label(text = "Hi Android")
    }
}