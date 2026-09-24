package com.gorman.ourmemoryapp.ui.info.ui

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.common.ui.SectionTitle

@Composable
fun ContactsSection(
    openingHours: String,
    phone: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(modifier = modifier.fillMaxWidth()) {
        SectionTitle(text = stringResource(R.string.opening_hours))
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (openingHours.isNotBlank()) {
                ContactRow(label = stringResource(R.string.opening_hours), value = openingHours)
            }
            if (phone.isNotBlank()) {
                ContactRow(
                    label = stringResource(R.string.phone),
                    value = phone,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            context.startActivity(Intent(Intent.ACTION_DIAL, "$TEL_SCHEME$phone".toUri()))
                        }
                )
            }
        }
    }
}

@Composable
private fun ContactRow(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private const val TEL_SCHEME = "tel:"
