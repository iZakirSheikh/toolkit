@file:Suppress("NOTHING_TO_INLINE")

package com.primex.material2


import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.DialogProperties
import com.primex.core.withSpanStyle


@Composable
inline fun Label(
    text: CharSequence,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = 1,
    style: TextStyle = LocalTextStyle.current,
) {
    Text(
        text = text,
        modifier = modifier,
        style = style,
        maxLines = maxLines,
        color = color,
        overflow = TextOverflow.Ellipsis,
        fontWeight = fontWeight,
        fontSize = fontSize,
        textAlign = textAlign
    )
}


@Composable
@Deprecated("Use the new Text with CharSequance.")
inline fun Header(
    text: CharSequence,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.h6,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = style.fontSize,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = 1,
) = Label(
    modifier = modifier,
    style = style,
    color = color,
    maxLines = maxLines,
    fontWeight = fontWeight,
    text = text,
    fontSize = fontSize,
    textAlign = textAlign
)


@Composable
inline fun Caption(
    text: CharSequence,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = 1,
) = Label(
    text = text,
    modifier = modifier,
    style = MaterialTheme.typography.caption,
    color = color,
    fontWeight = fontWeight,
    textAlign = textAlign,
    maxLines = maxLines
)

@Composable
inline fun IconButton(
    imageVector: ImageVector,
    noinline onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null,
    interactionSource: MutableInteractionSource? = null,
    tint: Color? = Color.Unspecified,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        interactionSource = interactionSource
    ) {
        val color = tint?.takeOrElse {
            if (enabled)
                LocalContentColor.current
            else
                LocalContentColor.current.copy(ContentAlpha.disabled)
        } ?: Color.Unspecified
        Icon(imageVector = imageVector, contentDescription = contentDescription, tint = color)
    }
}

@Composable
inline fun IconButton(
    bitmap: ImageBitmap,
    noinline onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null,
    interactionSource: MutableInteractionSource? = null,
    tint: Color? = Color.Unspecified,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        interactionSource = interactionSource
    ) {
        val color = tint?.takeOrElse {
            if (enabled)
                LocalContentColor.current
            else
                LocalContentColor.current.copy(ContentAlpha.disabled)
        } ?: Color.Unspecified
        Icon(bitmap = bitmap, contentDescription = contentDescription, tint = color)
    }
}

@Composable
inline fun IconButton(
    painter: Painter,
    noinline onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null,
    interactionSource: MutableInteractionSource? = null,
    tint: Color? = Color.Unspecified,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        interactionSource = interactionSource
    ) {
        val color = tint?.takeOrElse {
            if (enabled)
                LocalContentColor.current
            else
                LocalContentColor.current.copy(ContentAlpha.disabled)
        } ?: Color.Unspecified
        Icon(painter = painter, contentDescription = contentDescription, tint = color)
    }
}


/**
 * A composable function for creating a menu item with optional title, subtitle, icon, and click action.
 *
 * @param title The main text content of the menu item.
 * @param onClick The callback to be invoked when the menu item is clicked.
 * @param modifier Optional [Modifier] to apply to the menu item.
 * @param subtitle Optional secondary text content of the menu item.
 * @param icon Optional [ImageVector] icon to be displayed alongside the menu item.
 * @param enabled Whether the menu item is interactive and can be clicked.
 *
 * @sample MenuItem(
 *     title = "Settings",
 *     onClick = { /* Handle click action */ },
 *     modifier = Modifier.padding(8.dp),
 *     subtitle = "Configure app preferences",
 *     icon = Icons.Default.Settings,
 *     enabled = true
 * )
 *
 * @see Composable
 */
