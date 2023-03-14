package com.primex.material2

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp


@Composable
fun ColorPicker(
    entries: List<Color>,
    checked: Color,
    onColorChecked: (color: Color) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .padding(start = 16.dp, end = 16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (color in entries) {
            Spacer(
                modifier = Modifier
                    .graphicsLayer {
                        if (checked == color) {
                            scaleY = 1.15f
                        }
                    }
                    .size(width = 15.dp, height = 45.dp)
                    .weight(1f)
                    .background(color)
                    .clickable(enabled) {
                        onColorChecked(color)
                    }
            )
        }
    }
}