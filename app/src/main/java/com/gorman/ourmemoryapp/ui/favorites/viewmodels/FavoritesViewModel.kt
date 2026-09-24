package com.gorman.ourmemoryapp.ui.favorites.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gorman.ourmemoryapp.domain.repository.FavoritesRepository
import com.gorman.ourmemoryapp.domain.repository.VeteransRepository
import com.gorman.ourmemoryapp.ui.favorites.models.FavoritesUiState
import com.gorman.ourmemoryapp.ui.favorites.models.toFavoriteUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    favoritesRepository: FavoritesRepository,
    private val veteransRepository: VeteransRepository
) : ViewModel() {

    val uiState = favoritesRepository.observeFavorites()
        .map { favorites ->
            FavoritesUiState.Success(
                veteransRepository.getAllVeterans()
                    .filter { it.id in favorites }
                    .map { it.toFavoriteUi() }
                    .toPersistentList()
            )
        }
        .catch<FavoritesUiState> { error ->
            Log.e(LOG_TAG, "Failed to load favorites", error)
            emit(FavoritesUiState.Error)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = FavoritesUiState.Loading
        )

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5000L
        private const val LOG_TAG = "FavoritesViewModel"
    }
}
