package com.gorman.ourmemoryapp.ui.admin.burials.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
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
import com.gorman.ourmemoryapp.ui.common.ui.ErrorContent
import com.gorman.ourmemoryapp.ui.common.ui.LoadingContent
import com.gorman.ourmemoryapp.ui.common.ui.PlotNumberText
import com.gorman.ourmemoryapp.ui.common.ui.TopBarScaffold
import com.gorman.ourmemoryapp.ui.common.ui.TopBarSpacer

@Composable
fun AdminBurialsScreen(
    onBackClick: () -> Unit,
    onBurialClick: (String) -> Unit,
    onNewBurialClick: () -> Unit,
    adminBurialsViewModel: AdminBurialsViewModel = hiltViewModel()
) {
    val state by adminBurialsViewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { adminBurialsViewModel.onScreenResumed() }

    TopBarScaffold(title = stringResource(R.string.burial_places), onBackClick = onBackClick) {
        when (val current = state) {
            AdminBurialsUiState.Loading -> LoadingContent()
            AdminBurialsUiState.Error -> ErrorContent()
            is AdminBurialsUiState.Success -> LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item { TopBarSpacer() }
                items(current.items, key = { it.burial.id }) { item ->
                    AdminBurialRow(item = item, onClick = { onBurialClick(item.burial.id) })
                }
            }
        }
        FloatingActionButton(
            onClick = onNewBurialClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.add),
                contentDescription = stringResource(R.string.new_burial_place)
            )
        }
    }
}

@Composable
private fun AdminBurialRow(item: AdminBurialItemUi, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
