/*
 * Copyright 2025 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 27-01-2025.
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

package com.zs.compose.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

// source: material3 typography
// commit date: latest

private val DefaultLineHeightStyle =
    LineHeightStyle(alignment = LineHeightStyle.Alignment.Center, trim = LineHeightStyle.Trim.None)

internal val DefaultTextStyle = TextStyle.Default.copy(
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = DefaultLineHeightStyle
)

/**
 * Represents a set of typography styles for [AppTheme].
 *
 * This class defines the text styles for various semantic roles, such as display, headline,
 * title, body, and label. Each role has different levels (1, 2, 3) to represent different
 * emphasis or hierarchy.
 *
 * This class is immutable, meaning once an instance is created, it cannot be changed. To modify
 * the typography, use the [copy] function to create a new instance with the desired changes.
 *
 * @property display1 The largest display text style. Typically used for the most important
 * pieces of text on the screen.
 * @param defaultFontFamily The default font family to use for all text styles if not otherwise specified.
 *
 * @property display2 A large display text style.
 * @property display3 A moderately sized display text style.
 * @property headline1 The largest headline text style. Used for high-emphasis titles or headers.
 * @property headline2 A medium headline text style.
 * @property headline3 A smaller headline text style.
 * @property title1 The largest title text style. Used for important, but less prominent than headlines, titles.
 * @property title2 A medium title text style.
 * @property title3 A smaller title text style.
 * @property body1 The largest body text style. Used for the main body text.
 * @property body2 A medium body text style.
 * @property body3 A smaller body text style.
 * @property label1 The largest label text style. Used for text labeling UI elements.
 * @property label2 A medium label text style.
 * @property label3 A smaller label text style.
 *
 * @constructor Creates a new Typography instance with the specified text styles.
 *
 * You can use the default values of each of the types of TextStyles, or you can define your own to be used.
 *

 * */
