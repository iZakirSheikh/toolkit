@file:Suppress("NOTHING_TO_INLINE")
@file:OptIn(ExperimentalFoundationApi::class, ExperimentalThemeApi::class)

/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by 2024 on 14-10-2024.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.prime.toolkit.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Camera
import androidx.compose.material.icons.outlined.DataObject
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.outlined.Recycling
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.Textsms
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.prime.toolkit.R
import com.prime.toolkit.core.background
import com.prime.toolkit.core.observe
import com.prime.toolkit.core.rememberBackgroundProvider
import com.zs.compose.foundation.fadingEdge
import com.zs.compose.foundation.plus
import com.zs.compose.foundation.textResource
import com.zs.compose.theme.AppTheme
import com.zs.compose.theme.BaseListItem
import com.zs.compose.theme.Button
import com.zs.compose.theme.ButtonDefaults
import com.zs.compose.theme.Chip
import com.zs.compose.theme.ChipDefaults
import com.zs.compose.theme.Colors
import com.zs.compose.theme.ExperimentalThemeApi
import com.zs.compose.theme.FilledTonalButton
import com.zs.compose.theme.Icon
import com.zs.compose.theme.IconButton
import com.zs.compose.theme.LocalWindowSize
import com.zs.compose.theme.Preference
import com.zs.compose.theme.SliderPreference
import com.zs.compose.theme.Surface
import com.zs.compose.theme.SwitchPreference
import com.zs.compose.theme.TextButton
import com.zs.compose.theme.TonalIconButton
import com.zs.compose.theme.WindowSize.Category
import com.zs.compose.theme.adaptive.HorizontalTwoPaneStrategy
import com.zs.compose.theme.adaptive.SinglePaneStrategy
import com.zs.compose.theme.adaptive.TwoPane
import com.zs.compose.theme.adaptive.content
import com.zs.compose.theme.appbar.AppBarDefaults
import com.zs.compose.theme.minimumInteractiveComponentSize
import com.zs.compose.theme.text.Header
import com.zs.compose.theme.text.Label
import com.zs.compose.theme.text.Text
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.PaddingValues as Padding

private const val TAG = "Settings"

// The max width of the secondary pane
private val sPaneMaxWidth = 280.dp

// Used to style individual items within a preference section.
private val TopTileShape = RoundedCornerShape(24.dp, 24.dp, 0.dp, 0.dp)
private val CentreTileShape = RectangleShape
private val BottomTileShape = RoundedCornerShape(0.dp, 0.dp, 24.dp, 24.dp)
private val SingleTileShape = RoundedCornerShape(24.dp)

private val Colors.tileBackgroundColor
    @ReadOnlyComposable @Composable inline get() = background(elevation = 1.dp)

private fun PrefText(
    title: String,
    summery: String
) = buildAnnotatedString {
    append(title)
    withStyle(SpanStyle(color = Color.DarkGray)) {
        appendLine(summery)
    }
}

/**
 * Represents the group header of [Preference]s
 */
@Composable
@NonRestartableComposable
private fun GroupHeader(
    text: CharSequence,
    modifier: Modifier = Modifier,
    padding: Padding? = null,
) = Text(
    text = text,
    modifier = Modifier
        .let() {
            if (padding == null)
                it.padding(horizontal = 28.dp, vertical = 32.dp)
            else
                it.padding(padding)
        }
        .then(modifier),
    color = AppTheme.colors.accent,
    style = AppTheme.typography.title3
)

private const val CONTENT_TYPE_HEADER = "header"
private const val CONTENT_TYPE_PREF = "preference"

/** Represents the settings of General */
private inline fun LazyListScope.General(
) {
    // Recycle Bin

    item(contentType = CONTENT_TYPE_PREF) {
        var enabled by remember { mutableStateOf(false) }
        SwitchPreference(
            text = PrefText("Recycle Bin", "Enable to move items to recycle bin."),
            checked = enabled,
            onCheckedChange = { enabled= !enabled },
            icon = Icons.Outlined.Recycling,
            modifier = Modifier.background(AppTheme.colors.tileBackgroundColor, TopTileShape),
        )
    }

    // Legacy Artwork Method
    item(contentType = CONTENT_TYPE_PREF) {

        var enabled by remember { mutableStateOf(false) }
        SwitchPreference(
            text = PrefText("Legacy Artwork Method", "Enable to use legacy artwork method."),
            checked = enabled,
            onCheckedChange = { enabled= !enabled },
            icon = Icons.Outlined.Camera,
            modifier = Modifier.background(AppTheme.colors.tileBackgroundColor, CentreTileShape),
        )

    }

    // Exclude Track Duration
    // The duration from which below tracks are excluded from the library.
    item(contentType = CONTENT_TYPE_PREF) {
        var exclude by remember { mutableStateOf(0) }
        SliderPreference(
            text = PrefText("Exclude", "Exclude tracks below duration"),
            value = exclude.toFloat(),
            onRequestChange = { exclude = it.toInt() },
            valueRange = 0f..100f,
            steps = 5,
            icon = Icons.Outlined.Straighten,
            preview = {
                Label(
                    text = "${it.roundToInt()}s",
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .wrapContentSize(Alignment.Center)
                )
            },
            modifier = Modifier.background(AppTheme.colors.tileBackgroundColor, CentreTileShape),
        )
    }

    // Use Inbuilt Audio FX
    // Whether to use inbuilt audio effects or inApp.
    item(contentType = CONTENT_TYPE_PREF) {
        var enabled by remember { mutableStateOf(false) }
        SwitchPreference(
            text = PrefText("Inbuilt Audio FX", "Enable to use inbuilt audio effects."),
            checked = enabled,
            onCheckedChange = { enabled= !enabled },
            icon = Icons.Outlined.Tune,
            modifier = Modifier.background(AppTheme.colors.tileBackgroundColor, BottomTileShape)
        )
    }
}

