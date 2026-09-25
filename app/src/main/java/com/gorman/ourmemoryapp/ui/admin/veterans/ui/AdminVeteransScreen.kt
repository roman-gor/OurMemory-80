package com.gorman.ourmemoryapp.ui.admin.veterans.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.admin.common.ui.AdminAddButton
import com.gorman.ourmemoryapp.ui.admin.common.ui.AdminSearchField
import com.gorman.ourmemoryapp.ui.admin.common.ui.GuideHintCard
import com.gorman.ourmemoryapp.ui.admin.veterans.models.AdminVeteranItemUi
import com.gorman.ourmemoryapp.ui.admin.veterans.models.AdminVeteransUiEvent
import com.gorman.ourmemoryapp.ui.admin.veterans.models.AdminVeteransUiState
import com.gorman.ourmemoryapp.ui.admin.veterans.viewmodels.AdminVeteransViewModel
import com.gorman.ourmemoryapp.ui.common.ui.ErrorContent
import com.gorman.ourmemoryapp.ui.common.ui.LoadingContent
import com.gorman.ourmemoryapp.ui.common.ui.TopBarScaffold
import com.gorman.ourmemoryapp.ui.common.ui.TopBarSpacer
import com.gorman.ourmemoryapp.ui.common.ui.bottomBarContentPadding

@Composable
fun AdminVeteransScreen(
    onBackClick: () -> Unit,
    onVeteranClick: (String) -> Unit,
    onNewVeteranClick: () -> Unit,
    onGuideClick: () -> Unit,
    adminVeteransViewModel: AdminVeteransViewModel = hiltViewModel()
) {
    val state by adminVeteransViewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        adminVeteransViewModel.onUiEvent(AdminVeteransUiEvent.OnScreenResumed)
    }

    TopBarScaffold(title = stringResource(R.string.veterans), onBackClick = onBackClick) {
        when (val current = state) {
            AdminVeteransUiState.Loading -> LoadingContent()
            AdminVeteransUiState.Error -> ErrorContent()
            is AdminVeteransUiState.Success -> LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp + bottomBarContentPadding()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item { TopBarSpacer() }
                item { GuideHintCard(title = stringResource(R.string.how_to_add_a_veteran), onClick = onGuideClick) }
                item {
                    AdminSearchField(
                        value = current.search,
                        onValueChange = { adminVeteransViewModel.onUiEvent(AdminVeteransUiEvent.OnSearchChange(it)) },
                        placeholder = stringResource(R.string.search_by_name)
                    )
                }
                items(current.items, key = { it.id }) { item ->
                    AdminVeteranRow(item = item, onClick = { onVeteranClick(item.id) })
                }
            }
        }
        AdminAddButton(
            text = stringResource(R.string.add_veteran),
            onClick = onNewVeteranClick,
            modifier = Modifier.align(Alignment.BottomEnd)
        )
    }
}

@Composable
private fun AdminVeteranRow(item: AdminVeteranItemUi, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(ROW_CORNER_RADIUS),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(12.dp)
        ) {
            AsyncImage(
                model = item.portrait,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.portrait_placeholder),
                error = painterResource(R.drawable.portrait_placeholder),
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = item.years,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private val ROW_CORNER_RADIUS = 20.dp
