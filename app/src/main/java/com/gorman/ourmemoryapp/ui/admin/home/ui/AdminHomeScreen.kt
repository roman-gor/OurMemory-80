package com.gorman.ourmemoryapp.ui.admin.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.admin.home.models.AdminHomeUiEvent
import com.gorman.ourmemoryapp.ui.admin.home.viewmodels.AdminHomeViewModel
import com.gorman.ourmemoryapp.ui.common.ui.LinkRow
import com.gorman.ourmemoryapp.ui.common.ui.bottomBarContentPadding

@Composable
fun AdminHomeScreen(
    onModerationClick: () -> Unit,
    onVeteransClick: () -> Unit,
    onBurialsClick: () -> Unit,
    onToursClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    onSignedOut: () -> Unit,
    adminHomeViewModel: AdminHomeViewModel = hiltViewModel()
) {
    val state by adminHomeViewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 16.dp,
            end = 16.dp,
            bottom = 16.dp + bottomBarContentPadding()
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        item {
            Text(
                text = stringResource(R.string.administration),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        item {
            Text(
                text = stringResource(R.string.signed_in_as, state.email),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            LinkRow(
                title = stringResource(R.string.moderation),
                count = state.pendingSubmissionsCount,
                onClick = onModerationClick
            )
        }
        item {
            LinkRow(
                title = stringResource(R.string.feedback),
                count = state.newFeedbackCount,
                onClick = onFeedbackClick
            )
        }
        item {
            LinkRow(title = stringResource(R.string.veterans), onClick = onVeteransClick)
        }
        item {
            LinkRow(title = stringResource(R.string.burial_places), onClick = onBurialsClick)
        }
        item {
            LinkRow(title = stringResource(R.string.tours), onClick = onToursClick)
        }
        item {
            OutlinedButton(
                onClick = {
                    adminHomeViewModel.onUiEvent(AdminHomeUiEvent.OnSignOutClick)
                    onSignedOut()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.sign_out))
            }
        }
    }
}
