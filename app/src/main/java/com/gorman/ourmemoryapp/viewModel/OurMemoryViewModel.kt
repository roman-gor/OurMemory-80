package com.gorman.ourmemoryapp.viewModel

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gorman.ourmemoryapp.data.FirebaseDB
import com.gorman.ourmemoryapp.data.Veteran
import com.gorman.ourmemoryapp.data.VeteranUiState
import kotlinx.coroutines.launch
import kotlin.collections.plus

class OurMemoryViewModel: ViewModel() {

    private val _veteranState = mutableStateOf<VeteranUiState>(VeteranUiState.Loading)
    val veteranState: State<VeteranUiState> = _veteranState

    private val _repository: FirebaseDB = FirebaseDB()

    private val _searchState = mutableStateOf("")
    val searchState: State<String> = _searchState

    private val _checkedWarState = mutableStateOf(true)
    val checkedWarState: State<Boolean> = _checkedWarState

    private val _checkedArtState = mutableStateOf(true)
    val checkedArtState: State<Boolean> = _checkedArtState

    private val _veteransList = mutableStateOf<List<Veteran>>(emptyList())

    val filteredVeterans: State<List<Veteran>> = derivedStateOf {
        val search = _searchState.value
        val war = _checkedWarState.value
        val art = _checkedArtState.value
        val all = _veteransList.value
        when {
            war && art -> {
                if(search.isBlank()) all
                else all.filter {
                    it.name.contains(search, ignoreCase = true)
                }
            }
            war -> {
                all.filter {
                    it.category == "War" && it.name.contains(search, ignoreCase = true)
                }
            }
            art -> {
                all.filter {
                    it.category == "Art" && it.name.contains(search, ignoreCase = true)
                }
            }
            else -> {
                emptyList()
            }
        }
    }

    init {
        fetchingData()
    }

    fun fetchingData()
    {
        viewModelScope.launch {
            try {
                _veteransList.value = _repository.getAllVeterans()
                _veteranState.value = VeteranUiState.Success(_veteransList.value)
            }catch (e: Exception){
                _veteranState.value = VeteranUiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun onSearchTextChange(value: String){ _searchState.value = value }

    fun onCheckedWarChange(value: Boolean){ _checkedWarState.value = value }

    fun onCheckedArtChange(value: Boolean){ _checkedArtState.value = value }
}