@Composable
inline fun DropDownMenuItem(
    title: CharSequence,
    noinline onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: CharSequence? = null,
    icon: Painter? = null,
    enabled: Boolean = true,
    contentPadding: PaddingValues = MenuDefaults.DropdownMenuItemContentPadding,
    interactionSource: MutableInteractionSource? = null,
) {
    DropdownMenuItem(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        contentPadding = contentPadding,
        interactionSource = interactionSource
    ) {
        if (icon != null)
            Icon(
                painter = icon,
                contentDescription = null,
                //  modifier = Modifier.padding(start = 16.dp)
            )

        // the text
        Text(
            text = buildAnnotatedString {
                append(title)
                if (subtitle == null) return@buildAnnotatedString
                withSpanStyle(
                    color = LocalContentColor.current.copy(ContentAlpha.disabled),
                    fontSize = 11.sp,
                    block = {
                        append("\n" + subtitle)
                    }
                )
            },
            modifier = Modifier.padding(horizontal = 16.dp),
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
inline fun Button(
    label: CharSequence,
    noinline onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    elevation: ButtonElevation? = ButtonDefaults.elevation(),
    shape: Shape = MaterialTheme.shapes.small,
    border: BorderStroke? = null,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
) = Button(onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    interactionSource = interactionSource,
    elevation = elevation,
    shape = shape,
    border = border,
    colors = colors,
    contentPadding = contentPadding,
    content = {
        if (icon != null)
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.padding(end = ButtonDefaults.IconSpacing)
            )
        Label(text = label)
    }
)

@Composable
inline fun OutlinedButton(
    label: CharSequence,
    noinline onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    elevation: ButtonElevation? = null,
    shape: Shape = MaterialTheme.shapes.small,
    border: BorderStroke? = ButtonDefaults.outlinedBorder,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding
) = OutlinedButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    interactionSource = interactionSource,
    elevation = elevation,
    shape = shape,
    border = border,
    colors = colors,
    contentPadding = contentPadding,
    content = {
        if (icon != null)
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.padding(end = ButtonDefaults.IconSpacing)
            )
        Label(text = label)
    },
)

@Composable
inline fun TextButton(
    label: CharSequence,
    noinline onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    elevation: ButtonElevation? = null,
    shape: Shape = MaterialTheme.shapes.small,
    border: BorderStroke? = null,
    colors: ButtonColors = ButtonDefaults.textButtonColors(),
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
) = TextButton(onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    interactionSource = interactionSource,
    elevation = elevation,
    shape = shape,
    border = border,
    colors = colors,
    contentPadding = contentPadding,
    content = {
        if (icon != null)
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.padding(end = ButtonDefaults.IconSpacing)
            )
        Label(text = label)
    }
)



@ExperimentalMaterialApi
@Composable
inline fun OutlinedButton2(
    label: CharSequence,
    noinline onClick: () -> Unit,
    modifier: Modifier = Modifier,
    crown: Painter? = null,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    elevation: ButtonElevation? = null,
    shape: Shape = MaterialTheme.shapes.small,
    border: BorderStroke? = ButtonDefaults.outlinedBorder,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding
) = OutlinedButton2(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    interactionSource = interactionSource,
    elevation = elevation,
    shape = shape,
    border = border,
    colors = colors,
    contentPadding = contentPadding,
    content = {
        if (crown != null)
            Icon(
                painter = crown,
                contentDescription = null,
                modifier = Modifier.padding(bottom = ButtonDefaults.IconSpacing)
            )
        Label(text = label)
    },
)

@ExperimentalMaterialApi
@Composable
inline fun TextButton2(
    label: CharSequence,
    noinline onClick: () -> Unit,
    modifier: Modifier = Modifier,
    crown: Painter? = null,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    elevation: ButtonElevation? = null,
    shape: Shape = MaterialTheme.shapes.small,
    border: BorderStroke? = null,
    colors: ButtonColors = ButtonDefaults.textButtonColors(),
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
) = TextButton2(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    interactionSource = interactionSource,
    elevation = elevation,
    shape = shape,
    border = border,
    colors = colors,
    contentPadding = contentPadding,
    content = {
        if (crown != null)
            Icon(
                painter = crown,
                contentDescription = null,
                modifier = Modifier.padding(bottom = ButtonDefaults.IconSpacing)
            )
        Label(text = label)
    },
)


