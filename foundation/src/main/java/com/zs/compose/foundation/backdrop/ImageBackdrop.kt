package com.zs.compose.foundation.backdrop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates

/**
 * A [Backdrop] backed by a single [Painter], scaled to fill each consuming
 * child's own bounds independently.
 *
 * Unlike [ScreenBackdrop], there's no shared coordinate space across
 * children here — each child just gets the painter rendered fresh into
 * its own local size. Visually this behaves like an independent/repeated
 * background per component (e.g. the same texture or pattern drawn into
 * every card), NOT like a single shared surface with windows cut into it.
 * If multiple children need to look like they're viewing different slices
 * of one continuous image, use [ScreenBackdrop] instead.
 */
class ImageBackdrop() : Backdrop {

    internal var painter: Painter? = null
    internal var scale: ContentScale = ContentScale.Crop

    override fun DrawScope.drawRegion(coordinates: LayoutCoordinates?) {
        // Normal for the first frame or two, not an error — the child
        // hasn't reported its layout coordinates yet.
        if (coordinates == null) return

        // Local reference frame only — no screen/window coordinates
        // involved at all, since each child is self-contained.
        val componentSize =
            Size(coordinates.size.width.toFloat(), coordinates.size.height.toFloat())
        if (componentSize == Size.Zero) return

        val painter = painter ?: return
        val intrinsicSize = painter.intrinsicSize
        // e.g. a Coil AsyncImagePainter hasn't resolved a real image yet —
        // nothing sensible to draw. Compose's snapshot system will trigger
        // a redraw automatically once the painter's internal state updates,
        // as long as the painter was remembered upstream (e.g. via
        // rememberAsyncImagePainter) so it actually starts loading.
        if (intrinsicSize.isUnspecified || intrinsicSize == Size.Zero) return

        // How the painter's native content maps onto this component's own
        // size given `scale`. ContentScale.Crop fills the component with no
        // gaps, cropping overflow on the longer axis; Fit would letterbox,
        // FillBounds would distort the aspect ratio.
        val scaleFactor = scale.computeScaleFactor(srcSize = intrinsicSize, dstSize = componentSize)
        val scaledSize = Size(
            intrinsicSize.width * scaleFactor.scaleX,
            intrinsicSize.height * scaleFactor.scaleY,
        )
        // Centering offset for Crop/Fit — how far the scaled image sits
        // outside the component's own bounds before clipping. Can be
        // negative (image larger than the component on one axis, so it
        // overhangs symmetrically on both sides) or positive (image
        // smaller than the component, e.g. under Fit, leaving it centered
        // with empty space around it).
        val scaleOffset = Offset(
            (componentSize.width - scaledSize.width) / 2f,
            (componentSize.height - scaledSize.height) / 2f,
        )

        // Clip to the component's own bounds so any overhang from Crop's
        // upscaling doesn't bleed outside this child into whatever's drawn
        // after it.
        clipRect(0f, 0f, componentSize.width, componentSize.height) {
            // Positive translate here (unlike ScreenBackdrop's negative
            // translate) — there's no shared canvas position to cancel out,
            // this is just directly centering the scaled image within the
            // component's own local origin.
            translate(scaleOffset.x, scaleOffset.y) {
                with(painter) { draw(size = scaledSize) }
            }
        }
    }
}

/**
 * Remembers an [ImageBackdrop] for the given painter/scale. Unlike
 * [rememberScreenBackdrop], no layout listener or Context-derived state is
 * needed — this backdrop only ever depends on each child's own local size,
 * which is already reactive via LayoutCoordinates passed into drawRegion.
 */
@Composable
fun rememberImageBackdrop(
    painter: Painter? = null,
    scale: ContentScale = ContentScale.Crop
): ImageBackdrop = remember() {
    ImageBackdrop()
}.also {
    it.painter = painter
    it.scale = scale
}