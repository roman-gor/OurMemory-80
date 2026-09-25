package com.gorman.ourmemoryapp.ui.admin.common.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.domain.models.ContentLanguage
import com.gorman.ourmemoryapp.ui.admin.common.models.editableContentLanguages
import com.gorman.ourmemoryapp.ui.admin.common.models.labelRes

@Composable
fun ContentLanguageSelector(
    selected: ContentLanguage?,
    onSelect: (ContentLanguage?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(verticalArrangement = Arrangement.spacedBy(SELECTOR_SPACING), modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = stringResource(R.string.content_language),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            editableContentLanguages.forEachIndexed { index, language ->
                SegmentedButton(
                    selected = language == selected,
                    onClick = { onSelect(language) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = editableContentLanguages.size),
                    label = { Text(text = stringResource(language.labelRes), maxLines = 1, overflow = TextOverflow.Ellipsis) }
                )
            }
        }
        if (selected != null) {
            Text(
                text = stringResource(R.string.translation_is_optional_msg),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private val SELECTOR_SPACING = 8.dp
