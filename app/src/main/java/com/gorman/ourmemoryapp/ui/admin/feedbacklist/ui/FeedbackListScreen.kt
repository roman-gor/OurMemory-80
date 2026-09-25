package com.gorman.ourmemoryapp.ui.admin.feedbacklist.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.gorman.ourmemoryapp.ui.admin.feedbacklist.models.FeedbackListUiEvent
import com.gorman.ourmemoryapp.ui.admin.feedbacklist.models.FeedbackListUiState
import com.gorman.ourmemoryapp.ui.admin.feedbacklist.viewmodels.FeedbackListViewModel
import com.gorman.ourmemoryapp.ui.common.ui.ErrorContent
import com.gorman.ourmemoryapp.ui.common.ui.LoadingContent
import com.gorman.ourmemoryapp.ui.common.ui.TopBarScaffold
import com.gorman.ourmemoryapp.ui.common.ui.TopBarSpacer
import com.gorman.ourmemoryapp.ui.common.ui.bottomBarContentPadding

@Composable
fun FeedbackListScreen(
    onBackClick: () -> Unit,
    onVeteranClick: (String) -> Unit,
    feedbackListViewModel: FeedbackListViewModel = hiltViewModel()
) {
    val state by feedbackListViewModel.uiState.collectAsStateWithLifecycle()

    TopBarScaffold(title = stringResource(R.string.feedback), onBackClick = onBackClick) {
        when (val current = state) {
            FeedbackListUiState.Loading -> LoadingContent()
            FeedbackListUiState.Error -> ErrorContent()
            is FeedbackListUiState.Success -> LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp + bottomBarContentPadding()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item { TopBarSpacer() }
                if (current.items.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.nothing_here_yet),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
                items(current.items, key = { it.id }) { item ->
                    FeedbackCard(
                        item = item,
                        onVeteranClick = onVeteranClick,
                        onMarkReviewedClick = {
                            feedbackListViewModel.onUiEvent(FeedbackListUiEvent.OnMarkReviewedClick(item.id))
                        },
                        onReplyClick = { reply ->
                            feedbackListViewModel.onUiEvent(FeedbackListUiEvent.OnReplyClick(item.id, reply))
                        }
                    )
                }
            }
        }
    }
}
