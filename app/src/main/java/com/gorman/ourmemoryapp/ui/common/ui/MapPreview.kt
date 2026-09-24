package com.gorman.ourmemoryapp.ui.common.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.gorman.ourmemoryapp.R
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.runtime.image.ImageProvider

@Composable
fun MapPreview(
    latitude: Double,
    longitude: Double,
    zoom: Float,
    interactive: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mapView = rememberMapViewWithLifecycle()

    DisposableEffect(mapView, latitude, longitude, zoom, interactive) {
        val point = Point(latitude, longitude)
        val map = mapView.mapWindow.map
        map.isScrollGesturesEnabled = interactive
        map.isZoomGesturesEnabled = interactive
        map.isRotateGesturesEnabled = interactive
        map.isTiltGesturesEnabled = interactive
        map.move(CameraPosition(point, zoom, NO_AZIMUTH, NO_TILT))
        val placemark = map.mapObjects.addPlacemark().apply {
            geometry = point
            setIcon(ImageProvider.fromResource(context, R.drawable.ic_marker))
        }
        onDispose {
            map.mapObjects.remove(placemark)
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
    )
}

private const val NO_AZIMUTH = 0f
private const val NO_TILT = 0f
