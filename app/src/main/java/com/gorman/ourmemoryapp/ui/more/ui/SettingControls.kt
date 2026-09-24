package com.gorman.ourmemoryapp.ui.more.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun <T> ChoiceSetting(
    title: String,
    options: List<T>,
    selected: T,
    labelRes: (T) -> Int,
    onSelect: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Text(text = title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = option == selected,
                    onClick = { onSelect(option) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    label = {
                        Text(
                        text = stringResource(labelRes(option)),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    }
                )
            }
        }
    }
}

@Composable
fun SwitchSetting(
    title: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = isChecked, onCheckedChange = onCheckedChange)
    }
}
