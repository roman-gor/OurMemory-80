package com.gorman.ourmemoryapp.ui.details.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gorman.ourmemoryapp.R

@Composable
fun ContributeSection(
    onAddToHistoryClick: () -> Unit,
    onReportErrorClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        OutlinedButton(onClick = onAddToHistoryClick, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(R.string.add_to_history))
        }
        TextButton(onClick = onReportErrorClick, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(R.string.report_an_error))
        }
    }
}
