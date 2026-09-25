package com.gorman.ourmemoryapp.ui.admin.admins.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.admin.admins.models.AdminItemUi

@Composable
fun AdminCard(item: AdminItemUi, onRemoveConfirm: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    var isRemoveDialogOpen by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(CARD_CORNER_RADIUS),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(CONTENT_SPACING), modifier = Modifier.padding(CARD_PADDING)) {
            Text(
                text = item.email.ifBlank { item.uid },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (item.isSuperAdmin) {
                Text(
                    text = stringResource(R.string.super_administrator),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = item.uid,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { clipboard.setText(AnnotatedString(item.uid)) }) {
                    Text(text = stringResource(R.string.copy_uid))
                }
                if (item.canRemove) {
                    TextButton(onClick = { isRemoveDialogOpen = true }) {
                        Text(text = stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
    if (isRemoveDialogOpen) {
        AlertDialog(
            onDismissRequest = { isRemoveDialogOpen = false },
            text = { Text(text = stringResource(R.string.remove_administrator_msg)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        isRemoveDialogOpen = false
                        onRemoveConfirm()
                    }
                ) {
                    Text(text = stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { isRemoveDialogOpen = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }
}

private val CARD_CORNER_RADIUS = 20.dp
private val CARD_PADDING = 12.dp
private val CONTENT_SPACING = 4.dp
