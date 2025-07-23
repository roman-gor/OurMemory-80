package com.gorman.ourmemoryapp

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class OurMemoryViewModel: ViewModel() {

    private val _veteranState = mutableStateOf<VeteranUiState>(VeteranUiState.Loading)
    val veteranState: State<VeteranUiState> = _veteranState
    private val _repository: FirebaseDB = FirebaseDB()

    init {
        fetchingData()
    }

    fun fetchingData()
    {
        viewModelScope.launch {
            try {
                val response = _repository.getAllVeterans()
                _veteranState.value = VeteranUiState.Success(response)
            }catch (e: Exception){
                _veteranState.value = VeteranUiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

}