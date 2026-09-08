package com.gorman.ourmemoryapp.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gorman.ourmemoryapp.domain.repository.VeteransRepository
import com.gorman.ourmemoryapp.ui.states.HomeUiIntent
import com.gorman.ourmemoryapp.ui.states.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: VeteransRepository
) : ViewModel() {

    private val searchState = MutableStateFlow("")

    private val checkedWarState = MutableStateFlow(true)

    private val checkedArtState = MutableStateFlow(true)

    val uiState: StateFlow<HomeUiState> = combine(
        searchState,
        checkedWarState,
        checkedArtState
    ) { search, war, art ->
        val veteransList = repository.getAllVeterans()
        val filteredVeterans = when {
            war && art -> {
                if (search.isBlank()) {
                    veteransList
                } else {
                    veteransList.filter {
                        it.name.contains(search, ignoreCase = true)
                    }
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
            checkedArt = art
        ) as HomeUiState
    }.catch { error ->
        HomeUiState.Error(error)
    }.stateIn(
        scope = viewModelScope,
        initialValue = HomeUiState.Loading,
        started = SharingStarted.WhileSubscribed(5000L)
    )

    fun onUiIntent(intent: HomeUiIntent) {
        when (intent) {
            is HomeUiIntent.OnCheckedArtChange -> checkedArtState.value = intent.value
            is HomeUiIntent.OnCheckedWarChange -> checkedWarState.value = intent.value
            is HomeUiIntent.OnSearchChange -> searchState.value = intent.text
        }
    }
}
