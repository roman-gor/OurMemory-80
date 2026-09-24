package com.gorman.ourmemoryapp.ui.map.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.common.ui.PlotNumberText
import com.gorman.ourmemoryapp.ui.map.models.BurialDetailsUi
import com.gorman.ourmemoryapp.ui.map.models.VeteranShortUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BurialBottomSheet(
    details: BurialDetailsUi,
    onVeteranClick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(details.type.nameRes),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    PlotNumberText(burial = details.burial)
                }
            }
            if (details.photo.isNotBlank()) {
                item {
                    AsyncImage(
                        model = details.photo,
                        contentDescription = null,
                        placeholder = painterResource(R.drawable.image_info_placeholder),
                        error = painterResource(R.drawable.image_info_placeholder),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    )
                }
            }
            if (details.description.isNotBlank()) {
                item {
                    Text(
                        text = details.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            items(details.veterans, key = { it.id }) { veteran ->
                VeteranRow(veteran = veteran, onClick = { onVeteranClick(veteran.id) })
            }
        }
    }
}

@Composable
private fun VeteranRow(veteran: VeteranShortUi, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        AsyncImage(
            model = veteran.portrait,
            contentDescription = null,
            placeholder = painterResource(R.drawable.portrait_placeholder),
            error = painterResource(R.drawable.portrait_placeholder),
            fallback = painterResource(R.drawable.portrait_placeholder),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = veteran.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (veteran.years.isNotBlank()) {
                Text(
                    text = veteran.years,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            painter = painterResource(R.drawable.keyboard_arrow_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}
