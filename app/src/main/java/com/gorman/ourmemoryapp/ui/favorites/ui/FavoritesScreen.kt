package com.gorman.ourmemoryapp.ui.favorites.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.common.ui.EmptyContent
import com.gorman.ourmemoryapp.ui.common.ui.ErrorContent
import com.gorman.ourmemoryapp.ui.common.ui.LoadingContent
import com.gorman.ourmemoryapp.ui.common.ui.TopBarScaffold
import com.gorman.ourmemoryapp.ui.common.ui.TopBarSpacer
import com.gorman.ourmemoryapp.ui.common.ui.bottomBarContentPadding
import com.gorman.ourmemoryapp.ui.favorites.models.FavoriteVeteranUi
import com.gorman.ourmemoryapp.ui.favorites.models.FavoritesUiIntent
import com.gorman.ourmemoryapp.ui.favorites.models.FavoritesUiState
import com.gorman.ourmemoryapp.ui.favorites.viewmodels.FavoritesViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch

@Composable
fun FavoritesScreen(
    onBackClick: () -> Unit,
    onVeteranClick: (String) -> Unit,
    favoritesViewModel: FavoritesViewModel = hiltViewModel()
) {
    val state by favoritesViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    TopBarScaffold(title = stringResource(R.string.favorites), onBackClick = onBackClick) {
        when (val current = state) {
            FavoritesUiState.Loading -> LoadingContent()
            FavoritesUiState.Error -> ErrorContent()
            is FavoritesUiState.Success -> FavoritesList(
                items = current.items,
                snackbarHostState = snackbarHostState,
                onVeteranClick = onVeteranClick,
                onUiIntent = favoritesViewModel::onUiIntent
            )
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        )
    }
}

@Composable
private fun FavoritesList(
    items: ImmutableList<FavoriteVeteranUi>,
    snackbarHostState: SnackbarHostState,
    onVeteranClick: (String) -> Unit,
    onUiIntent: (FavoritesUiIntent) -> Unit
) {
    val scope = rememberCoroutineScope()
    val removedMessage = stringResource(R.string.removed_from_favorites)
    val undoLabel = stringResource(R.string.undo)
    val onRemove: (String) -> Unit = { veteranId ->
        onUiIntent(FavoritesUiIntent.OnRemove(veteranId))
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(
                message = removedMessage,
                actionLabel = undoLabel,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) onUiIntent(FavoritesUiIntent.OnUndoRemove(veteranId))
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp + bottomBarContentPadding()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item { TopBarSpacer() }
        if (items.isEmpty()) {
            item {
                EmptyContent(
                    iconRes = R.drawable.favorite_border,
                    message = stringResource(R.string.no_favorites_yet_msg)
                )
            }
        }
        items(items, key = { it.id }) { item ->
            SwipeToRemoveRow(
                onRemove = { onRemove(item.id) },
                modifier = Modifier.animateItem()
            ) {
                FavoriteRow(item = item, onClick = { onVeteranClick(item.id) })
            }
        }
    }
}

@Composable
private fun SwipeToRemoveRow(
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState()
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        onDismiss = { onRemove() },
        backgroundContent = {
            Box(
                contentAlignment = Alignment.CenterEnd,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CardDefaults.shape)
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 24.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.remove),
                    contentDescription = stringResource(R.string.remove_from_favorites),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        },
        modifier = modifier
    ) {
        content()
    }
}

@Composable
private fun FavoriteRow(item: FavoriteVeteranUi, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                    .size(PORTRAIT_SIZE)
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

private val PORTRAIT_SIZE = 48.dp
