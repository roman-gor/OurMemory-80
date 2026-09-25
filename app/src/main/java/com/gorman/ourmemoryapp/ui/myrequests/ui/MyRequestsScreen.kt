package com.gorman.ourmemoryapp.ui.myrequests.ui

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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.common.ui.ErrorContent
import com.gorman.ourmemoryapp.ui.common.ui.LoadingContent
import com.gorman.ourmemoryapp.ui.common.ui.TopBarScaffold
import com.gorman.ourmemoryapp.ui.common.ui.TopBarSpacer
import com.gorman.ourmemoryapp.ui.common.ui.bottomBarContentPadding
import com.gorman.ourmemoryapp.ui.myrequests.models.MyRequestsUiState
import com.gorman.ourmemoryapp.ui.myrequests.viewmodels.MyRequestsViewModel

@Composable
fun MyRequestsScreen(
    onBackClick: () -> Unit,
    myRequestsViewModel: MyRequestsViewModel = hiltViewModel()
) {
    val state by myRequestsViewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { myRequestsViewModel.onScreenResumed() }

    TopBarScaffold(title = stringResource(R.string.my_requests), onBackClick = onBackClick) {
        when (val current = state) {
            MyRequestsUiState.Loading -> LoadingContent()
            MyRequestsUiState.Error -> ErrorContent()
            is MyRequestsUiState.Success -> LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp + bottomBarContentPadding()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item { TopBarSpacer() }
                if (current.items.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.no_requests_yet_msg),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
                items(current.items, key = { "${it.kind}_${it.id}" }) { item -> MyRequestCard(item = item) }
            }
        }
    }
}
