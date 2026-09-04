package com.zs.compose.foundation.backdrop

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntSize

/**
 * A [Backdrop] backed by a single [Painter], treated as if it covers the
 * entire physical display (not just this app's window). Children crop
 * against absolute screen coordinates, so this stays correct regardless
 * of whether the app is fullscreen, floating, or split-screen.
 *
 * Conceptually: imagine one giant image the size of the physical screen,
 * pinned at the screen's origin (0,0). Every child, wherever it is on
 * screen, draws exactly the slice of that image that sits behind it —
 * like cutting windows into a single backdrop rather than each child
 * having its own independent copy (contrast with a component-local
 * backdrop, where each child would render the painter fresh into its own
 * bounds with no shared coordinate space).
 *
 * KNOWN LIMITATION: on some OEM skins (confirmed on HyperOS / Poco F6,
 * Android 16) floating/freeform windows can render app content at an
 * internal size larger than the window's visible on-screen footprint,
 * then compositor-scale the whole surface down to fit the floating frame.
 * When that happens, `coordinates.positionOnScreen()` reports coordinates
 * in the inflated internal space, while `screenSize` (below) reports the
 * true physical display size — the two disagree, and the backdrop will
 * under-fill one edge of the component during/after a floating-window
 * resize. This class does NOT currently correct for that; a fix would
 * need a measured content-vs-visible scale factor (see conversation
 * history / TODO if revisiting this).
 */
class ScreenBackdrop : Backdrop {

   internal var painter: Painter? = null
   internal var scale: ContentScale = ContentScale.Crop

    /**
     * Real physical display size in px. DrawScope has no Context, so this
     * can't be resolved lazily here — it's pushed in externally and kept
     * current on resize/rotation. See [rememberScreenBackdrop].
     *
     * Backed by mutableStateOf so writes from the layout listener in
     * rememberScreenBackdrop correctly invalidate/redraw every child
     * currently reading it inside drawRegion (Compose's snapshot system
     * tracks state reads made during the draw phase too, not just
     * composition).
     */
    var screenSize: IntSize by mutableStateOf(IntSize.Zero)

    override fun DrawScope.drawRegion(coordinates: LayoutCoordinates?) {
        // Normal for the first frame or two, not an error — layout/size
        // haven't been reported yet. Also guards the window between app
        // start and the first layout-listener callback populating screenSize.
        if (coordinates == null || screenSize == IntSize.Zero) return

        val fullSize = Size(screenSize.width.toFloat(), screenSize.height.toFloat())

        val painter = painter ?: return
        val intrinsicSize = painter.intrinsicSize
        // e.g. Coil hasn't resolved a real image yet — nothing sensible to draw.
        // (A Coil AsyncImagePainter reports Unspecified/Zero here until its
        // request completes; this makes drawRegion a safe no-op until then,
        // and Compose's snapshot system will trigger a redraw automatically
        // once the painter's internal state updates — see class notes on
        // remembering the painter upstream for this to actually fire.)
        if (intrinsicSize.isUnspecified || intrinsicSize == Size.Zero) return

        // How the painter's native content maps onto the full display given `scale`.
        // ContentScale.Crop is the right default for wallpaper-style content:
        // it fills the entire screenSize with no gaps, at the cost of cropping
        // overflow on the longer axis — Fit would leave letterbox bars, and
        // FillBounds would distort the aspect ratio. Crop also keeps multiple
        // children's crops geometrically consistent with each other, since
        // it's the same uniform scale for every child regardless of where
        // that child sits.
        val scaleFactor = scale.computeScaleFactor(srcSize = intrinsicSize, dstSize = fullSize)
        val scaledSize = Size(
            intrinsicSize.width * scaleFactor.scaleX,
            intrinsicSize.height * scaleFactor.scaleY,
        )
        // Centering offset ContentScale.Crop applies within the full display.
        // This is where the *scaled* image's top-left corner sits relative to
        // screen-origin (0,0) — can be negative (image is larger than
        // screenSize.width on one axis and gets centered/overhung on both
        // sides equally).
        val scaleOffset = Offset(
            (fullSize.width - scaledSize.width) / 2f,
            (fullSize.height - scaledSize.height) / 2f,
        )

        // Absolute screen-space position — already includes the window's own
        // offset on screen (floating/split-screen), so no separate window-offset
        // term is needed here; adding one would double-count it.
        // IMPORTANT: this must stay positionOnScreen(), not positionInRoot()/
        // localPositionOf — those are relative to this app's own root/window,
        // which is the wrong frame once the backdrop is meant to represent the
        // whole physical screen rather than just this app's content area.
        val childOffset = coordinates.positionOnScreen()

        // Net offset: how far the child's screen position sits from the
        // scaled image's own top-left corner. This becomes the source-region
        // origin we need to draw starting from, so the correct slice of the
        // backdrop lines up under the child.
        val cropOffset = childOffset - scaleOffset

        // Clip to the child's own bounds first — without this, drawing the
        // (much larger) full scaledSize backdrop would paint outside the
        // child and bleed into whatever's drawn after it.
        clipRect(0f, 0f, coordinates.size.width.toFloat(), coordinates.size.height.toFloat()) {
            // Shift the coordinate space backward by cropOffset so that the
            // correct source pixels (the ones behind this child) land at
            // local (0,0) — i.e. inside the clip rect we just set up.
            translate(-cropOffset.x, -cropOffset.y) {
                with(painter) { draw(size = scaledSize) }
            }
        }
    }
}

