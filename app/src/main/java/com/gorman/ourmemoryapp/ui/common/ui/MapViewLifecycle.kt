package com.gorman.ourmemoryapp.ui.common.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.gorman.ourmemoryapp.ui.theme.LocalDarkTheme
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.mapview.MapView

@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember { MapView(context) }
    val isDarkTheme = LocalDarkTheme.current
    LaunchedEffect(mapView, isDarkTheme) {
        mapView.mapWindow.map.isNightModeEnabled = isDarkTheme
    }
    DisposableEffect(mapView, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.start()
                Lifecycle.Event.ON_STOP -> mapView.stop()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) mapView.stop()
        }
    }
    return mapView
}

private fun MapView.start() {
    onStart()
    MapKitFactory.getInstance().onStart()
}

private fun MapView.stop() {
    onStop()
    MapKitFactory.getInstance().onStop()
}
