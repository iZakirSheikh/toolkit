package com.prime.toolkit.preview/*
package com.prime.toolkit.preview

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.outlined.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.prime.toolkit.R
import com.primex.core.OrientRed
import com.primex.core.SkyBlue
import com.primex.core.snackbar.Data
import com.primex.core.snackbar.SnackbarDuration
import com.primex.core.textResource
import com.primex.core.withStyle
import com.primex.material2.DismissableSnackbar
import com.primex.material2.Snackbar
import java.util.Objects

@OptIn(ExperimentalTextApi::class)
@Composable
private fun testData() = object : Data {
    override val accent: Color
        get() = Color.OrientRed
    override val leading: Any?
        get() = Icons.Outlined.Build
    override val message: CharSequence = textResource(id = R.string.snackbar_msg_short)
    override val action: CharSequence?
        get() = "ACTION"
    override val duration: SnackbarDuration
        get() = SnackbarDuration.Indefinite
    override val withDismissAction: Boolean
        get() = true

    override fun action() {
        TODO("Not yet implemented")
    }

    override fun dismiss() {
        TODO("Not yet implemented")
    }
}
// indicator
@Preview(showBackground = true, widthDp = 360, heightDp = 720, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun SnackbarPreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        DismissableSnackbar(data = testData(), modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp))
    }
}*/
