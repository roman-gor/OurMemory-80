package com.gorman.ourmemoryapp.ui.admin.burials.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.gorman.ourmemoryapp.ui.common.ui.LOCATION_PERMISSIONS
import com.gorman.ourmemoryapp.ui.common.ui.hasLocationPermission
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.location.Location
import com.yandex.mapkit.location.LocationListener
import com.yandex.mapkit.location.LocationStatus

@Composable
fun rememberCurrentLocationRequest(
    onLocation: (Double, Double) -> Unit,
    onPermissionDenied: () -> Unit
): () -> Unit {
    val context = LocalContext.current
    val currentOnLocation by rememberUpdatedState(onLocation)
    val currentOnPermissionDenied by rememberUpdatedState(onPermissionDenied)
    val locationManager = remember { MapKitFactory.getInstance().createLocationManager() }
    val locationListener = remember {
        object : LocationListener {
            override fun onLocationUpdated(location: Location) {
                currentOnLocation(location.position.latitude, location.position.longitude)
            }

            override fun onLocationStatusUpdated(status: LocationStatus) = Unit
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) {
            locationManager.requestSingleUpdate(locationListener)
        } else {
            currentOnPermissionDenied()
        }
    }

    return {
        if (context.hasLocationPermission()) {
            locationManager.requestSingleUpdate(locationListener)
        } else {
            permissionLauncher.launch(LOCATION_PERMISSIONS)
        }
    }
}
