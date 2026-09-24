package com.gorman.ourmemoryapp.ui.map.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gorman.ourmemoryapp.R

@Composable
fun MapControls(
    isSatellite: Boolean,
    onMapTypeClick: () -> Unit,
    onMyLocationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SmallFloatingActionButton(
            onClick = onMapTypeClick,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                painter = painterResource(R.drawable.layers),
                contentDescription = stringResource(if (isSatellite) R.string.scheme else R.string.satellite)
            )
        }
        SmallFloatingActionButton(
            onClick = onMyLocationClick,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                painter = painterResource(R.drawable.my_location),
                contentDescription = stringResource(R.string.my_location)
            )
        }
    }
}
