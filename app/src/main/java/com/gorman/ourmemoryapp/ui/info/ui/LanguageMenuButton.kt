package com.gorman.ourmemoryapp.ui.info.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.gorman.ourmemoryapp.ui.common.ui.PillButton
import com.gorman.ourmemoryapp.ui.more.models.AppLanguage

@Composable
fun LanguageMenuButton(
    currentLanguage: AppLanguage,
    onLanguageChange: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    Box {
        PillButton(text = stringResource(currentLanguage.labelRes), onClick = { isExpanded = true })
        DropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
            AppLanguage.entries.forEach { language ->
                DropdownMenuItem(
                    text = { Text(stringResource(language.labelRes)) },
                    onClick = {
                        isExpanded = false
                        if (language != currentLanguage) onLanguageChange(language.tag)
                    }
                )
            }
        }
    }
}
