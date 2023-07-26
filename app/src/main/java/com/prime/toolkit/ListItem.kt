package com.prime.toolkit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.primex.material2.ListTile

@Preview(widthDp = 360)
@Composable
fun ListItemPrev() {
    ListTile(
        headline = { Text(text = "Headline") },
        subtitle = { Text(text = "Subtitle") },
        overline = { Text(text = "overline") },
        leading = {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(0.1f),
                modifier = Modifier.size(56.dp)
            ) {

            }
        },
        trailing = {
            Icon(imageVector = Icons.Outlined.MoreVert, contentDescription = null)
        },
        color = Color.Red.copy(0.1f),
        shape = RoundedCornerShape(20),
        modifier = Modifier.clickable {  }
    )
}
