package com.primex.material2

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.primex.core.ExperimentalToolkitApi

/**
 * A widget that provides a user interface for the user to enter a search query and submit a request
 * to a search provider. Shows a list of query suggestions or results, if available, and allows the
 * user to pick a suggestion or result to launch into.
 */
@Composable
@ExperimentalToolkitApi
fun Search(
    query: String,
    onQueryChanged: (query: String) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(50),
    elevation: Dp = 4.dp,
    color: Color = MaterialTheme.colors.surface,
    placeholder: String? = null,
    keyboardActions: KeyboardActions = KeyboardActions(),
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    Surface(
        shape = shape,
        modifier = Modifier
            .scale(0.85f)
            .then(modifier),
        elevation = elevation,
        color = color,
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            placeholder = {
                if (placeholder != null)
                    Text(text = placeholder)
            },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = trailingIcon,
            keyboardActions = keyboardActions,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = androidx.compose.ui.text.input.ImeAction.Search)
        )
    }

}