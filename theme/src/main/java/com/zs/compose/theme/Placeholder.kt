package com.zs.compose.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zs.compose.theme.text.ProvideTextStyle

private val PLACE_HOLDER_ICON_BOX_SIZE = 192.dp
private val PLACE_HOLDER_ICON_BOX_DEFAULT_SIZE = 56.dp

@Composable
private fun Vertical(
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    message: @Composable (() -> Unit)? = null,
    action: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit),
) {
    Column(
        modifier = Modifier

            // optional full max size
            .fillMaxSize()

            // add a padding normal
            .padding(horizontal = 32.dp, vertical = 16.dp)
            .then(modifier),

        // The Placeholder will be Placed in the middle of the available space
        // both h/v
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // place icon if available
        if (icon != null) {
            Box(
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    //  .align(Alignment.CenterHorizontally)
                    .size(PLACE_HOLDER_ICON_BOX_SIZE),
                propagateMinConstraints = true
            ) {
                icon()
            }
        }

        // Place Title
        ProvideTextStyle(
            value = AppTheme.typography.headline2.copy(textAlign = TextAlign.Center),
            content = title,
        )

        //Message
        if (message != null) {
            Spacer(modifier = Modifier.padding(vertical = 8.dp))
            CompositionLocalProvider(
                LocalContentColor provides LocalContentColor.current.copy(
                    ContentAlpha.medium,
                )
            ) {
                ProvideTextStyle(
                    value = AppTheme.typography.body2.copy(textAlign = TextAlign.Center),
                    content = message,
                )
            }
        }

        //Action
        if (action != null) {
            Spacer(modifier = Modifier.padding(vertical = 32.dp))
            action()
        }
    }
}

@Composable
private fun Horizontal(
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    message: @Composable (() -> Unit)? = null,
    action: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit),
) {
    Row(
        modifier = Modifier
            // optional full max size
            .fillMaxSize()
            // add a padding normal
            .padding(horizontal = 32.dp, vertical = 16.dp)
            .then(modifier),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Spacer(modifier = Modifier.weight(0.15f))

        Column(
            modifier = Modifier
                .padding(end = 32.dp)
                .weight(0.7f, fill = false)
        ) {

            // Place Title
            ProvideTextStyle(
                value = AppTheme.typography.headline2,
                content = title
            )

            //Message
            if (message != null) {
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                CompositionLocalProvider(
                    LocalContentColor provides LocalContentColor.current.copy(
                        alpha = ContentAlpha.medium,
                    )
                ) {
                    ProvideTextStyle(
                        value = AppTheme.typography.body2,
                        content = message
                    )
                }
            }

            //Action
            if (action != null) {
                Spacer(modifier = Modifier.padding(top = 32.dp))
                action()
            }
        }

        // place icon if available
        if (icon != null) {
            Box(
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    //  .align(Alignment.CenterHorizontally)
                    .size(PLACE_HOLDER_ICON_BOX_SIZE),
                propagateMinConstraints = true
            ) {
                icon()
            }
        }

        Spacer(modifier = Modifier.weight(0.15f))
    }
}

@Composable
@NonRestartableComposable
@ExperimentalThemeApi
fun Placeholder(
    title: @Composable (() -> Unit),
    modifier: Modifier = Modifier,
    vertical: Boolean = true,
    icon: @Composable (() -> Unit)? = null,
    message: @Composable (() -> Unit)? = null,
    action: @Composable (() -> Unit)? = null,
) {
    when (vertical) {
        true -> Vertical(modifier, icon, message, action, title)
        else -> Horizontal(modifier, icon, message, action, title)
    }
}