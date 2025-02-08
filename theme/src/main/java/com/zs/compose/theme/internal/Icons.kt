/*
 * Copyright 2025 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 31-01-2025.
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

package com.zs.compose.theme.internal

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.DefaultFillType
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

private inline fun materialIcon(
    name: String,
    block: ImageVector.Builder.() -> ImageVector.Builder
): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = MaterialIconDimension.dp,
        defaultHeight = MaterialIconDimension.dp,
        viewportWidth = MaterialIconDimension,
        viewportHeight = MaterialIconDimension
    )
        .block()
        .build()

private inline fun materialIcon(
    name: String,
    autoMirror: Boolean = false,
    block: ImageVector.Builder.() -> ImageVector.Builder
): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = MaterialIconDimension.dp,
        defaultHeight = MaterialIconDimension.dp,
        viewportWidth = MaterialIconDimension,
        viewportHeight = MaterialIconDimension,
        autoMirror = autoMirror
    )
        .block()
        .build()

private inline fun ImageVector.Builder.materialPath(
    fillAlpha: Float = 1f,
    strokeAlpha: Float = 1f,
    pathFillType: PathFillType = DefaultFillType,
    pathBuilder: PathBuilder.() -> Unit
) =
    path(
        fill = SolidColor(Color.Black),
        fillAlpha = fillAlpha,
        stroke = null,
        strokeAlpha = strokeAlpha,
        strokeLineWidth = 1f,
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Bevel,
        strokeLineMiter = 1f,
        pathFillType = pathFillType,
        pathBuilder = pathBuilder
    )

// All Material icons (currently) are 24dp by 24dp, with a viewport size of 24 by 24.
private const val MaterialIconDimension = 24f

internal object Icons {

    private var _close: ImageVector? = null
    val Close: ImageVector
        get() {
            if (_close != null) {
                return _close!!
            }
            _close =
                materialIcon(name = "Filled.Close") {
                    materialPath {
                        moveTo(19.0f, 6.41f)
                        lineTo(17.59f, 5.0f)
                        lineTo(12.0f, 10.59f)
                        lineTo(6.41f, 5.0f)
                        lineTo(5.0f, 6.41f)
                        lineTo(10.59f, 12.0f)
                        lineTo(5.0f, 17.59f)
                        lineTo(6.41f, 19.0f)
                        lineTo(12.0f, 13.41f)
                        lineTo(17.59f, 19.0f)
                        lineTo(19.0f, 17.59f)
                        lineTo(13.41f, 12.0f)
                        close()
                    }
                }
            return _close!!
        }

    private var _arrowDropDown: ImageVector? = null
    internal val ArrowDropDown: ImageVector
        get() {
            if (_arrowDropDown != null) {
                return _arrowDropDown!!
            }
            _arrowDropDown =
                materialIcon(name = "Filled.ArrowDropDown") {
                    materialPath {
                        moveTo(7.41f, 8.59f)
                        lineTo(12.0f, 13.17f)
                        lineToRelative(4.59f, -4.58f)
                        lineTo(18.0f, 10.0f)
                        lineToRelative(-6.0f, 6.0f)
                        lineToRelative(-6.0f, -6.0f)
                        lineToRelative(1.41f, -1.41f)
                        close()
                    }
                }
            return _arrowDropDown!!
        }






}