package com.zs.compose.foundation.util

import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect


/**
 * Clips the drawing content to the specified [Outline].
 *
 * @param outline The [Outline] to clip the drawing content to.
 * @param block The drawing operations to be executed within the clipped region.
 */
inline fun DrawScope.clipOutline(
    outline: Outline,
    clipOp: ClipOp = ClipOp.Intersect,
    block: DrawScope.() -> Unit
) {
    when (outline) {
        is Outline.Rectangle -> {
            clipRect(
                left = outline.rect.left,
                top = outline.rect.top,
                right = outline.rect.right,
                bottom = outline.rect.bottom,
                clipOp = clipOp,
                block = block
            )
        }
        is Outline.Rounded -> {
            val path = androidx.compose.ui.graphics.Path().apply { addRoundRect(outline.roundRect) }
            clipPath(path = path, clipOp = clipOp, block = block)
        }
        is Outline.Generic -> {
            clipPath(path = outline.path, clipOp = clipOp, block = block)
        }
    }
}