/**
 * A simple extension of [OutlinedButton2] with direct support of [label] and [crown].
 * @see OutlinedButton2
 * @param label The string label.
 * @param crown The top icon.
 */
@ExperimentalMaterialApi
@Composable
inline fun Button2(
    label: CharSequence,
    noinline onClick: () -> Unit,
    modifier: Modifier = Modifier,
    crown: Painter? = null,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    elevation: ButtonElevation? = ButtonDefaults.elevation(),
    shape: Shape = MaterialTheme.shapes.small,
    border: BorderStroke? = null,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding
) = Button2(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    interactionSource = interactionSource,
    elevation = elevation,
    shape = shape,
    border = border,
    colors = colors,
    contentPadding = contentPadding,
    content = {
        if (crown != null)
            Icon(
                painter = crown,
                contentDescription = null,
                modifier = Modifier.padding(bottom = ButtonDefaults.IconSpacing)
            )
        Label(text = label)
    },
)

/**
 * ### Advanced Text Composable
 * An enhanced version of the original `Text` composable that allows direct input of a
 * `CharSequence`, reducing the need for separate composable. This advanced version eliminates the
 * requirement to convert the text to a `String` or `AnnotatedString`.
 *
 * @see androidx.compose.material3.Text
 */
@Composable
inline fun Text(
    text: CharSequence,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    inlineContent: Map<String, InlineTextContent> = mapOf(),
    noinline onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current
) {
    when (text) {
        is AnnotatedString -> androidx.compose.material.Text(
            text,
            modifier,
            color,
            fontSize,
            fontStyle,
            fontWeight,
            fontFamily,
            letterSpacing,
            textDecoration,
            textAlign,
            lineHeight,
            overflow,
            softWrap,
            maxLines,
            minLines,
            inlineContent,
            onTextLayout,
            style
        )

        is String -> Text(
            text = text,
            modifier,
            color,
            fontSize,
            fontStyle,
            fontWeight,
            fontFamily,
            letterSpacing,
            textDecoration,
            textAlign,
            lineHeight,
            overflow,
            softWrap,
            maxLines,
            minLines,
            onTextLayout,
            style
        )

        else -> error("$text must be either AnnotatedString or String!!")
    }
}


/**
 * Creates a customizable dialog with extended features beyond the standard Material dialog.
 *
 * This composable allows you to create dialogs with custom shapes, borders, and other
 * visual properties, providing more control over their appearance.
 *
 * @param expanded [Boolean] Whether the dialog is currently expanded and visible.
 * @param onDismissRequest [() -> Unit] A callback function invoked when the user dismisses the dialog.
 * @param modifier [Modifier] Optional modifier to apply to the dialog's layout.
 * @param properties [DialogProperties] Configuration for the dialog's behavior and appearance.
 * @param backgroundColor [Color] The background color of the dialog.
 * @param contentColor [Color] The color of the content within the dialog.
 * @param shape [Shape] The shape of the dialog's container.
 * @param border [BorderStroke?] Optional border to apply to the dialog's edges.
 * @param content [() -> Unit] The content of the dialog, defined as a composable function.
 */
@Composable
inline fun Dialog(
    expanded: Boolean,
    noinline onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(),
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = MaterialTheme.colors.onSurface,
    shape: Shape = MaterialTheme.shapes.small,
    border: BorderStroke? = null,
    noinline content: @Composable () -> Unit
) {

    if (expanded)
        androidx.compose.ui.window.Dialog(
            onDismissRequest = onDismissRequest,
            properties = properties,
            content = {
                Surface(
                    modifier = modifier,
                    color = backgroundColor,
                    contentColor = contentColor,
                    shape = shape,
                    border = border,
                    content = content
                )
            },
        )
}