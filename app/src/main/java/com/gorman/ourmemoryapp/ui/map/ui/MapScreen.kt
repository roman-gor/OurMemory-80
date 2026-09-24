package com.gorman.ourmemoryapp.ui.map.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.common.ui.CategoryFilterChips
import com.gorman.ourmemoryapp.ui.common.ui.CircleIconButton
import com.gorman.ourmemoryapp.ui.common.ui.ErrorContent
import com.gorman.ourmemoryapp.ui.common.ui.LoadingContent
import com.gorman.ourmemoryapp.ui.common.ui.SystemBarIcons
import com.gorman.ourmemoryapp.ui.common.ui.rememberMapViewWithLifecycle
import com.gorman.ourmemoryapp.ui.map.models.MapUiIntent
import com.gorman.ourmemoryapp.ui.map.models.MapUiState
import com.gorman.ourmemoryapp.ui.map.viewmodels.MapViewModel
import kotlinx.coroutines.launch

@Composable
fun MapScreen(
    onBackClick: (() -> Unit)?,
    onVeteranClick: (String) -> Unit,
    mapViewModel: MapViewModel = hiltViewModel()
) {
    val uiState by mapViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var isSatellite by rememberSaveable { mutableStateOf(false) }

    SystemBarIcons(darkIcons = !isSatellite)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val state = uiState) {
            MapUiState.Loading -> LoadingContent(modifier = Modifier.statusBarsPadding())
            MapUiState.Error -> ErrorContent(modifier = Modifier.statusBarsPadding())
            is MapUiState.Success -> MapContent(
                state = state,
                isSatellite = isSatellite,
                onMapTypeClick = { isSatellite = !isSatellite },
                snackbarHostState = snackbarHostState,
                onUiIntent = mapViewModel::onUiIntent,
                onVeteranClick = onVeteranClick,
                onBackClick = onBackClick
            )
        }
        if (uiState !is MapUiState.Success && onBackClick != null) {
            BackButton(onClick = onBackClick, modifier = Modifier.statusBarsPadding())
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        )
    }
}

@Composable
private fun MapContent(
    state: MapUiState.Success,
    isSatellite: Boolean,
    onMapTypeClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
    onUiIntent: (MapUiIntent) -> Unit,
    onVeteranClick: (String) -> Unit,
    onBackClick: (() -> Unit)?
) {
    val mapView = rememberMapViewWithLifecycle()
    val scope = rememberCoroutineScope()
    val permissionDeniedMessage = stringResource(R.string.allow_location_access_msg)
    val onMyLocationClick = rememberMyLocationAction(
        mapView = mapView,
        onPermissionDenied = { scope.launch { snackbarHostState.showSnackbar(permissionDeniedMessage) } }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        BurialsMap(
            mapView = mapView,
            markers = state.markers,
            focusedBurialId = state.focusedBurialId,
            isSatellite = isSatellite,
            onMarkerClick = { onUiIntent(MapUiIntent.OnMarkerClick(it)) },
            modifier = Modifier.fillMaxSize()
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 8.dp)
        ) {
            onBackClick?.let { BackButton(onClick = it) }
            CategoryFilterChips(
                checkedWar = state.checkedWar,
                checkedArt = state.checkedArt,
                onCheckedWarChange = { onUiIntent(MapUiIntent.OnCheckedWarChange(it)) },
                onCheckedArtChange = { onUiIntent(MapUiIntent.OnCheckedArtChange(it)) },
                modifier = Modifier.weight(1f)
            )
        }
        MapControls(
            isSatellite = isSatellite,
            onMapTypeClick = onMapTypeClick,
            onMyLocationClick = onMyLocationClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(16.dp)
        )
    }

    state.selectedBurial?.let { details ->
        BurialBottomSheet(
            details = details,
            onVeteranClick = onVeteranClick,
            onDismiss = { onUiIntent(MapUiIntent.OnSheetDismiss) }
        )
    }
}

@Composable
private fun BackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    CircleIconButton(
        painter = painterResource(R.drawable.chevron_left),
        contentDescription = stringResource(R.string.back),
        onClick = onClick,
        modifier = modifier.padding(start = 12.dp)
    )
}
