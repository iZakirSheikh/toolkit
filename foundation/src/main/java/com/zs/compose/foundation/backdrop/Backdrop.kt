package com.zs.compose.foundation.backdrop

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.LayoutCoordinates

@Stable
interface Backdrop {
    fun DrawScope.drawRegion(coordinates: LayoutCoordinates?)
}