/** Represents items that are related to appearence of the App. */
private inline fun LazyListScope.Appearence(
) {


    // Colorization Strategy
    item(contentType = CONTENT_TYPE_PREF) {
        var colorizationStrategy by remember { mutableStateOf(false) }
        SwitchPreference(
            checked = colorizationStrategy,
            text = PrefText("Colorization Strategy", "Enable to use colorization strategy."),
            onCheckedChange = { should: Boolean ->
               colorizationStrategy = should
            },
            modifier = Modifier.background(AppTheme.colors.tileBackgroundColor, TopTileShape)
        )
    }



    // Translucent System Bars
    // Whether System Bars are rendered as translucent or Transparent.
    item(contentType = CONTENT_TYPE_PREF) {
        var translucentSystemBars by remember { mutableStateOf(false) }
        SwitchPreference(
            checked = translucentSystemBars,
            text = PrefText("Translucent System Bars", "Enable to use translucent system bars."),
            onCheckedChange = { should: Boolean ->
               translucentSystemBars = should
            },
            modifier = Modifier.background(AppTheme.colors.tileBackgroundColor, CentreTileShape)
        )

    }

    // Hide/Show SystemBars for Immersive View
    // Whether System Bars are hidden for immersive view or not.
    item(contentType = CONTENT_TYPE_PREF) {
        var immersiveView by remember { mutableStateOf(false) }
        SwitchPreference(
            checked = immersiveView,
            text = PrefText("Hide/Show SystemBars for Immersive View", "Enable to hide/show system bars for immersive view."),
            onCheckedChange = { should: Boolean ->
               immersiveView = should
            },
            modifier = Modifier
                .background(AppTheme.colors.tileBackgroundColor, BottomTileShape)
        )
    }
}

@Composable
private fun Sponsor(modifier: Modifier = Modifier) {
    BaseListItem(
        modifier = modifier
            .offset(y = -16.dp)
            .background(AppTheme.colors.tileBackgroundColor, SingleTileShape),
        centerAlign = true,
        contentColor = AppTheme.colors.onBackground,
        // App name.
        overline = {
            Text(
                text = textResource(R.string.app_name),
                style = AppTheme.typography.display3,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Cursive,
                color = AppTheme.colors.onBackground
            )
        },
        // Build version info.
        heading = {
            Text(
                text = "3.0.0-dev",
                style = AppTheme.typography.label3,
                fontWeight = FontWeight.Normal
            )
        },
        // app icon
        leading = {
            Surface(
                color = AppTheme.colors.background(4.dp),
                shape = AppTheme.shapes.large,
                modifier = Modifier.size(64.dp),
                content = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                }
            )
        },
        // RateUs + Buy me a Coffee Button.
        footer = {
            Row(
                modifier = Modifier.padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = {

                    // RateUs
                    FilledTonalButton(
                        "Rate Us",
                        icon = Icons.Outlined.RateReview,
                        onClick = {},
                        colors = ButtonDefaults.filledTonalButtonColors(
                            backgroundColor = AppTheme.colors.background(
                                4.dp
                            )
                        )
                    )

                    // Coffee
                    Button(
                        "Buy me a coffee",
                        icon = Icons.Outlined.DataObject,
                        onClick = { },
                    )
                }
            )
        }
    )
}

@Composable
@NonRestartableComposable
private fun ColumnScope.AboutUs() {
    // The app version and check for updates.
    BaseListItem(
        heading = { Label("Version", fontWeight = FontWeight.Bold) },
        subheading = {
            Label(
                "3.0.0-dev"
            )
        },
        footer = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                content = {
                    TextButton(
                        "Update Toolkit",
                        onClick = {  })
                    TextButton(
                        "Join the beta",
                        onClick = {  },
                        enabled = false
                    )
                }
            )
        },
        leading = {
            Icon(
                imageVector = Icons.Outlined.NewReleases,
                contentDescription = null
            )
        },
    )

    // Privacy Policy
    Preference(
        text = "Privacy Policy",
        icon = Icons.Outlined.PrivacyTip,
        modifier = Modifier
            .clip(AppTheme.shapes.medium)
            .clickable {  },
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val colors = ChipDefaults.chipColors(
            backgroundColor = AppTheme.colors.background(1.dp),
            contentColor = AppTheme.colors.accent
        )
        Chip(
            content = { Label("Rate us") },
            leadingIcon = { Icon(Icons.Outlined.Star, null) },
            onClick = {},
            colors = colors,
            shape = AppTheme.shapes.xSmall
        )

        Chip(
            content = { Label("Share app") },
            leadingIcon = { Icon(Icons.Outlined.Share, null) },
            onClick = {  },
            colors = colors,
            shape = AppTheme.shapes.xSmall
        )
    }
}