@Immutable
class Typography internal constructor(
    val display1: TextStyle,
    val display2: TextStyle,
    val display3: TextStyle,
    val headline1: TextStyle,
    val headline2: TextStyle,
    val headline3: TextStyle,
    val title1: TextStyle,
    val title2: TextStyle,
    val title3: TextStyle,
    val body1: TextStyle,
    val body2: TextStyle,
    val body3: TextStyle,
    val label1: TextStyle,
    val label2: TextStyle,
    val label3: TextStyle,
) {

    constructor(
        defaultFontFamily: FontFamily = FontFamily.Default,
        display1: TextStyle = DefaultTextStyle.copy(
            fontFamily = defaultFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 57.sp,
            lineHeight = 64.0.sp,
            letterSpacing = -0.2.sp
        ),
        display2: TextStyle = DefaultTextStyle.copy(
            fontFamily = defaultFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 45.sp,
            lineHeight = 52.0.sp,
            letterSpacing = 0.0.sp
        ),
        display3: TextStyle = DefaultTextStyle.copy(
            fontFamily = defaultFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 36.sp,
            lineHeight = 44.0.sp,
            letterSpacing = 0.0.sp
        ),
        headline1: TextStyle = DefaultTextStyle.copy(
            fontFamily = defaultFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 32.sp,
            lineHeight = 40.0.sp,
            letterSpacing = 0.0.sp
        ),
        headline2: TextStyle = DefaultTextStyle.copy(
            fontFamily = defaultFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 28.sp,
            lineHeight = 36.0.sp,
            letterSpacing = 0.0.sp
        ),
        headline3: TextStyle = DefaultTextStyle.copy(
            fontFamily = defaultFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 24.sp,
            lineHeight = 32.0.sp,
            letterSpacing = 0.0.sp
        ),
        title1: TextStyle = DefaultTextStyle.copy(
            fontFamily = defaultFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 22.sp,
            lineHeight = 28.0.sp,
            letterSpacing = 0.0.sp
        ),
        title2: TextStyle = DefaultTextStyle.copy(
            fontFamily = defaultFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 24.0.sp,
            letterSpacing = 0.2.sp
        ),
        title3: TextStyle = DefaultTextStyle.copy(
            fontFamily = defaultFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.0.sp,
            letterSpacing = 0.1.sp
        ),
        body1: TextStyle = DefaultTextStyle.copy(
            fontFamily = defaultFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.0.sp,
            letterSpacing = 0.5.sp
        ),
        body2: TextStyle = DefaultTextStyle.copy(
            fontFamily = defaultFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.0.sp,
            letterSpacing = 0.2.sp
        ),
        body3: TextStyle = DefaultTextStyle.copy(
            fontFamily = defaultFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.0.sp,
            letterSpacing = 0.4.sp
        ),
        label1: TextStyle = DefaultTextStyle.copy(
            fontFamily = defaultFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.0.sp,
            letterSpacing = 0.1.sp
        ),
        label2: TextStyle = DefaultTextStyle.copy(
            fontFamily = defaultFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.0.sp,
            letterSpacing = 0.5.sp
        ),
        label3: TextStyle = DefaultTextStyle.copy(
            fontFamily = defaultFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.0.sp,
            letterSpacing = 0.5.sp
        )
    ) : this(
        display1 = display1,
        display2 = display2,
        display3 = display3,
        headline1 = headline1,
        headline2 = headline2,
        headline3 = headline3,
        title1 = title1,
        title2 = title2,
        title3 = title3,
        body1 = body1,
        body2 = body2,
        body3 = body3,
        label1 = label1,
        label2 = label2,
        label3 = label3
    )

    /** Returns a copy of this Typography, optionally overriding some of the values. */
    fun copy(
        display1: TextStyle = this.display1,
        display2: TextStyle = this.display2,
        display3: TextStyle = this.display3,
        headline1: TextStyle = this.headline1,
        headline2: TextStyle = this.headline2,
        headline3: TextStyle = this.headline3,
        title1: TextStyle = this.title1,
        title2: TextStyle = this.title2,
        title3: TextStyle = this.title3,
        body1: TextStyle = this.body1,
        body2: TextStyle = this.body2,
        body3: TextStyle = this.body3,
        label1: TextStyle = this.label1,
        label2: TextStyle = this.label2,
        label3: TextStyle = this.label3,
    ) = Typography(
        display1 = display1,
        display2 = display2,
        display3 = display3,
        headline1 = headline1,
        headline2 = headline2,
        headline3 = headline3,
        title1 = title1,
        title2 = title2,
        title3 = title3,
        body1 = body1,
        body2 = body2,
        body3 = body3,
        label1 = label1,
        label2 = label2,
        label3 = label3
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Typography) return false

        if (display1 != other.display1) return false
        if (display2 != other.display2) return false
        if (display3 != other.display3) return false
        if (headline1 != other.headline1) return false
        if (headline2 != other.headline2) return false
        if (headline3 != other.headline3) return false
        if (title1 != other.title1) return false
        if (title2 != other.title2) return false
        if (title3 != other.title3) return false
        if (body1 != other.body1) return false
        if (body2 != other.body2) return false
        if (body3 != other.body3) return false
        if (label1 != other.label1) return false
        if (label2 != other.label2) return false
        if (label3 != other.label3) return false
        return true
    }

    override fun hashCode(): Int {
        var result = display1.hashCode()
        result = 31 * result + display2.hashCode()
        result = 31 * result + display3.hashCode()
        result = 31 * result + headline1.hashCode()
        result = 31 * result + headline2.hashCode()
        result = 31 * result + headline3.hashCode()
        result = 31 * result + title1.hashCode()
        result = 31 * result + title2.hashCode()
        result = 31 * result + title3.hashCode()
        result = 31 * result + body1.hashCode()
        result = 31 * result + body2.hashCode()
        result = 31 * result + body3.hashCode()
        result = 31 * result + label1.hashCode()
        result = 31 * result + label2.hashCode()
        result = 31 * result + label3.hashCode()
        return result
    }

    override fun toString(): String {
        return "Typography(display1=$display1, display2=$display2, display3=$display3, " + "headline1=$headline1, headline2=$headline2, headline3=$headline3, " + "title1=$title1, title2=$title2, title3=$title3, " + "body1=$body1, body2=$body2, body3=$body3, " + "label1=$label1, label2=$label2, label3=$label3)"
    }
}

/**
 * This CompositionLocal holds on to the current definition of typography for this application as
 * described by the Material spec. You can read the values in it when creating custom components
 * that want to use Material types, as well as override the values when you want to re-style a part
 * of your hierarchy. Material components related to text such as [Button] will use this
 * CompositionLocal to set values with which to style children text components.
 *
 * To access values within this CompositionLocal, use [MaterialTheme.typography].
 */
internal val LocalTypography = staticCompositionLocalOf { Typography() }
