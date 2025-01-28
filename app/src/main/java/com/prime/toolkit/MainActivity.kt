package com.prime.toolkit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.Button
import com.zs.compose.theme.ElevatedButton
import com.zs.compose.theme.FilledTonalButton
import com.zs.compose.theme.OutlinedButton
import com.zs.compose.theme.Text
import com.zs.compose.theme.TextButton

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Row {
                    Button(onClick = {}) {
                        Text("Filled")
                    }
                    TextButton("", onClick = {})
                    TextButton(onClick = {}) {
                        Text("Text ")
                    }
                    OutlinedButton(onClick = {}) {
                        Text("Outlined")
                    }
                    ElevatedButton(onClick = {}) {
                        Text("Elevated")
                    }
                    FilledTonalButton(onClick = {}) { "Tonal" }
                }
            }
        }
    }
}

