package com.gorman.ourmemoryapp.ui.admin.burials.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.common.models.CemeteryLocation
import com.gorman.ourmemoryapp.ui.common.ui.rememberMapViewWithLifecycle
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.runtime.image.ImageProvider
import com.yandex.mapkit.map.Map as YandexMap

@Composable
fun BurialLocationPicker(
    latitude: Double?,
    longitude: Double?,
    onPointPicked: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mapView = rememberMapViewWithLifecycle()
    val currentOnPointPicked by rememberUpdatedState(onPointPicked)
    var isPermissionDenied by remember { mutableStateOf(false) }
    val initialPoint = remember {
        Point(latitude ?: CemeteryLocation.LATITUDE, longitude ?: CemeteryLocation.LONGITUDE)
    }
    val inputListener = remember {
        object : InputListener {
            override fun onMapTap(map: YandexMap, point: Point) = currentOnPointPicked(point.latitude, point.longitude)

            override fun onMapLongTap(map: YandexMap, point: Point) = Unit
        }
    }
    val placemark = remember(mapView) {
        mapView.mapWindow.map.mapObjects.addPlacemark().apply {
            geometry = initialPoint
            setIcon(ImageProvider.fromResource(context, R.drawable.ic_marker))
        }
    }
    val requestLocation = rememberCurrentLocationRequest(
        onLocation = { lat, lon ->
            isPermissionDenied = false
            currentOnPointPicked(lat, lon)
            mapView.mapWindow.map.move(CameraPosition(Point(lat, lon), PICKER_ZOOM, NO_AZIMUTH, NO_TILT))
        },
        onPermissionDenied = { isPermissionDenied = true }
    )

    DisposableEffect(mapView) {
        val map = mapView.mapWindow.map
        map.addInputListener(inputListener)
        map.move(CameraPosition(initialPoint, PICKER_ZOOM, NO_AZIMUTH, NO_TILT))
        onDispose { map.removeInputListener(inputListener) }
    }

    LaunchedEffect(placemark, latitude, longitude) {
        if (latitude != null && longitude != null) placemark.geometry = Point(latitude, longitude)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier
                .fillMaxWidth()
                .height(MAP_HEIGHT)
                .clip(RoundedCornerShape(12.dp))
        )
        Text(
            text = stringResource(R.string.tap_map_to_move_marker_msg),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedButton(onClick = requestLocation, modifier = Modifier.fillMaxWidth()) {
            Icon(painter = painterResource(R.drawable.my_location), contentDescription = null)
            Text(text = stringResource(R.string.my_location))
        }
        if (isPermissionDenied) {
            Text(
                text = stringResource(R.string.allow_location_access_msg),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

private val MAP_HEIGHT = 260.dp
private const val PICKER_ZOOM = 18f
private const val NO_AZIMUTH = 0f
private const val NO_TILT = 0f
