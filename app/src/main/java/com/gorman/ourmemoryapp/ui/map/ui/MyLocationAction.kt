package com.gorman.ourmemoryapp.ui.map.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.gorman.ourmemoryapp.ui.common.ui.LOCATION_PERMISSIONS
import com.gorman.ourmemoryapp.ui.common.ui.hasLocationPermission
import com.gorman.ourmemoryapp.ui.theme.MemoryRed
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.location.Location
import com.yandex.mapkit.location.LocationListener
import com.yandex.mapkit.location.LocationStatus
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView

@Composable
fun rememberMyLocationAction(mapView: MapView, onPermissionDenied: () -> Unit): () -> Unit {
    val context = LocalContext.current
    val currentOnPermissionDenied by rememberUpdatedState(onPermissionDenied)
    val userLocationLayer = remember(mapView) {
        MapKitFactory.getInstance().createUserLocationLayer(mapView.mapWindow)
    }
    val locationManager = remember { MapKitFactory.getInstance().createLocationManager() }
    val locationListener = remember(mapView) {
        object : LocationListener {
            override fun onLocationUpdated(location: Location) {
                mapView.mapWindow.map.move(
                    CameraPosition(location.position, MY_LOCATION_ZOOM, NO_AZIMUTH, NO_TILT)
                )
            }

            override fun onLocationStatusUpdated(status: LocationStatus) = Unit
        }
    }
    val showMyLocation = {
        userLocationLayer.isVisible = true
        locationManager.requestSingleUpdate(locationListener)
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) showMyLocation() else currentOnPermissionDenied()
    }

    val userLocationObjectListener = remember {
        object : UserLocationObjectListener {
            override fun onObjectAdded(view: UserLocationView) {
                val icon = UserLocationImageProvider(context)
                view.pin.setIcon(icon)
                view.arrow.setIcon(icon)
                view.accuracyCircle.fillColor = MemoryRed.copy(alpha = ACCURACY_FILL_ALPHA).toArgb()
                view.accuracyCircle.strokeColor = MemoryRed.copy(alpha = ACCURACY_STROKE_ALPHA).toArgb()
                view.accuracyCircle.strokeWidth = ACCURACY_STROKE_WIDTH
            }

            override fun onObjectRemoved(view: UserLocationView) = Unit

            override fun onObjectUpdated(view: UserLocationView, event: ObjectEvent) = Unit
        }
    }

    LaunchedEffect(userLocationLayer) {
        userLocationLayer.setObjectListener(userLocationObjectListener)
        if (context.hasLocationPermission()) userLocationLayer.isVisible = true
    }

    return {
        if (context.hasLocationPermission()) {
            showMyLocation()
        } else {
            permissionLauncher.launch(LOCATION_PERMISSIONS)
        }
    }
}

private const val MY_LOCATION_ZOOM = 18f
private const val ACCURACY_FILL_ALPHA = 0.15f
private const val ACCURACY_STROKE_ALPHA = 0.4f
private const val ACCURACY_STROKE_WIDTH = 1f
private const val NO_AZIMUTH = 0f
private const val NO_TILT = 0f
