package com.gorman.ourmemoryapp.ui.admin.veterans.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.admin.veterans.models.toIsoDateOrNull
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VeteranDateField(
    label: String,
    date: String,
    isValid: Boolean,
    onDateChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isPickerOpen by rememberSaveable { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.fillMaxWidth()) {
        OutlinedButton(onClick = { isPickerOpen = true }, modifier = Modifier.weight(1f)) {
            Text(
                text = "$label: ${date.ifBlank { stringResource(R.string.not_specified) }}",
                color = if (isValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
        if (date.isNotBlank()) {
            IconButton(onClick = { onDateChange("") }) {
                Icon(painter = painterResource(R.drawable.close), contentDescription = stringResource(R.string.clear))
            }
        }
    }

    if (isPickerOpen) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.toIsoDateOrNull()?.toEpochMillis(),
            yearRange = FIRST_YEAR..LocalDate.now().year
        )
        DatePickerDialog(
            onDismissRequest = { isPickerOpen = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { onDateChange(it.toIsoDate()) }
                        isPickerOpen = false
                    }
                ) {
                    Text(text = stringResource(R.string.done))
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

private fun LocalDate.toEpochMillis() = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toIsoDate() = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate().toString()

private const val FIRST_YEAR = 1850
