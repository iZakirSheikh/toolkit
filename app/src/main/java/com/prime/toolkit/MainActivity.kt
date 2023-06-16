package com.prime.toolkit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.prime.toolkit.ui.theme.ToolkitTheme
import com.primex.core.padding
import com.primex.core.rememberVectorPainter
import com.primex.material2.Button
import com.primex.material2.IconButton
import com.primex.material2.OutlinedButton
import com.primex.material2.Placeholder
import com.primex.material2.SwitchPreference
import com.primex.preferences.Preferences

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val x = Preferences(this)
        setContent {
            ToolkitTheme {


            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}


@Preview(widthDp = 800, heightDp = 360)
@Composable
fun ListTile() {
    ToolkitTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Placeholder(
                title = { Text(text = "Storage Permission", Modifier.padding(top = 16.dp))},
                vertical = false,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null
                    )
                },
                message = {
                    com.primex.material2.Text(text = "In order to work properly the app requires permission to your devices storage (Photos, Media and Files).")
                },
                action = {
                    OutlinedButton(
                        label = "Allow", onClick = { /*TODO*/ },
                        leading = rememberVectorPainter(
                            image = Icons.Default.Info,
                        ),
                        modifier = Modifier,
                        shape = CircleShape
                    )
                },
                modifier = Modifier
            )
        }
    }
}
