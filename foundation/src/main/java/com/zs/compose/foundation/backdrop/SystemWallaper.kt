package com.zs.compose.foundation.backdrop

import android.Manifest
import android.annotation.SuppressLint
import android.app.WallpaperColors
import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

private const val TAG = "SystemWallaper"

/**
 * ┌───────────────────────────────────────────────────────────────────┐
 * │                       WallpaperPainter                              │
 * │   Draw-phase-invalidated painter for the live system wallpaper.    │
 * │   Owns its own CoroutineScope — no external scope needs passing.   │
 * └───────────────────────────────────────────────────────────────────┘
 *
 * SCOPE OWNERSHIP
 * ────────────────
 *   Implements CoroutineScope itself, backed by a SupervisorJob created
 *   fresh in onRemembered() and cancelled in onForgotten()/onAbandoned().
 *   SupervisorJob (not plain Job) so a failed load() doesn't cancel the
 *   whole scope — one bad decode shouldn't kill future reload attempts.
 *   This mirrors what rememberCoroutineScope() would have given from the
 *   composable call site, just owned internally instead.
 *
 * PERMISSION CONTRACT (not enforced at runtime — caller's responsibility)
 * ─────────────────────────────────────────────────────────────────────
 *   Caller MUST already hold READ_EXTERNAL_STORAGE (all API levels) or
 *   MANAGE_EXTERNAL_STORAGE (API 30+) before this is remembered.
 *
 * UPDATE TRIGGER
 * ───────────────
 *   ACTION_WALLPAPER_CHANGED broadcast → reload → crossfade redraw.
 */