// ---------- Screen size resolution (API 23+) ----------

/**
 * Resolves the true physical display size, independent of the app's
 * current window size — i.e. correct even when the app is floating,
 * split-screen, or otherwise occupying less than the full screen.
 *
 * minSdk is 23, but WindowMetrics (used below) is API 30+, hence the
 * version branch.
 */
private fun getScreenSize(context: Context): IntSize {
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        // maximumWindowMetrics, not currentWindowMetrics — the "current" variant
        // reports YOUR APP'S window bounds (shrinks in floating/split-screen —
        // confirmed by logging during development: currentWindowMetrics.bounds
        // tracked the floating window's shrinking size exactly, which is the
        // opposite of what we want here), while "maximum" reports the largest
        // bounds a window could take on this display, i.e. full display size,
        // independent of current window state.
        val bounds = windowManager.maximumWindowMetrics.bounds
        IntSize(bounds.width(), bounds.height())
    } else {
        // Pre-30: no WindowMetrics API at all. getRealMetrics reports full
        // physical display size (includes system bars), the closest
        // pre-30 equivalent of maximumWindowMetrics.bounds for this purpose.
        // (Plain getMetrics()/Resources.displayMetrics would exclude system
        // decorations on some API levels — deliberately not used here.)
        @Suppress("DEPRECATION")
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getRealMetrics(metrics)
        IntSize(metrics.widthPixels, metrics.heightPixels)
    }
}

// ---------- Remember + live update ----------

/**
 * Remembers a [ScreenBackdrop] and keeps its [ScreenBackdrop.screenSize]
 * current as the window resizes or moves (floating/split-screen) or the
 * device rotates. Uses a layout listener rather than relying solely on
 * Configuration changes, since some OEMs resize free-form windows without
 * firing onConfigurationChanged — a Configuration-only approach was tried
 * first and missed some resize events for exactly this reason.
 */
@Composable
fun rememberScreenBackdrop(
    painter: Painter?= null,
    scale: ContentScale = ContentScale.Crop,
): ScreenBackdrop {
    val view = LocalView.current
    // Keyed on painter/scale so swapping either produces a fresh backdrop
    // instance rather than mutating one meant for a different painter.
    val backdrop = remember(::ScreenBackdrop)

    DisposableEffect(view) {
        fun refresh() {
            backdrop.screenSize = getScreenSize(view.context)
        }

        // Catches resize; harmless (cheap) to also refresh on pure move,
        // since getScreenSize reads display bounds, not window bounds —
        // a move alone can't actually change the result, but re-querying
        // costs little and keeps this listener simple (one code path
        // instead of separately tracking "did size vs position change").
        val listener = View.OnLayoutChangeListener { _, left, top, right, bottom,
                                                     oldLeft, oldTop, oldRight, oldBottom ->
            val sizeChanged = (right - left != oldRight - oldLeft) ||
                    (bottom - top != oldBottom - oldTop)
            val movedChanged = (left != oldLeft) || (top != oldTop)
            if (sizeChanged || movedChanged) refresh()
        }

        view.addOnLayoutChangeListener(listener)
        refresh() // populate initial value immediately, don't wait for the
        // first layout-change event (there may not be one before
        // the first draw if the window never moves/resizes)

        onDispose { view.removeOnLayoutChangeListener(listener) }
    }

    // Mutate the backdrop properties in-place. Because these properties are used inside
    // the draw phase of the consumer modifier (which receives key),
    // any changes here will trigger a redraw.
    //
    // This "late-binding" approach is specifically compatible with Coil's
    // CrossfadePainter and AsyncImagePainter: as the painter internally transitions
    // from a placeholder to the final bitmap, it invalidates itself. Because we
    // pass the painter instance directly, the backdrop correctly observes those
    // internal invalidations and re-renders the smooth transition.
    return backdrop.also { it.scale = scale; it.painter = painter }
}