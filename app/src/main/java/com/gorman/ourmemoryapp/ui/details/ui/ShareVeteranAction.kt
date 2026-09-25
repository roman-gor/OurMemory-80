package com.gorman.ourmemoryapp.ui.details.ui

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.common.models.VeteranLink

@Composable
fun rememberShareVeteranAction(veteranId: String, veteranName: String): () -> Unit {
    val context = LocalContext.current
    val chooserTitle = stringResource(R.string.share)
    val shareText = stringResource(R.string.remember_veteran_msg, veteranName, VeteranLink.forId(veteranId))
    return remember(context, chooserTitle, shareText) {
        {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = PLAIN_TEXT
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            context.startActivity(Intent.createChooser(intent, chooserTitle))
        }
    }
}

private const val PLAIN_TEXT = "text/plain"