@Stable
class WallpaperPainter(
    private val context: Context,
    private val targetBitmapDimension: Int,
    private val fadeDurationMs: Int = 400
) : Painter(), RememberObserver, CoroutineScope {

    // SCOPE OWNERSHIP
    // ────────────────
    // SupervisorJob (not plain Job): one failed load() must not cancel the
    // whole scope — a bad decode shouldn't prevent future reload attempts
    // on the next wallpaper-changed broadcast.
    //
    // AndroidUiDispatcher.Main (not Dispatchers.Main.immediate): bundles a
    // MonotonicFrameClock alongside the dispatcher. fadeProgress.animateTo()
    // below needs withFrameNanos() to sync animation steps to the display —
    // Dispatchers.Main.immediate alone does NOT provide a frame clock and
    // will throw IllegalStateException ("MonotonicFrameClock not available")
    // the moment an animation runs. This was a real crash, not theoretical —
    // keep this dispatcher choice if touching this class again.
    private var job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = AndroidUiDispatcher.Main + job

    // CROSSFADE STATE
    // ─────────────────
    // `disposed` = the OUTGOING bitmap, fading out underneath the new one.
    // Name is misleading (nothing is actually disposed/recycled here — see
    // note below) — kept as-is to match existing call sites; consider
    // renaming to `previousBitmap` if touching this again.
    // `bitmap`   = the INCOMING/current bitmap, fading in on top.
    // Both are draw-phase state (mutableStateOf, read only in onDraw()) —
    // updates trigger a redraw of consumers, not a full recomposition.
    private var disposed by mutableStateOf<ImageBitmap?>(null)
    private var bitmap by mutableStateOf<ImageBitmap?>(null)
    private val fadeProgress = Animatable(1f)

    // NOTE: currently unused — remove if nothing outside this class reads it,
    // or wire it up if it was meant to gate some external loading UI.
    val isLoaded: Boolean get() = bitmap != null

    private var receiver: BroadcastReceiver? = null
    private var loadJob: Job? = null

    override val intrinsicSize: Size
        get() = bitmap?.let { Size(it.width.toFloat(), it.height.toFloat()) }
            ?: Size.Unspecified

    // LIFECYCLE
    // ──────────
    // RememberObserver ties this painter's async work + broadcast receiver
    // to actual composition presence — started only while remembered,
    // torn down cleanly when forgotten/abandoned.
    override fun onRemembered() {
        // Fresh Job each time this enters composition — covers the case
        // where the same instance could theoretically be re-remembered
        // after a prior onForgotten() cancelled the old one. Reusing an
        // already-cancelled Job would make every launch{} fail immediately.
        job = SupervisorJob()
        load()
        receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) = load()
        }
        ContextCompat.registerReceiver(
            context, receiver, IntentFilter(Intent.ACTION_WALLPAPER_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onForgotten() {
        job.cancel() // cancels loadJob + any in-flight animation together —
        // no need to separately track/cancel loadJob here.
        receiver?.let { context.unregisterReceiver(it) }
        receiver = null
    }

    override fun onAbandoned() = onForgotten()

    // PERMISSION CONTRACT (NOT enforced at runtime — caller's responsibility)
    // ─────────────────────────────────────────────────────────────────────
    // @SuppressLint here is intentional, not a shortcut: decodeWallpaper()
    // internally requires READ_EXTERNAL_STORAGE / MANAGE_EXTERNAL_STORAGE
    // depending on API level. The public entry point that remembers this
    // painter is annotated with @RequiresPermission so lint/tooling can
    // catch missing permission at the CALL SITE — suppressing it here just
    // avoids a redundant/impossible-to-satisfy lint warning on this private
    // internal function, which itself has no way to declare or check the
    // permission independently of its caller.
    @SuppressLint("MissingPermission")
    private fun load() {
        loadJob?.cancel()
        loadJob = launch {
            val bmp = withContext(Dispatchers.IO) {
                runCatching { decodeWallpaper(context, targetBitmapDimension) }.getOrNull()
            }?.asImageBitmap() ?: return@launch

            // Shift current → outgoing, install new bitmap as current, then
            // animate opacity from 0 → 1. snapTo(0f) matters: without it, a
            // wallpaper change arriving mid-fade would jump from wherever
            // fadeProgress currently sits instead of restarting cleanly.
            disposed = bitmap
            bitmap = bmp
            fadeProgress.snapTo(0f)
            fadeProgress.animateTo(1f, animationSpec = tween(fadeDurationMs))

            // Fade complete — drop the reference to the old bitmap.
            // NOTE: this only clears the reference, it does NOT call
            disposed?.asAndroidBitmap()?.recycle()
            disposed = null
        }
    }

    override fun DrawScope.onDraw() {
        val dst = IntSize(size.width.toInt(), size.height.toInt())
        // Outgoing bitmap drawn first (underneath), fading out.
        var image = disposed
        if (image != null) drawImage(image, dstSize = dst, alpha = 1f - fadeProgress.value)
        // Incoming bitmap drawn on top, fading in. On the very first load
        // (no prior bitmap), fadeProgress starts at 1f so this just appears
        // at full opacity with no fade-from-nothing.
        image = bitmap
        if (image != null) drawImage(image, dstSize = dst, alpha = fadeProgress.value)
    }
}

@RequiresPermission(
    anyOf = [Manifest.permission.READ_EXTERNAL_STORAGE, "android.permission.MANAGE_EXTERNAL_STORAGE"]
)
@NonRestartableComposable
@Composable
fun rememberWallpaperPainter(targetBitmapDimension: Int = 512, fadeDurationMs: Int = 400): WallpaperPainter {
    val context = LocalContext.current
    return remember(context, targetBitmapDimension, fadeDurationMs) {
        WallpaperPainter(context, targetBitmapDimension, fadeDurationMs)
    }
}

/**
 * API-level dispatch point — routes to the subsampled-decode path on 24+,
 * or the full-decode-then-scale-down fallback on 23.
 */
@RequiresPermission(
    allOf = [Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.MANAGE_EXTERNAL_STORAGE]
)
private fun decodeWallpaper(context: Context, targetMaxDimension: Int): Bitmap? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        decodeWallpaperResized(context, targetMaxDimension)
    } else {
        decodeWallpaperLegacy(context, targetMaxDimension)
    }
}

