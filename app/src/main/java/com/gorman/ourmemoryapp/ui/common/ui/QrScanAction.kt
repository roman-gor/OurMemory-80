package com.gorman.ourmemoryapp.ui.common.ui

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.common.models.VeteranLink
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun rememberQrScanAction(onVeteranScanned: (String) -> Unit): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentOnVeteranScanned by rememberUpdatedState(onVeteranScanned)

    return {
        scope.launch {
            val scanner = GmsBarcodeScanning.getClient(
                context,
                GmsBarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
            )
            try {
                val rawValue = scanner.startScan().await()?.rawValue
                if (rawValue != null) {
                    VeteranLink.parseVeteranId(rawValue)?.let(currentOnVeteranScanned)
                        ?: context.showToast(R.string.not_our_qr_code_msg)
                }
            } catch (error: MlKitException) {
                Log.e(LOG_TAG, "QR scan failed", error)
                context.showToast(R.string.failed_to_open_scanner_msg)
            }
        }
    }
}

private fun Context.showToast(@StringRes messageRes: Int) {
    Toast.makeText(this, messageRes, Toast.LENGTH_LONG).show()
}

private const val LOG_TAG = "QrScanAction"
