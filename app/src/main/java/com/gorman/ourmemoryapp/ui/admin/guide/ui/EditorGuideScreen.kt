package com.gorman.ourmemoryapp.ui.admin.guide.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.admin.guide.models.EditorGuideUiEvent
import com.gorman.ourmemoryapp.ui.admin.guide.viewmodels.EditorGuideViewModel
import com.gorman.ourmemoryapp.ui.common.ui.TopBarScaffold
import com.gorman.ourmemoryapp.ui.common.ui.TopBarSpacer
import com.gorman.ourmemoryapp.ui.common.ui.bottomBarContentPadding

@Composable
fun EditorGuideScreen(
    onBackClick: () -> Unit,
    editorGuideViewModel: EditorGuideViewModel = hiltViewModel()
) {
    val state by editorGuideViewModel.uiState.collectAsStateWithLifecycle()

    TopBarScaffold(title = stringResource(R.string.editor_guide), onBackClick = onBackClick) {
        LazyColumn(
            contentPadding = PaddingValues(
                start = SCREEN_PADDING,
                end = SCREEN_PADDING,
                bottom = SCREEN_PADDING + bottomBarContentPadding()
            ),
            verticalArrangement = Arrangement.spacedBy(ITEM_SPACING),
            modifier = Modifier.fillMaxSize()
        ) {
            item { TopBarSpacer() }
            item {
                Text(
                    text = stringResource(R.string.add_burials_first_msg),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(state.sections, key = { it.name }) { section ->
                GuideSectionCard(
                    section = section,
                    isExpanded = section in state.expanded,
                    onClick = { editorGuideViewModel.onUiEvent(EditorGuideUiEvent.OnSectionClick(section)) }
                )
            }
        }
    }
}

private val SCREEN_PADDING = 16.dp
private val ITEM_SPACING = 12.dp