/**
 * Decodes the wallpaper on API < 24 only, where no file-descriptor-based
 * decode path exists — WallpaperManager only exposes an already-fully-
 * decoded, full-resolution [Drawable] on these versions.
 *
 * Full-res decode is unavoidable here (strictly more memory-expensive
 * than the API 24+ path), so the full-res intermediate is scaled down
 * and recycled immediately rather than allowed to persist.
 *
 * Not safe to call directly for general use — always call through
 * [decodeWallpaper], which dispatches by API level and keeps this
 * version boundary consistent with the other decode paths.
 */
@Deprecated(
    message = "Internal API-level-specific helper — only valid below API 24. " +
            "Call decodeWallpaper() instead, which dispatches to the correct " +
            "path for the running OS version.",
)
@SuppressLint("MissingPermission")
@RequiresPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
@Suppress("DEPRECATION")
private fun decodeWallpaperLegacy(context: Context, targetMaxDimension: Int): Bitmap? {
    // .drawable is the only option pre-24 — always full resolution,
    // no way to request a subsampled decode at this API level.
    val drawable = WallpaperManager.getInstance(context).drawable ?: return null

    // Full-res bitmap, unavoidably allocated at full size before any
    // downscaling can happen.
    val fullRes = drawable.toBitmap()

    val longerEdge = maxOf(fullRes.width, fullRes.height)
    // Already small enough — nothing to scale, return as-is.
    if (longerEdge <= targetMaxDimension) return fullRes

    // Scale factor to bring the longer edge down to the target size,
    // preserving aspect ratio.
    val scale = targetMaxDimension.toFloat() / longerEdge
    val scaled = fullRes.scale((fullRes.width * scale).toInt(), (fullRes.height * scale).toInt())

    // Drop the full-res copy immediately — it's served its purpose and
    // would otherwise sit in memory until GC gets to it on its own schedule.
    if (scaled !== fullRes) fullRes.recycle()

    return scaled
}

/**
 * Decodes the current wallpaper on API 24+, subsampling at decode time
 * to avoid allocating a full-resolution bitmap.
 *
 * Requirements:
 * - API ≥ 24 (uses getWallpaperFile()).
 * - Permissions: READ_EXTERNAL_STORAGE (API 24–32) or
 *   MANAGE_EXTERNAL_STORAGE (API 33+ for the real wallpaper).
 *
 * Behavior:
 * - Caller must hold the correct permission for the running OS.
 * - Without it, the system may return the default wallpaper or throw [SecurityException].
 * - This function does not validate permissions; responsibility lies
 *   with the caller.
 *
 * Prefer using [decodeWallpaper] for API-level dispatch.
 */

@SuppressLint("NewApi")
@RequiresApi(Build.VERSION_CODES.N)
@RequiresPermission(
    allOf = [Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.MANAGE_EXTERNAL_STORAGE]
)
private fun decodeWallpaperResized(context: Context, targetMaxDimension: Int): Bitmap? {
    // No version ceiling here — unlike the earlier version of this
    // function, this now attempts the call on every API 24+ device.
    // Whether it returns the REAL wallpaper or the default one on 33+
    // depends entirely on whether MANAGE_EXTERNAL_STORAGE is held —
    // that check is intentionally NOT done here; caller owns it.
    val wallpaperManager = WallpaperManager.getInstance(context)

    // First file descriptor: consumed by the bounds-only read below, not
    // reusable afterward.
    val boundsPfd = wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_SYSTEM) ?: return null

    val inSampleSize = boundsPfd.use { descriptor ->
        // inJustDecodeBounds = true → reads width/height only, allocates
        // zero pixels — cheap way to inspect the image before committing
        // to a real decode.
        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFileDescriptor(descriptor.fileDescriptor, null, boundsOptions)

        // Compute how much to subsample based on the real dimensions just read.
        calculateInSampleSize(boundsOptions.outWidth, boundsOptions.outHeight, targetMaxDimension)
    }
    // boundsPfd closes here via .use{} — its underlying descriptor was
    // consumed by the read above and can't be rewound for a second pass.

    // Second file descriptor: a fresh one, since the first was single-use.
    val decodePfd = wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_SYSTEM) ?: return null

    return decodePfd.use { descriptor ->
        // inSampleSize applied here means the bitmap is subsampled AT
        // DECODE TIME — a full-resolution bitmap is never allocated,
        // not even momentarily, unlike decode-then-scale-down approaches.
        val decodeOptions = BitmapFactory.Options().apply { this.inSampleSize = inSampleSize }
        BitmapFactory.decodeFileDescriptor(descriptor.fileDescriptor, null, decodeOptions)
    }
}

