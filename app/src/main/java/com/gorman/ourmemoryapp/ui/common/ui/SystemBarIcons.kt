package com.gorman.ourmemoryapp.ui.common.ui

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.gorman.ourmemoryapp.ui.theme.LocalDarkTheme

@Composable
fun SystemBarIcons(darkIcons: Boolean) {
    val view = LocalView.current
    val isDarkTheme = LocalDarkTheme.current
    DisposableEffect(view, darkIcons, isDarkTheme) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        controller?.isAppearanceLightStatusBars = darkIcons && !isDarkTheme
        onDispose {
            controller?.isAppearanceLightStatusBars = !isDarkTheme
        }
    }
}
