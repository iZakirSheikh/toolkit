package com.prime.toolkit.preview

import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.primex.core.rememberState
import com.primex.material2.Label
import com.primex.material2.SliderPreference
import com.primex.material2.TextFieldPreference

@Preview
@Composable
fun PreviewPreference() {
    MaterialTheme {
        var value by rememberState(initial = TextFieldValue(""))
        Column {
            TextFieldPreference(
                title = "Preference",
                summery = "Tis is a test for observable preview preference.",
                value = value,
                onValueChange = { value = it },
                label = "Preview",
                placeholder = "This is a placeholder text",
                icon = Icons.Default.Build,
                preview = { Label(text = "${value.text}")},
                leadingFieldIcon = Icons.Outlined.FavoriteBorder,
            )
            var slider by rememberState(initial = 0.1f)
            SliderPreference(title = "title", summery = "saume zkhzkjd zfkjshf", defaultValue = slider, onValueChange = { slider = it })
        }
    }
}