/**
 * Represents the settings screen.
 */
@Composable
fun Settings() {
    // Retrieve the current window size
    val (width, _) = LocalWindowSize.current
    // Determine the two-pane strategy based on window width range
    // when in mobile portrait; we don't show second pane;
    val strategy = when {
        // TODO  -Replace with OnePane Strategy when updating TwoPane Layout.
        width < Category.Medium -> SinglePaneStrategy
        else -> HorizontalTwoPaneStrategy(0.5f) // Use horizontal layout with 50% split for large screens
    }

    // obtain the padding of BottomNavBar/NavRail
    val navBarPadding = WindowInsets.content
    val isPhoneLayout = width < Category.Medium
    val provider = rememberBackgroundProvider()
    val topAppBarScrollBehavior = AppBarDefaults.exitUntilCollapsedScrollBehavior()
    val colors = AppTheme.colors
    // Place the content
    // FIXME: Width < 650dp then screen is single pane what if navigationBars are at end.
    TwoPane(
        spacing = 16.dp,
        strategy = strategy,
        topBar = {
            com.prime.toolkit.core.AdaptiveLargeTopAppBar(
                immersive = false,
                title = { Label("Settings") },
                behavior = topAppBarScrollBehavior,
                background = colors.background(provider),
                insets = WindowInsets.systemBars.only(WindowInsetsSides.Top),
                navigationIcon = {
                    Icon(
                        Icons.Default.Settings,
                        null,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    )
                },
                actions = {
                    // Feedback
                    TonalIconButton(
                        icon = Icons.Outlined.AlternateEmail,
                        contentDescription = null,
                        onClick = {  },
                    )
                    // Star on Github
                    TonalIconButton(
                        icon = Icons.Outlined.DataObject,
                        contentDescription = null,
                        onClick = {  },
                    )
                    // Report Bugs on Github.
                    TonalIconButton(
                        icon = Icons.Outlined.BugReport,
                        contentDescription = null,
                        onClick = { },
                    )
                    // Join our telegram channel
                    TonalIconButton(
                        icon = Icons.Outlined.Textsms,
                        contentDescription = null,
                        onClick = { },
                    )
                }
            )
        },
        secondary = {
            // this will not be called when in single pane mode
            // this is just for decoration
            if (strategy is SinglePaneStrategy) return@TwoPane
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(top = 8.dp)
                    .widthIn(max = sPaneMaxWidth)
                    .windowInsetsPadding(WindowInsets.systemBars.union(navBarPadding)),
                content = {
                    Header(
                        "About us",
                        color = AppTheme.colors.accent,
                        // drawDivider = true,
                        style = AppTheme.typography.title3,
                        contentPadding = Padding(
                            vertical = 16.dp,
                            horizontal =8.dp
                        )
                    )
                    AboutUs()
                }
            )
        },
        primary = {
            val state = rememberLazyListState()
            val safeInsets = WindowInsets.systemBars.only(WindowInsetsSides.Vertical)
            LazyColumn(
                state = state,
                // In immersive mode, add horizontal padding to prevent settings from touching the screen edges.
                // Immersive layouts typically have a bottom app bar, so extra padding improves aesthetics.
                // Non-immersive layouts only need vertical padding.
                contentPadding = Padding(
                    if (isPhoneLayout) 28.dp else 8.dp,
                    vertical = 16.dp
                ) +  (WindowInsets.content.union(safeInsets).union(navBarPadding)).asPaddingValues(),
                modifier = Modifier
                    .observe(provider)
                    .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
                    .fadingEdge(
                        state = state,
                        horizontal = false,
                        length = 56.dp
                    ),
                content = {
                    //Sponsor
                    item(contentType = "sponsor") {
                        Sponsor()
                    }

                    // General
                    item(contentType = CONTENT_TYPE_HEADER) {
                        GroupHeader(
                            text = "General",
                            padding = Padding(16.dp, 4.dp, 16.dp, 32.dp)
                        )
                    }
                    General()

                    // Appearance
                    item(CONTENT_TYPE_HEADER) { GroupHeader(text = "Appearance") }
                    Appearence()

                    // AboutUs
                    // Load AboutUs here if this is mobile port
                    if (strategy !is SinglePaneStrategy)
                        return@LazyColumn

                    item(contentType = CONTENT_TYPE_HEADER) {
                        Header(
                            "About us",
                            color = AppTheme.colors.accent,
                            //drawDivider = true,
                            style = AppTheme.typography.title3,
                            contentPadding = Padding(
                                vertical = 16.dp,
                                horizontal = 8.dp
                            )
                        )
                    }

                    item(contentType = "about_us") {
                        Column { AboutUs() }
                    }
                }
            )
        }
    )
}