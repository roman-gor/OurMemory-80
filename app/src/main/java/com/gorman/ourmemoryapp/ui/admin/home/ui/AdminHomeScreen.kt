package com.gorman.ourmemoryapp.ui.admin.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.admin.home.models.AdminHomeUiEvent
import com.gorman.ourmemoryapp.ui.admin.home.viewmodels.AdminHomeViewModel
import com.gorman.ourmemoryapp.ui.common.ui.GroupTitle
import com.gorman.ourmemoryapp.ui.common.ui.bottomBarContentPadding

@Composable
fun AdminHomeScreen(
    onModerationClick: () -> Unit,
    onVeteransClick: () -> Unit,
    onBurialsClick: () -> Unit,
    onToursClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    onGuideClick: () -> Unit,
    onSignedOut: () -> Unit,
    adminHomeViewModel: AdminHomeViewModel = hiltViewModel()
) {
    val state by adminHomeViewModel.uiState.collectAsStateWithLifecycle()
    val counts = state.contentCounts

    LazyColumn(
        contentPadding = PaddingValues(
            start = SCREEN_PADDING,
            top = SCREEN_PADDING,
            end = SCREEN_PADDING,
            bottom = SCREEN_PADDING + bottomBarContentPadding()
        ),
        verticalArrangement = Arrangement.spacedBy(ITEM_SPACING),
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        item {
            AdminHeader(
                email = state.email,
                onSignOutClick = {
                    adminHomeViewModel.onUiEvent(AdminHomeUiEvent.OnSignOutClick)
                    onSignedOut()
                }
            )
        }
        item { GroupTitle(text = stringResource(R.string.requires_attention)) }
        item {
            TilesRow {
                StatTile(
                    count = state.pendingSubmissionsCount,
                    title = stringResource(R.string.pending_submissions),
                    iconRes = R.drawable.add_photo,
                    onClick = onModerationClick,
                    modifier = it
                )
                StatTile(
                    count = state.newFeedbackCount,
                    title = stringResource(R.string.new_requests),
                    iconRes = R.drawable.mail,
                    onClick = onFeedbackClick,
                    modifier = it
                )
            }
        }
        item { GroupTitle(text = stringResource(R.string.content)) }
        item {
            TilesRow {
                ContentTile(
                    title = stringResource(R.string.veterans),
                    subtitle = pluralStringResource(R.plurals.veteran_cards_count, counts.veterans, counts.veterans),
                    iconRes = R.drawable.person,
                    onClick = onVeteransClick,
                    modifier = it
                )
                ContentTile(
                    title = stringResource(R.string.burial_places),
                    subtitle = pluralStringResource(R.plurals.burial_places_count, counts.burials, counts.burials),
                    iconRes = R.drawable.monument,
                    onClick = onBurialsClick,
                    modifier = it
                )
            }
        }
        item {
            TilesRow {
                ContentTile(
                    title = stringResource(R.string.tours),
                    subtitle = pluralStringResource(R.plurals.routes_count, counts.tours, counts.tours),
                    iconRes = R.drawable.directions_walk,
                    onClick = onToursClick,
                    modifier = it
                )
                ContentTile(
                    title = stringResource(R.string.editor_guide),
                    subtitle = stringResource(R.string.how_to_add_and_edit_content_msg),
                    iconRes = R.drawable.menu_book,
                    onClick = onGuideClick,
                    modifier = it
                )
            }
        }
    }
}

@Composable
private fun TilesRow(content: @Composable (Modifier) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(ITEM_SPACING),
        modifier = Modifier.height(IntrinsicSize.Max)
    ) {
        content(
            Modifier
                .weight(1f)
                .fillMaxHeight()
        )
    }
}

private val SCREEN_PADDING = 16.dp
private val ITEM_SPACING = 12.dp
