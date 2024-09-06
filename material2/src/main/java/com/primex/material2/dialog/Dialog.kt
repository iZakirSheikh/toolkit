package com.primex.material2.dialog


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.primex.core.JetBlack
import com.primex.core.SignalWhite
import com.primex.material2.Caption
import com.primex.material2.Label

private val PaddingMedium = 8.dp
private val PaddingSmall = 4.dp
private val PaddingNormal = 16.dp
private val PaddingLarge = 32.dp


private val DialogMinHeight = 100.dp


@Deprecated("Use Dialog from android instead.")
@Composable
fun BaseDialog(
    title: String,
    onDismissRequest: () -> Unit,
    subtitle: String? = null,
    vectorIcon: ImageVector? = null,
    properties: DialogProperties = DialogProperties(),
    topBarBackgroundColor: Color = Color.Unspecified,
    topBarContentColor: Color = contentColorFor(backgroundColor = topBarBackgroundColor),
    topBarElevation: Dp = 0.dp,
    actions: @Composable RowScope.() -> Unit = {},
    button1: @Composable (() -> Unit)? = null,
    button2: @Composable (() -> Unit)? = null,
    bottom: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest, properties = properties) {
        Surface(
            modifier = Modifier
                .heightIn(min = DialogMinHeight)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colors.surface
        ) {
            Column {

                val title =
                    @Composable {
                        Title(title = title, subtitle = subtitle)
                    }

                val bgColor =
                    topBarBackgroundColor.takeOrElse { if (MaterialTheme.colors.isLight) Color.SignalWhite else Color.JetBlack }

                TopAppBar(
                    title = title,
                    backgroundColor = bgColor,
                    contentColor = topBarContentColor,
                    elevation = topBarElevation,
                    actions = actions,
                    navigationIcon = vectorIcon?.let {
                        @Composable {
                            Icon(
                                imageVector = it,
                                contentDescription = "dialog icon",
                                modifier = Modifier.padding(start = PaddingMedium)
                            )
                        }
                    }
                )

                // the actual content
                content()

                // controls
                if (button1 != null || button2 != null)
                    Row(
                        modifier = Modifier
                            .padding(
                                bottom = PaddingMedium,
                                end = PaddingMedium,
                                start = PaddingMedium
                            )
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        button1?.invoke()
                        button2?.invoke()
                    }
                // the bottom bar.
                bottom?.invoke()
            }
        }
    }
}

@Composable
private fun Title(title: String, subtitle: String?, modifier: Modifier = Modifier) {
    when (subtitle != null) {
        true -> Column(modifier = modifier) {
            Label(text = title)
            Caption(text = subtitle)
        }
        else -> Label(text = title)
    }
}


private val defaultSizeModifier = Modifier
    // Preferred min and max width used during the intrinsic measurement.
    .sizeIn(
        maxWidth = 130.dp,
    )


@Deprecated("Use Dialog from android instead.")
@Composable
fun PrimeDialog(
    title: String,
    onDismissRequest: () -> Unit,
    subtitle: String? = null,
    vectorIcon: ImageVector? = null,
    properties: DialogProperties = DialogProperties(),
    bottom: @Composable (() -> Unit)? = null,
    topBarBackgroundColor: Color = Color.Unspecified,
    topBarContentColor: Color = contentColorFor(backgroundColor = topBarBackgroundColor),
    topBarElevation: Dp = 0.dp,
    imageButton: Pair<ImageVector, () -> Unit>? = null,
    button1: Pair<String, () -> Unit>? = null,
    button2: Pair<String, () -> Unit>? = null,
    content: @Composable () -> Unit
) {
    BaseDialog(
        title = title,
        onDismissRequest = onDismissRequest,
        subtitle = subtitle,
        vectorIcon = vectorIcon,
        content = content,
        properties = properties,
        topBarBackgroundColor = topBarBackgroundColor,
        topBarContentColor = topBarContentColor,
        topBarElevation = topBarElevation,
        bottom = bottom,
        actions = {
            imageButton?.let {
                IconButton(onClick = it.second) {
                    Icon(imageVector = it.first, contentDescription = null)
                }
            }
        },
        button1 = button1?.let {
            @Composable {
                TextButton(onClick = it.second, modifier = defaultSizeModifier) {
                    Label(
                        text = it.first,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        button2 = button2?.let {
            @Composable {
                TextButton(onClick = it.second, modifier = defaultSizeModifier) {
                    Label(
                        text = it.first,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
    )
}


@Deprecated("Use Dialog from android instead.")
@Composable
fun AlertDialog(
    title: String,
    message: String,
    subtitle: String? =null,
    vectorIcon: ImageVector? = null,
    topBarBackgroundColor: Color = Color.Unspecified,
    topBarContentColor: Color = contentColorFor(backgroundColor = topBarBackgroundColor),
    onDismissRequest: (Boolean) -> Unit
) {
    PrimeDialog(
        title = title,
        subtitle = subtitle,
        vectorIcon = vectorIcon,
        topBarContentColor = topBarContentColor,
        topBarBackgroundColor = topBarBackgroundColor,
        onDismissRequest = { onDismissRequest(false) },
        button2 = "Confirm" to { onDismissRequest(true) },
        button1 = "Dismiss" to { onDismissRequest(false) }) {
        Surface(modifier = Modifier.padding(horizontal = PaddingNormal, vertical = PaddingMedium)) {
            Text(text = message, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun TextInputDialog(
    title: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    textFieldShape: Shape = RoundedCornerShape(50),
    defaultValue: String = "",
    label: String? = null,
    subtitle: String? = null,
    topBarBackgroundColor: Color = Color.Unspecified,
    topBarContentColor: Color = contentColorFor(backgroundColor = topBarBackgroundColor),
    vectorIcon: ImageVector? = null,
    onDismissRequest: (String?) -> Unit,
) {
    var text by remember {
        mutableStateOf(
            TextFieldValue(
                defaultValue,
                selection = TextRange(0, defaultValue.length)
            )
        )
    }

    PrimeDialog(
        title = title,
        subtitle = subtitle,
        vectorIcon = vectorIcon,
        topBarBackgroundColor = topBarBackgroundColor,
        topBarContentColor = topBarContentColor,
        onDismissRequest = { onDismissRequest(null) },
        button1 = "Dismiss" to { onDismissRequest(null) },
        button2 = "Confirm" to { onDismissRequest(text.text) },
    ) {
        val focusRequester = remember { FocusRequester() }

        val style = MaterialTheme.typography.h5.copy(
            fontWeight = FontWeight.Bold
        )

        val modifier =
            Modifier
                .focusRequester(focusRequester = focusRequester)
                .padding(PaddingLarge)
                .fillMaxWidth()

        val trailing =
            @Composable {
                IconButton(onClick = { text = TextFieldValue("") }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null)
                }
            }

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = modifier,
            textStyle = style,
            keyboardOptions = keyboardOptions,
            leadingIcon = {
                Icon(imageVector = Icons.Outlined.Edit, contentDescription = null)
            },
            label = label?.let {
                @Composable {
                    Label(text = it)
                }
            },
            shape = textFieldShape,
            trailingIcon =  trailing
        )

        DisposableEffect(key1 = Unit){
            focusRequester.requestFocus()
            onDispose {  }
        }
    }
}