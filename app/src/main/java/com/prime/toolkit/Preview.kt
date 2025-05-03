/*
 * Copyright 2025 sheik
 *
 * Created by sheik on 03-05-2025.
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

@file:OptIn(ExperimentalThemeApi::class)

package com.prime.toolkit

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.ExperimentalThemeApi
import com.zs.compose.theme.snackbar.Snackbar
import com.zs.compose.theme.snackbar.SnackbarData
import com.zs.compose.theme.snackbar.SnackbarDuration
import com.zs.compose.theme.snackbar.SnackbarHostState

private val SampleMessage = object: SnackbarData {
    override val message: CharSequence = buildAnnotatedString {
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append("Rate our app and help us improve")
        }
        withStyle(SpanStyle(color = Color.Gray)) {
            append("\nIf you are enjoying our app, please  consider leaving us a 5-star rating on the Play Store. Your positive feedback helps us grow and improve our app.")
        }
    }
    override val action: CharSequence?  = "Go"
    override val icon: ImageVector? = Icons.Outlined.Coffee
    override fun action() {
        TODO("Not yet implemented")
    }

    override fun dismiss() {
        TODO("Not yet implemented")
    }

    override val duration: SnackbarDuration =SnackbarDuration.Indefinite
    override val accent: Color = Color.Unspecified
}

@OptIn(ExperimentalFoundationApi::class)
@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun Preview() {
    AppTheme{
        Snackbar(
            SampleMessage,
            Modifier
        )
    }
}