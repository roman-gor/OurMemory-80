package com.gorman.ourmemoryapp.ui.map.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.common.models.CemeteryLocation
import com.gorman.ourmemoryapp.ui.common.ui.NumberImageProvider
import com.gorman.ourmemoryapp.ui.map.models.BurialMarkerUi
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.ClusterListener
import com.yandex.mapkit.map.ClusterTapListener
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.MapType
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import kotlinx.collections.immutable.ImmutableList

@Composable
fun BurialsMap(
    mapView: MapView,
    markers: ImmutableList<BurialMarkerUi>,
    focusedBurialId: String?,
    isSatellite: Boolean,
    onMarkerClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val map = mapView.mapWindow.map
    val currentOnMarkerClick by rememberUpdatedState(onMarkerClick)
    var savedCamera by rememberSaveable { mutableStateOf<DoubleArray?>(null) }

    val markerIcon = remember { ImageProvider.fromResource(context, R.drawable.ic_marker) }
    val markerTapListener = remember {
        MapObjectTapListener { mapObject, _ ->
            (mapObject.userData as? String)?.let(currentOnMarkerClick)
            true
        }
    }
    val clusterTapListener = remember {
        ClusterTapListener { cluster ->
            map.move(
                CameraPosition(
                    cluster.appearance.geometry,
                    map.cameraPosition.zoom + CLUSTER_ZOOM_STEP,
                    NO_AZIMUTH,
                    NO_TILT
                )
            )
            true
        }
    }
    val clusterListener = remember {
        ClusterListener { cluster ->
            cluster.appearance.setIcon(NumberImageProvider(context, cluster.size))
            cluster.addClusterTapListener(clusterTapListener)
        }
    }
    val collection = remember(map) { map.mapObjects.addClusterizedPlacemarkCollection(clusterListener) }

    LaunchedEffect(isSatellite) {
        map.mapType = if (isSatellite) MapType.HYBRID else MapType.MAP
    }

    LaunchedEffect(markers) {
        collection.clear()
        markers.forEach { marker ->
            collection.addPlacemark().apply {
                geometry = Point(marker.latitude, marker.longitude)
                setIcon(markerIcon)
                userData = marker.id
                addTapListener(markerTapListener)
            }
        }
        collection.clusterPlacemarks(CLUSTER_RADIUS, CLUSTER_MIN_ZOOM)
    }

    DisposableEffect(map) {
        map.move(savedCamera?.toCameraPosition() ?: initialCamera(markers, focusedBurialId))
        onDispose {
            val position = map.cameraPosition
            savedCamera = doubleArrayOf(
                position.target.latitude,
                position.target.longitude,
                position.zoom.toDouble()
            )
        }
    }

    AndroidView(factory = { mapView }, modifier = modifier)
}

private fun initialCamera(markers: List<BurialMarkerUi>, focusedBurialId: String?): CameraPosition {
    val focused = markers.firstOrNull { it.id == focusedBurialId }
    return if (focused != null) {
        CameraPosition(Point(focused.latitude, focused.longitude), FOCUSED_ZOOM, NO_AZIMUTH, NO_TILT)
    } else {
        CameraPosition(
            Point(CemeteryLocation.LATITUDE, CemeteryLocation.LONGITUDE),
            CEMETERY_ZOOM,
            NO_AZIMUTH,
            NO_TILT
        )
    }
}

private fun DoubleArray.toCameraPosition() = CameraPosition(
    Point(this[LATITUDE_INDEX], this[LONGITUDE_INDEX]),
    this[ZOOM_INDEX].toFloat(),
    NO_AZIMUTH,
    NO_TILT
)

private const val CLUSTER_RADIUS = 60.0
private const val CLUSTER_MIN_ZOOM = 19
private const val CLUSTER_ZOOM_STEP = 2f
private const val CEMETERY_ZOOM = 16f
private const val FOCUSED_ZOOM = 19f
private const val NO_AZIMUTH = 0f
private const val NO_TILT = 0f
private const val LATITUDE_INDEX = 0
private const val LONGITUDE_INDEX = 1
private const val ZOOM_INDEX = 2
