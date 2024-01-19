package com.primex.material3

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import com.primex.core.ExperimentalToolkitApi

// FixMe: Instead of depending on Button, Built own once required fun which are internal are made public.

/**
 * A variant style of [Button] which exposes [ColumnScope] instead of [RowScope]
 */
@Composable
@ExperimentalToolkitApi
inline fun Button2(
    noinline onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    noinline content: @Composable ColumnScope.() -> Unit
) = Button(
    onClick,
    modifier,
    enabled,
    shape,
    colors,
    elevation,
    border,
    contentPadding,
    interactionSource,
    content = {
        Column(
            Modifier
                .defaultMinSize(
                    minWidth = ButtonDefaults.MinWidth,
                    minHeight = ButtonDefaults.MinHeight
                )
                .padding(contentPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            content = content
        )
    }
)

@ExperimentalToolkitApi
@Composable
inline fun ElevatedButton2(
    noinline onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.elevatedShape,
    colors: ButtonColors = ButtonDefaults.elevatedButtonColors(),
    elevation: ButtonElevation? = ButtonDefaults.elevatedButtonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    noinline content: @Composable ColumnScope.() -> Unit
) = Button2(
    onClick,
    modifier,
    enabled,
    shape,
    colors,
    elevation,
    border,
    contentPadding,
    interactionSource,
    content = content
)


@ExperimentalToolkitApi
@Composable
inline fun FilledTonalButton2(
    noinline onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.filledTonalShape,
    colors: ButtonColors = ButtonDefaults.filledTonalButtonColors(),
    elevation: ButtonElevation? = ButtonDefaults.filledTonalButtonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    noinline content: @Composable ColumnScope.() -> Unit
) = Button2(
    onClick,
    modifier,
    enabled,
    shape,
    colors,
    elevation,
    border,
    contentPadding,
    interactionSource,
    content = content
)

@ExperimentalToolkitApi
@Composable
inline fun OutlinedButton2(
    noinline onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.outlinedShape,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = ButtonDefaults.outlinedButtonBorder,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    noinline content: @Composable ColumnScope.() -> Unit
) = Button2(
    onClick,
    modifier,
    enabled,
    shape,
    colors,
    elevation,
    border,
    contentPadding,
    interactionSource,
    content = content
)

@ExperimentalToolkitApi
@Composable
inline fun TextButton2(
    noinline onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.textShape,
    colors: ButtonColors = ButtonDefaults.textButtonColors(),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    noinline content: @Composable ColumnScope.() -> Unit
) {
    Button2(
        onClick,
        modifier,
        enabled,
        shape,
        colors,
        elevation,
        border,
        contentPadding,
        interactionSource,
        content = content
    )
}