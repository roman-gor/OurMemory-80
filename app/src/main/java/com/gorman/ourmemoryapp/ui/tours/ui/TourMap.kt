package com.gorman.ourmemoryapp.ui.tours.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.doOnLayout
import com.gorman.ourmemoryapp.ui.common.ui.NumberImageProvider
import com.gorman.ourmemoryapp.ui.common.ui.rememberMapViewWithLifecycle
import com.gorman.ourmemoryapp.ui.theme.MemoryRed
import com.gorman.ourmemoryapp.ui.tours.models.TourStopUi
import com.yandex.mapkit.Animation
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObjectTapListener
import kotlinx.collections.immutable.ImmutableList

@Composable
fun TourMap(
    stops: ImmutableList<TourStopUi>,
    selectedStopIndex: Int?,
    onStopClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mapView = rememberMapViewWithLifecycle()
    val map = mapView.mapWindow.map
    val currentOnStopClick by rememberUpdatedState(onStopClick)
    val collection = remember(map) { map.mapObjects.addCollection() }
    val stopTapListener = remember {
        MapObjectTapListener { mapObject, _ ->
            (mapObject.userData as? Int)?.let(currentOnStopClick)
            true
        }
    }

    LaunchedEffect(stops) {
        collection.clear()
        val points = stops.map { Point(it.burial.latitude, it.burial.longitude) }
        if (points.size > 1) {
            collection.addPolyline(Polyline(points)).apply {
                setStrokeColor(MemoryRed.toArgb())
                strokeWidth = ROUTE_STROKE_WIDTH
            }
        }
        points.forEachIndexed { index, point ->
            collection.addPlacemark().apply {
                geometry = point
                setIcon(NumberImageProvider(context, stops[index].number))
                userData = index
                addTapListener(stopTapListener)
            }
        }
        mapView.doOnLayout {
            val camera = when {
                points.isEmpty() -> null
                points.size == 1 -> CameraPosition(points.first(), STOP_ZOOM, NO_AZIMUTH, NO_TILT)
                else -> map.cameraPosition(Geometry.fromPolyline(Polyline(points))).let {
                    CameraPosition(it.target, it.zoom - FIT_ZOOM_MARGIN, NO_AZIMUTH, NO_TILT)
                }
            }
            camera?.let(map::move)
        }
    }

    LaunchedEffect(selectedStopIndex) {
        val stop = selectedStopIndex?.let(stops::getOrNull) ?: return@LaunchedEffect
        map.move(
            CameraPosition(Point(stop.burial.latitude, stop.burial.longitude), STOP_ZOOM, NO_AZIMUTH, NO_TILT),
            Animation(Animation.Type.SMOOTH, CAMERA_ANIMATION_SECONDS),
            null
        )
    }

    AndroidView(factory = { mapView }, modifier = modifier)
}

private const val ROUTE_STROKE_WIDTH = 4f
private const val STOP_ZOOM = 19f
private const val FIT_ZOOM_MARGIN = 0.5f
private const val CAMERA_ANIMATION_SECONDS = 0.4f
private const val NO_AZIMUTH = 0f
private const val NO_TILT = 0f
