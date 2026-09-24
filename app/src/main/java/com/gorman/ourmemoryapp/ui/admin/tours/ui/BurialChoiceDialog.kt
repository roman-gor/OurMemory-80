package com.gorman.ourmemoryapp.ui.admin.tours.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.admin.burials.models.AdminBurialItemUi
import com.gorman.ourmemoryapp.ui.common.ui.PlotNumberText
import kotlinx.collections.immutable.ImmutableList

@Composable
fun BurialChoiceDialog(
    burials: ImmutableList<AdminBurialItemUi>,
    onChoose: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    val typeLabels = burials.associate { it.burial.id to stringResource(it.type.nameRes) }
    val filtered = burials.filter { item ->
        val searchable = listOf(
            item.veteranNames,
            typeLabels[item.burial.id].orEmpty(),
            item.burial.section,
            item.burial.row,
            item.burial.place
        )
        query.isBlank() || searchable.any { it.contains(query.trim(), ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.add_stop)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text(text = stringResource(R.string.search)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                LazyColumn(modifier = Modifier.heightIn(max = LIST_MAX_HEIGHT)) {
                    items(filtered, key = { it.burial.id }) { item ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onChoose(item.burial.id) }
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = item.veteranNames.ifBlank { typeLabels[item.burial.id].orEmpty() },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            PlotNumberText(burial = item.burial)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = stringResource(R.string.cancel)) }
        }
    )
}

private val LIST_MAX_HEIGHT = 360.dp
