package com.gorman.ourmemoryapp.ui.admin.burials.ui

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.admin.burials.models.AdminBurialItemUi
import com.gorman.ourmemoryapp.ui.admin.burials.models.AdminBurialsUiState
import com.gorman.ourmemoryapp.ui.admin.burials.viewmodels.AdminBurialsViewModel
import com.gorman.ourmemoryapp.ui.admin.common.ui.AdminAddButton
import com.gorman.ourmemoryapp.ui.admin.common.ui.GuideHintCard
import com.gorman.ourmemoryapp.ui.common.ui.ErrorContent
import com.gorman.ourmemoryapp.ui.common.ui.LoadingContent
import com.gorman.ourmemoryapp.ui.common.ui.PlotNumberText
import com.gorman.ourmemoryapp.ui.common.ui.TopBarScaffold
import com.gorman.ourmemoryapp.ui.common.ui.TopBarSpacer
import com.gorman.ourmemoryapp.ui.common.ui.bottomBarContentPadding

@Composable
fun AdminBurialsScreen(
    onBackClick: () -> Unit,
    onBurialClick: (String) -> Unit,
    onNewBurialClick: () -> Unit,
    onGuideClick: () -> Unit,
    adminBurialsViewModel: AdminBurialsViewModel = hiltViewModel()
) {
    val state by adminBurialsViewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { adminBurialsViewModel.onScreenResumed() }

    TopBarScaffold(title = stringResource(R.string.burial_places), onBackClick = onBackClick) {
        when (val current = state) {
            AdminBurialsUiState.Loading -> LoadingContent()
            AdminBurialsUiState.Error -> ErrorContent()
            is AdminBurialsUiState.Success -> LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp + bottomBarContentPadding()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item { TopBarSpacer() }
                item {
                    GuideHintCard(
                    title = stringResource(R.string.how_to_add_a_burial_place),
                    onClick = onGuideClick
                )
                }
                items(current.items, key = { it.burial.id }) { item ->
                    AdminBurialRow(item = item, onClick = { onBurialClick(item.burial.id) })
                }
            }
        }
        AdminAddButton(
            text = stringResource(R.string.add_burial_place),
            onClick = onNewBurialClick,
            modifier = Modifier.align(Alignment.BottomEnd)
        )
    }
}

@Composable
private fun AdminBurialRow(item: AdminBurialItemUi, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(ROW_CORNER_RADIUS),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(12.dp)) {
            Text(
                text = stringResource(item.type.nameRes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            PlotNumberText(burial = item.burial)
            if (item.veteranNames.isNotBlank()) {
                Text(
                    text = item.veteranNames,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private val ROW_CORNER_RADIUS = 20.dp
