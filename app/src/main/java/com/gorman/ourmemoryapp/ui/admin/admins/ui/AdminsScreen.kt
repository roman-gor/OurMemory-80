package com.gorman.ourmemoryapp.ui.admin.admins.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.admin.admins.models.AdminsUiIntent
import com.gorman.ourmemoryapp.ui.admin.admins.models.AdminsUiState
import com.gorman.ourmemoryapp.ui.admin.admins.viewmodels.AdminsViewModel
import com.gorman.ourmemoryapp.ui.admin.common.ui.AdminAddButton
import com.gorman.ourmemoryapp.ui.common.ui.ErrorContent
import com.gorman.ourmemoryapp.ui.common.ui.LoadingContent
import com.gorman.ourmemoryapp.ui.common.ui.TopBarScaffold
import com.gorman.ourmemoryapp.ui.common.ui.TopBarSpacer
import com.gorman.ourmemoryapp.ui.common.ui.bottomBarContentPadding

@Composable
fun AdminsScreen(
    onBackClick: () -> Unit,
    adminsViewModel: AdminsViewModel = hiltViewModel()
) {
    val state by adminsViewModel.uiState.collectAsStateWithLifecycle()
    var isAddDialogOpen by remember { mutableStateOf(false) }

    TopBarScaffold(title = stringResource(R.string.administrators), onBackClick = onBackClick) {
        when (val current = state) {
            AdminsUiState.Loading -> LoadingContent()
            AdminsUiState.Error -> ErrorContent()
            is AdminsUiState.Success -> Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = SCREEN_PADDING,
                        end = SCREEN_PADDING,
                        bottom = LIST_BOTTOM_PADDING + bottomBarContentPadding()
                    ),
                    verticalArrangement = Arrangement.spacedBy(ITEM_SPACING),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item { TopBarSpacer() }
                    item {
                        Text(
                            text = stringResource(R.string.add_uid_to_storage_rules_msg),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    items(current.items, key = { it.uid }) { item ->
                        AdminCard(
                            item = item,
                            onRemoveConfirm = { adminsViewModel.onUiIntent(AdminsUiIntent.OnRemoveConfirm(item.uid)) }
                        )
                    }
                }
                AdminAddButton(
                    text = stringResource(R.string.add_administrator),
                    onClick = { isAddDialogOpen = true },
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
                if (isAddDialogOpen) {
                    AddAdminDialog(
                        status = current.addStatus,
                        onAdd = { adminsViewModel.onUiIntent(AdminsUiIntent.OnAddClick(it)) },
                        onDismiss = {
                            isAddDialogOpen = false
                            adminsViewModel.onUiIntent(AdminsUiIntent.OnAddStatusShown)
                        }
                    )
                }
            }
        }
    }
}

private val SCREEN_PADDING = 16.dp
private val ITEM_SPACING = 10.dp
private val LIST_BOTTOM_PADDING = 96.dp
