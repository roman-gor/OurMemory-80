package com.gorman.ourmemoryapp.ui.admin.veterans.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.common.models.BurialUi
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BurialPicker(
    burials: ImmutableList<BurialUi>,
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val notSpecified = stringResource(R.string.not_specified)
    val selected = burials.firstOrNull { it.id == selectedId }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected?.let { burialLabel(it) } ?: selectedId.ifBlank { notSpecified },
            onValueChange = {},
            readOnly = true,
            label = { Text(text = stringResource(R.string.burial_place)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
            DropdownMenuItem(
                text = { Text(text = notSpecified) },
                onClick = {
                    onSelect("")
                    isExpanded = false
                }
            )
            burials.forEach { burial ->
                DropdownMenuItem(
                    text = { Text(text = burialLabel(burial)) },
                    onClick = {
                        onSelect(burial.id)
                        isExpanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun burialLabel(burial: BurialUi) = if (burial.hasPlotNumber) {
    stringResource(R.string.section_row_place_msg, burial.section, burial.row, burial.place)
} else {
    burial.id
}
