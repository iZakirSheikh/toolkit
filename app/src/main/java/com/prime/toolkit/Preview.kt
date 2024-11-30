/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by 2024 on 19-09-2024.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:OptIn(ExperimentalToolkitApi::class)

package com.prime.toolkit

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.TopAppBar
import androidx.compose.material.darkColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.lightColors
import androidx.compose.material.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.prime.toolkit.core.games
import com.primex.core.ExperimentalToolkitApi
import com.primex.core.MetroGreen
import com.primex.core.SignalWhite
import com.primex.core.plus
import com.primex.core.shapes.SquircleShape
import com.primex.core.textResource
import com.primex.core.withSpanStyle
import com.primex.material2.Button
import com.primex.material2.Button2
import com.primex.material2.CheckBoxPreference
import com.primex.material2.DropDownPreference
import com.primex.material2.IconButton
import com.primex.material2.Label
import com.primex.material2.ListTile
import com.primex.material2.Preference
import com.primex.material2.SliderPreference
import com.primex.material2.SwitchPreference
import com.primex.material2.TextFieldPreference

private const val TAG = "Preview"
@Preview(showBackground = false, widthDp = 360)
@Composable
fun PreviewM2() {
    MaterialTheme(lightColors()) {
        Column(modifier = Modifier.statusBarsPadding()) {
            Log.d(TAG, "PreviewM2: ${LocalTextStyle.current.fontSize}")
           Surface {
               val (def, onRequestChange) = remember { mutableFloatStateOf(0f) }
               Column {
                   TextFieldPreference(
                       text = textResource(R.string.sample_preference_text),
                       icon = Icons.Outlined.Language,
                       value = "",
                       onConfirmClick = {},
                       label = "Text",
                       placeholder = "Enter text here."
                   )
                   TextFieldPreference(
                       text = textResource(R.string.sample_preference_text),
                       icon = Icons.Outlined.Language,
                       value = "",
                       onConfirmClick = {},
                       label = "Text",
                       placeholder = "Enter text here."
                   )
                   TextFieldPreference(
                       text = textResource(R.string.sample_preference_text),
                       icon = Icons.Outlined.Language,
                       value = "",
                       onConfirmClick = {},
                       label = "Text",
                       placeholder = "Enter text here."
                   )
                   SliderPreference(
                       text = buildAnnotatedString {
                           withSpanStyle(fontWeight = FontWeight.Bold) {
                               append("Slider")
                           }
                           withSpanStyle(color = Color.LightGray) {
                               append("\nThis is a sample pref")
                           }
                       },
                       icon = Icons.Outlined.Language,
                       value = def,
                       onRequestChange = onRequestChange,
                       preview = {
                           Label("$it")
                       }
                   )
               }
           }
        }
    }
}
