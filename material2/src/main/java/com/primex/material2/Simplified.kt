@file:Suppress("NOTHING_TO_INLINE")

package com.primex.material2


import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.DialogProperties
import com.primex.material2.dialog.BottomSheetDialogProperties


@Composable
inline fun Label(
    text: AnnotatedString,
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
inline fun Label(
    text: String,
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
inline fun Header(
    text: AnnotatedString,
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

@NonRestartableComposable
@Composable
inline fun Header(
    text: String,
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
    text: AnnotatedString,
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
inline fun Caption(
    text: String,
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
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
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
                LocalContentColor.current.copy(ContentAlpha.medium)
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
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
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
                LocalContentColor.current.copy(ContentAlpha.medium)
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
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
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
                LocalContentColor.current.copy(ContentAlpha.medium)
            else
                LocalContentColor.current.copy(ContentAlpha.disabled)
        } ?: Color.Unspecified
        Icon(painter = painter, contentDescription = contentDescription, tint = color)
    }
}

/**
 * A single line [Label] that is animated using the [AnimatedContent]
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
inline fun AnimatedLabel(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    noinline transitionSpec: AnimatedContentScope<AnnotatedString>.() -> ContentTransform = {
        slideInVertically { height -> height } + fadeIn() with
                slideOutVertically { height -> -height } + fadeOut()
    }
) {
    AnimatedContent(
        targetState = text,
        transitionSpec = transitionSpec,
        modifier = modifier,
        content = {
            Label(
                text = it,
                style = style,
                color = color,
                fontSize = fontSize,
                fontWeight = fontWeight,
                textAlign = textAlign
            )
        }
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
inline fun AnimatedLabel(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    noinline transitionSpec: AnimatedContentScope<AnnotatedString>.() -> ContentTransform = {
        slideInVertically { height -> height } + fadeIn() with
                slideOutVertically { height -> -height } + fadeOut()
    }
) {
    AnimatedLabel(
        text = AnnotatedString(text),
        modifier = modifier,
        style = style,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        textAlign = textAlign,
        transitionSpec = transitionSpec
    )
}

@Composable
inline fun DropDownMenuItem(
    title: AnnotatedString,
    noinline onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: Painter? = null,
    enabled: Boolean = true,
    contentPadding: PaddingValues = MenuDefaults.DropdownMenuItemContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    DropdownMenuItem(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        contentPadding = contentPadding,
        interactionSource = interactionSource
    ) {
        if (leading != null)
            Icon(
                painter = leading,
                contentDescription = null,
                //  modifier = Modifier.padding(start = 16.dp)
            )

        // the text
        Label(
            text = title,
            modifier = Modifier.padding(horizontal = 16.dp),
            fontWeight = FontWeight.Medium
        )
    }
}


@Composable
inline fun DropDownMenuItem(
    title: String,
    noinline onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: Painter? = null,
    enabled: Boolean = true,
    contentPadding: PaddingValues = MenuDefaults.DropdownMenuItemContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) = DropDownMenuItem(
    title = AnnotatedString(title),
    onClick = onClick,
    modifier = modifier,
    leading = leading,
    enabled = enabled,
    contentPadding = contentPadding,
    interactionSource = interactionSource,
)

@Composable
inline fun Button(
    label: AnnotatedString,
    noinline onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: Painter? = null,
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
        if (leading != null)
            Icon(
                painter = leading,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
        Label(text = label)
    }
)

@Composable
inline fun Button(
    label: String,
    noinline onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: Painter? = null,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    elevation: ButtonElevation? = ButtonDefaults.elevation(),
    shape: Shape = MaterialTheme.shapes.small,
    border: BorderStroke? = null,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
) = Button(
    onClick = onClick,
    label = AnnotatedString(label),
    modifier = modifier,
    leading = leading,
    enabled = enabled,
    interactionSource = interactionSource,
    elevation = elevation,
    shape = shape,
    border = border,
    colors = colors,
    contentPadding = contentPadding,
)

@Composable
inline fun OutlinedButton(
    label: AnnotatedString,
    noinline onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: Painter? = null,
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
        if (leading != null)
            Icon(
                painter = leading,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
        Label(text = label)
    },
)

@Composable
inline fun OutlinedButton(
    label: String,
    noinline onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: Painter? = null,
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
    label = AnnotatedString(label),
    leading = leading
)

@Composable
inline fun TextButton(
    label: AnnotatedString,
    noinline onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: Painter? = null,
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
        if (leading != null)
            Icon(
                painter = leading,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
        Label(text = label)
    }
)

@Composable
inline fun TextButton(
    label: String,
    noinline onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: Painter? = null,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    elevation: ButtonElevation? = null,
    shape: Shape = MaterialTheme.shapes.small,
    border: BorderStroke? = null,
    colors: ButtonColors = ButtonDefaults.textButtonColors(),
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
) = TextButton(
    label = AnnotatedString(label),
    onClick = onClick,
    modifier = modifier,
    leading = leading,
    enabled = enabled,
    interactionSource = interactionSource,
    elevation = elevation,
    shape = shape,
    border = border,
    colors = colors,
    contentPadding = contentPadding
)

@Composable
inline fun Dialog(
    expanded: Boolean,
    noinline onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(),
    noinline content: @Composable () -> Unit
) {
    if (expanded)
        androidx.compose.ui.window.Dialog(
            onDismissRequest = onDismissRequest,
            properties = properties,
            content = content
        )
}


@Composable
@ExperimentalComposeUiApi
inline fun BottomSheetDialog(
    expanded: Boolean,
    noinline onDismissRequest: () -> Unit,
    properties: BottomSheetDialogProperties = BottomSheetDialogProperties(dismissWithAnimation = true),
    noinline content: @Composable () -> Unit
) {
    if (expanded)
        com.primex.material2.dialog.BottomSheetDialog(
            onDismissRequest = onDismissRequest,
            properties = properties,
            content = content,
        )
}