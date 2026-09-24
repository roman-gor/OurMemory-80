package com.gorman.ourmemoryapp.ui.details.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.common.ui.MapPreview
import com.gorman.ourmemoryapp.ui.details.models.BurialUi

@Composable
fun BurialSection(burial: BurialUi, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionTitle(text = stringResource(R.string.burial_place))
        if (burial.hasPlotNumber) {
            Text(
                text = stringResource(R.string.section_row_place_msg, burial.section, burial.row, burial.place),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
            )
        }
        MapPreview(
            latitude = burial.latitude,
            longitude = burial.longitude,
            zoom = BURIAL_ZOOM,
            interactive = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
        )
    }
}

private const val BURIAL_ZOOM = 18f
