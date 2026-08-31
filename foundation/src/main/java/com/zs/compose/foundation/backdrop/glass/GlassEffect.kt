package com.zs.compose.foundation.backdrop.glass

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import com.zs.compose.foundation.backdrop.Backdrop


private class GlassEffectElement(
    val backdrop: Backdrop,
    var blurRadiusPx: Float
) : ModifierNodeElement<GlassEffectNode>() {

    override fun create(): GlassEffectNode  = GlassEffectNode(backdrop, blurRadiusPx)
    override fun update(node: GlassEffectNode) {
        node.blurRadiusPx = blurRadiusPx
        node.backdrop = backdrop
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GlassEffectElement

        if (blurRadiusPx != other.blurRadiusPx) return false
        if (backdrop != other.backdrop) return false

        return true
    }

    override fun hashCode(): Int {
        var result = blurRadiusPx.hashCode()
        result = 31 * result + backdrop.hashCode()
        return result
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "backdrop"
        properties["backdrop"] = backdrop
        properties["blurRadiusPx"] = blurRadiusPx
    }
}


/**
 * Applies a blur to the backdrop content captured by the provided [bridge].
 *
 * This modifier uses [android.graphics.RenderEffect] to blur the content behind the current
 * composable. The content to be blurred must be explicitly linked via the [BackdropBridge].
 *
 * Note: This effect is only supported on Android 12 (API 31) and above.
 *
 * @param bridge The [BackdropBridge] that coordinates the backdrop capture.
 * @param radius The blur radius in [Dp].
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Stable
@ExperimentalFoundationApi
fun Modifier.glassEffect(
    backdrop: Backdrop,
    blurRadiusPx: Float
) = this then GlassEffectElement(backdrop, blurRadiusPx)