/**
 * Computes a BitmapFactory `inSampleSize` for decode-time downsampling.
 *
 * Returns the largest power-of-two factor such that the image's longer
 * edge stays at or above [targetMax] after subsampling — i.e. shrinks
 * as much as possible without going below the target resolution.
 *
 * Power-of-two only: BitmapFactory only guarantees an exact, fast
 * decode-time subsample for powers of two; other values are rounded
 * down internally anyway, so returning anything else would be wasted
 * precision.
 *
 * @param rawWidth actual decoded image width, from a bounds-only pass
 * @param rawHeight actual decoded image height, from a bounds-only pass
 * @param targetMax desired minimum size (px) of the longer edge after subsampling
 * @return power-of-two sample size to pass as `BitmapFactory.Options.inSampleSize`
 */
private fun calculateInSampleSize(rawWidth: Int, rawHeight: Int, targetMax: Int): Int {
    var inSampleSize = 1
    // Track the longer edge — that's the one that determines when we've
    // downsampled enough, regardless of aspect ratio.
    var longerEdge = maxOf(rawWidth, rawHeight)

    // Keep doubling inSampleSize (halving longerEdge) as long as we'd
    // still stay at or above the target after the next halving — stops
    // one step before going below targetMax, not after.
    while (longerEdge / 2 >= targetMax) {
        inSampleSize *= 2
        longerEdge /= 2
    }
    // Power-of-two only: BitmapFactory only guarantees an exact, fast
    // decode-time subsample for powers of two — other values get rounded
    // down internally anyway, so computing anything else here is wasted.
    return inSampleSize
}

/**
 * Observes the system's Material-You-style wallpaper accent colors
 * (primary/secondary/tertiary + light/dark theme hints), updating live
 * whenever the wallpaper changes.
 *
 * No permission required — reads a system-cached WallpaperColors object
 * (the same one driving notification-shade theming since API 27), never
 * the wallpaper file itself.
 */
@RequiresApi(Build.VERSION_CODES.O_MR1)
@Composable
fun dynamicAccentColor(): State<WallpaperColors?> {
    // applicationContext, not the raw LocalContext.current
    val appContext =  LocalContext.current.applicationContext

    // getInstance() is NOT a cached singleton internally — it's a thin
    // wrapper over context.getSystemService(WALLPAPER_SERVICE), a
    // HashMap lookup + cast on every call. remember{} here is what
    // actually avoids repeating that lookup on every recomposition.
    val mgr = remember(appContext) { WallpaperManager.getInstance(appContext) }

    // Seeded with the CURRENT colors immediately, not null — the system
    // already has these cached and computed before this composable even
    // runs, so there's no reason to wait for the first change-event to
    // show something.
    val colors: MutableState<WallpaperColors?> = remember(mgr) {
        mutableStateOf(mgr.getWallpaperColors(WallpaperManager.FLAG_SYSTEM))
    }

    DisposableEffect(mgr) {
        val listener = WallpaperManager.OnColorsChangedListener { _, which ->
            // WHICH is a bitmask of which wallpaper layer changed
            // (system vs lock screen) — filters out lock-screen-only
            // changes this function doesn't care about.
            if (which and WallpaperManager.FLAG_SYSTEM != 0) {
                colors.value = mgr.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
            }
        }
        mgr.addOnColorsChangedListener(listener, Handler(Looper.getMainLooper()))
        onDispose { mgr.removeOnColorsChangedListener(listener) }
    }

    return colors
}