package com.prime.toolkit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.ExperimentalThemeApi
import com.zs.compose.theme.Icon
import com.zs.compose.theme.text.Text
import com.zs.compose.theme.text.OutlinedTextField
import com.zs.compose.theme.text.SecureTextField
import com.zs.compose.theme.text.TextField

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
                    TextField(
                        rememberTextFieldState(),
                        placeholder = {Text("TextField")},
                        trailingIcon = {
                            Icon(Icons.Filled.Clear, contentDescription = null)
                        },
                        label = {
                            Text("Label Text")
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = null)
                        }
                    )
                    OutlinedTextField(
                        rememberTextFieldState(),
                        trailingIcon = {
                            Icon(Icons.Filled.Clear, contentDescription = null)
                        },
                        label = {
                            Text("Label Text")
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = null)
                        }
                    )
                    SecureTextField(
                        rememberTextFieldState(),
                        trailingIcon = {
                            Icon(Icons.Filled.Clear, contentDescription = null)
                        },
                        label = {
                            Text("Label Text")
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = null)
                        }
                    )
                    Spacer(Modifier.weight(1f), )
                }
            }
        }
    }
}

