package com.prime.toolkit.preview

import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.prime.toolkit.R
import com.primex.core.pluralStringResource2
import com.primex.core.stringResource2
import com.primex.material3.Text

@Composable
fun TextView(text: CharSequence, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { TextView(it).apply { clipToOutline = true } },
        update = {it.text = text},
        modifier = modifier
    )
}


/*@Preview(widthDp = 360, showBackground = true)
@Composable
fun PreviewAndroidText() {
    val styled = LocalContext.current.getText(
        R.string.styled_from_android_source
    )
    TextView(text = styled)
}*/

@OptIn(ExperimentalTextApi::class)
@Preview(widthDp = 360, showBackground = true)
@Composable
fun PreviewComposeText(){
    val styled = pluralStringResource2(id = R.plurals.plural_styled_string_with_args, 5, 5)
    Text(text = styled)
}

