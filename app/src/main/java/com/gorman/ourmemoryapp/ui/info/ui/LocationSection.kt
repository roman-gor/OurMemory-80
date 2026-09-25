package com.gorman.ourmemoryapp.ui.info.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.common.models.CemeteryLocation
import com.gorman.ourmemoryapp.ui.common.ui.MapPreview
import com.gorman.ourmemoryapp.ui.common.ui.SectionTitle

@Composable
fun LocationSection(onOpenMapClick: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(modifier = modifier.fillMaxWidth()) {
        SectionTitle(text = stringResource(R.string.locationHeader))
        Text(
            text = stringResource(R.string.address),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
        )
        MapPreview(
            latitude = CemeteryLocation.LATITUDE,
            longitude = CemeteryLocation.LONGITUDE,
            zoom = CEMETERY_ZOOM,
            interactive = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
        )
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = onOpenMapClick, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    painter = painterResource(R.drawable.map),
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(text = stringResource(R.string.cemetery_map))
            }
            OutlinedButton(onClick = { context.openRouteToCemetery() }, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    painter = painterResource(R.drawable.my_location),
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(text = stringResource(R.string.get_directions))
            }
        }
    }
}

private fun Context.openRouteToCemetery() {
    val uri = "$ROUTE_URL${CemeteryLocation.LATITUDE},${CemeteryLocation.LONGITUDE}".toUri()
    try {
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    } catch (_: ActivityNotFoundException) {
    }
}

private const val CEMETERY_ZOOM = 16f
private const val ROUTE_URL = "https://yandex.ru/maps/?rtext=~"
