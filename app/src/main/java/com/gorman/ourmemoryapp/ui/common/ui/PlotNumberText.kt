package com.gorman.ourmemoryapp.ui.common.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.common.models.BurialUi

@Composable
fun PlotNumberText(burial: BurialUi, modifier: Modifier = Modifier) {
    if (burial.hasPlotNumber) {
        Text(
            text = stringResource(R.string.section_row_place_msg, burial.section, burial.row, burial.place),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = modifier
        )
    }
}
