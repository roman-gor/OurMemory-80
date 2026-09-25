package com.gorman.ourmemoryapp.ui.home.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gorman.ourmemoryapp.domain.repository.VeteransRepository
import com.gorman.ourmemoryapp.ui.home.models.HomeUiIntent
import com.gorman.ourmemoryapp.ui.home.models.HomeUiState
import com.gorman.ourmemoryapp.ui.home.models.anniversariesOn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: VeteransRepository,
    private val clock: Clock
) : ViewModel() {

    private val searchState = MutableStateFlow("")

    private val checkedWarState = MutableStateFlow(true)

    private val checkedArtState = MutableStateFlow(true)

    private val isRefreshingState = MutableStateFlow(false)

    private val reloadState = MutableStateFlow(0)

    val uiState: StateFlow<HomeUiState> = combine(
        searchState,
        checkedWarState,
        checkedArtState,
        isRefreshingState,
        reloadState
    ) { search, war, art, isRefreshing, _ ->
        val veteransList = repository.getAllVeterans()
        val filteredVeterans = when {
            war && art -> {
                if (search.isBlank()) {
                    veteransList
                } else {
                    veteransList.filter { it.name.contains(search, ignoreCase = true) }
                }
            }
            war -> {
                veteransList.filter {
                    it.category == "War" && it.name.contains(search, ignoreCase = true)
                }
            }
            art -> {
                veteransList.filter {
                    it.category == "Art" && it.name.contains(search, ignoreCase = true)
                }
            }
            else -> {
                emptyList()
            }
        }

        HomeUiState.Success(
            veterans = filteredVeterans.toPersistentList(),
            search = search,
            checkedWar = war,
            checkedArt = art,
            anniversaries = if (search.isBlank()) {
                veteransList.anniversariesOn(LocalDate.now(clock)).toPersistentList()
            } else {
                persistentListOf()
            },
            isRefreshing = isRefreshing
        ) as HomeUiState
    }.catch { error ->
        emit(HomeUiState.Error(error))
    }.stateIn(
        scope = viewModelScope,
        initialValue = HomeUiState.Loading,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS)
    )

    fun onUiIntent(intent: HomeUiIntent) {
        when (intent) {
            is HomeUiIntent.OnCheckedArtChange -> checkedArtState.value = intent.value
            is HomeUiIntent.OnCheckedWarChange -> checkedWarState.value = intent.value
            is HomeUiIntent.OnSearchChange -> searchState.value = intent.text
            HomeUiIntent.OnRefresh -> refresh()
        }
    }

    private fun refresh() {
        if (isRefreshingState.value) return
        viewModelScope.launch {
            isRefreshingState.value = true
            runCatching {
                repository.invalidate()
                repository.getAllVeterans()
            }
                .onFailure { Log.e(LOG_TAG, "Failed to refresh veterans", it) }
            reloadState.update { it + 1 }
            isRefreshingState.value = false
        }
    }

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5000L
        private const val LOG_TAG = "HomeViewModel"
    }
}
