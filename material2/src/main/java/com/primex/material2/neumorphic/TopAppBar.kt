package com.primex.material2.neumorphic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.primex.core.ExperimentalToolkitApi
import com.primex.core.shadow.SpotLight

// TODO: this should probably be part of the touch target of the start and end icons, clarify this
private val AppBarHorizontalPadding = 4.dp

// Start inset for the title when there is no navigation icon provided
private val TitleInsetWithoutIcon = Modifier.width(16.dp - AppBarHorizontalPadding)

// Start inset for the title when there is a navigation icon provided
private val TitleIconModifier = Modifier
    .fillMaxHeight()
    .width(72.dp - AppBarHorizontalPadding)

private val AppBarHeight = 56.dp

@Composable
private fun TopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(AppBarDefaults.ContentPadding)
            .height(AppBarHeight),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (navigationIcon == null) {
            Spacer(TitleInsetWithoutIcon)
        } else {
            Row(TitleIconModifier, verticalAlignment = Alignment.CenterVertically) {
                CompositionLocalProvider(
                    LocalContentAlpha provides ContentAlpha.high,
                    content = navigationIcon
                )
            }
        }

        Row(
            Modifier
                .fillMaxHeight()
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            com.primex.material2.ProvideTextStyle(
                style = MaterialTheme.typography.body1,
                fontWeight = FontWeight.SemiBold,
                alpha = ContentAlpha.high,
                content = title
            )
        }

        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Row(
                Modifier.fillMaxHeight(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                content = actions
            )
        }
    }
}


@Composable
@NonRestartableComposable
private fun TopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    backgroundColor: Color = MaterialTheme.colors.primarySurface,
    contentColor: Color = contentColorFor(backgroundColor),
    elevation: Dp = AppBarDefaults.TopAppBarElevation
) {
    Surface(
        Modifier
            .padding(horizontal = 10.dp)
            .scale(0.85f)
            .then(modifier),
        color = backgroundColor,
        contentColor = contentColor,
        elevation = elevation,
        shape = shape
    ) {
        TopAppBar(
            title = title,
            navigationIcon = navigationIcon,
            actions = actions
        )
    }
}


@ExperimentalToolkitApi
@Composable
@NonRestartableComposable
fun NeumorphicTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
    spotLight: SpotLight = SpotLight.TOP_LEFT,
    lightShadowColor: Color = DefaultLightShadowColor,
    darkShadowColor: Color = DefaultDarkShadowColor,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    backgroundColor: Color = MaterialTheme.colors.background,
    contentColor: Color = contentColorFor(backgroundColor),
    elevation: Dp = AppBarDefaults.TopAppBarElevation
) {
    Neumorphic(
        modifier
            .padding(horizontal = 10.dp)
            .scale(0.85f),
        color = backgroundColor,
        contentColor = contentColor,
        elevation = elevation,
        shape = shape,
        spotLight = spotLight,
        lightShadowColor = lightShadowColor,
        darkShadowColor = darkShadowColor
    ) {
        TopAppBar(
            title = title,
            navigationIcon = navigationIcon,
            actions = actions
        )
    }
}
   