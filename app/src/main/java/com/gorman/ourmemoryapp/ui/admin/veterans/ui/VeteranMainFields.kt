package com.gorman.ourmemoryapp.ui.admin.veterans.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.admin.veterans.models.VeteranCategory
import com.gorman.ourmemoryapp.ui.admin.veterans.models.VeteranEditorUiIntent
import com.gorman.ourmemoryapp.ui.admin.veterans.models.VeteranForm

@Composable
fun VeteranMainFields(
    form: VeteranForm,
    isEnabled: Boolean,
    onUiIntent: (VeteranEditorUiIntent) -> Unit
) {
    val pickPortrait = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { onUiIntent(VeteranEditorUiIntent.OnPortraitPicked(it.toString())) }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            AsyncImage(
                model = form.portrait,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.portrait_placeholder),
                error = painterResource(R.drawable.portrait_placeholder),
                modifier = Modifier
                    .size(PORTRAIT_SIZE)
                    .clip(CircleShape)
            )
            OutlinedButton(
                onClick = {
                    pickPortrait.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                enabled = isEnabled
            ) {
                Text(text = stringResource(R.string.choose_portrait))
            }
        }
        OutlinedTextField(
            value = form.text.name,
            onValueChange = { onUiIntent(VeteranEditorUiIntent.OnNameChange(it)) },
            label = { Text(text = stringResource(R.string.full_name)) },
            placeholder = originalPlaceholder(form, form.original.name),
            isError = form.language == null && !form.isNameValid,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = form.years,
            onValueChange = { onUiIntent(VeteranEditorUiIntent.OnYearsChange(it)) },
            label = { Text(text = stringResource(R.string.years_of_life)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            VeteranCategory.entries.forEach { category ->
                FilterChip(
                    selected = form.category == category,
                    onClick = { onUiIntent(VeteranEditorUiIntent.OnCategoryChange(category)) },
                    label = { Text(text = stringResource(category.labelRes)) }
                )
            }
        }
        OutlinedTextField(
            value = form.text.baseInfo,
            onValueChange = { onUiIntent(VeteranEditorUiIntent.OnBaseInfoChange(it)) },
            label = { Text(text = stringResource(R.string.short_info)) },
            placeholder = originalPlaceholder(form, form.original.baseInfo),
            minLines = SHORT_TEXT_MIN_LINES,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = form.text.allInfo,
            onValueChange = { onUiIntent(VeteranEditorUiIntent.OnAllInfoChange(it)) },
            label = { Text(text = stringResource(R.string.main_text)) },
            placeholder = originalPlaceholder(form, form.original.allInfo),
            minLines = LONG_TEXT_MIN_LINES,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun originalPlaceholder(form: VeteranForm, original: String): (@Composable () -> Unit)? =
    if (form.language == null || original.isBlank()) {
        null
    } else {
        { Text(text = original, maxLines = PLACEHOLDER_MAX_LINES) }
    }

private val PORTRAIT_SIZE = 88.dp
private const val PLACEHOLDER_MAX_LINES = 3
private const val SHORT_TEXT_MIN_LINES = 2
private const val LONG_TEXT_MIN_LINES = 4
