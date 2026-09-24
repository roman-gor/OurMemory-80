package com.gorman.ourmemoryapp.ui.admin.tours.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.admin.common.ui.AdminAddButton
import com.gorman.ourmemoryapp.ui.admin.common.ui.GuideHintCard
import com.gorman.ourmemoryapp.ui.admin.tours.models.AdminTourItemUi
import com.gorman.ourmemoryapp.ui.admin.tours.models.AdminToursUiState
import com.gorman.ourmemoryapp.ui.admin.tours.viewmodels.AdminToursViewModel
import com.gorman.ourmemoryapp.ui.common.ui.ErrorContent
import com.gorman.ourmemoryapp.ui.common.ui.LoadingContent
import com.gorman.ourmemoryapp.ui.common.ui.TopBarScaffold
import com.gorman.ourmemoryapp.ui.common.ui.TopBarSpacer

@Composable
fun AdminToursScreen(
    onBackClick: () -> Unit,
    onTourClick: (String) -> Unit,
    onNewTourClick: () -> Unit,
    onGuideClick: () -> Unit,
    adminToursViewModel: AdminToursViewModel = hiltViewModel()
) {
    val state by adminToursViewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { adminToursViewModel.onScreenResumed() }

    TopBarScaffold(title = stringResource(R.string.tours), onBackClick = onBackClick) {
        when (val current = state) {
            AdminToursUiState.Loading -> LoadingContent()
            AdminToursUiState.Error -> ErrorContent()
            is AdminToursUiState.Success -> LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item { TopBarSpacer() }
                item { GuideHintCard(title = stringResource(R.string.how_to_build_a_tour), onClick = onGuideClick) }
                items(current.items, key = { it.id }) { item ->
                    AdminTourRow(item = item, onClick = { onTourClick(item.id) })
                }
            }
        }
        AdminAddButton(
            text = stringResource(R.string.add_tour),
            onClick = onNewTourClick,
            modifier = Modifier.align(Alignment.BottomEnd)
        )
    }
}

@Composable
private fun AdminTourRow(item: AdminTourItemUi, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(ROW_CORNER_RADIUS),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(12.dp)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = pluralStringResource(R.plurals.stops_count, item.stopCount, item.stopCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private val ROW_CORNER_RADIUS = 20.dp
