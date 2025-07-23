package com.gorman.ourmemoryapp.viewModel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gorman.ourmemoryapp.data.FirebaseDB
import com.gorman.ourmemoryapp.data.VeteranUiState
import kotlinx.coroutines.launch

class DetailsViewModel: ViewModel() {

    private val _veteranState = mutableStateOf<VeteranUiState>(VeteranUiState.Loading)
    val veteranState: State<VeteranUiState> = _veteranState
    private val _repository: FirebaseDB = FirebaseDB()

    fun loadVeteranById(id: String)
    {
        viewModelScope.launch {
            try {
                val response = _repository.getAllVeterans()
                val selectedVeteran = response.find { it.id == id }
                if (selectedVeteran != null)
                    _veteranState.value = VeteranUiState.Success(listOf(selectedVeteran))
                else
                    _veteranState.value = VeteranUiState.Error("Не обнаружен ветеран с ID = $id")
            }catch (e: Exception){
                _veteranState.value = VeteranUiState.Error("${e.message}")
            }
        }
    }

}