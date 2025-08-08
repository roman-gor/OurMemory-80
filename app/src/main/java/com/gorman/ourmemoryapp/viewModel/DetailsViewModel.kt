package com.gorman.ourmemoryapp.viewModel

import android.annotation.SuppressLint
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gorman.ourmemoryapp.data.FirebaseDB
import com.gorman.ourmemoryapp.data.Veteran
import com.gorman.ourmemoryapp.data.VeteranUiState
import kotlinx.coroutines.launch
import kotlin.collections.listOf

@SuppressLint("MutableCollectionMutableState")
class DetailsViewModel: ViewModel() {

    private val _veteranState = mutableStateOf<VeteranUiState>(VeteranUiState.Loading)
    val veteranState: State<VeteranUiState> = _veteranState
    private val _rewardsState = mutableStateOf<List<Int>>(emptyList())
    val rewardsState: State<List<Int>> = _rewardsState
    private val _repository: FirebaseDB = FirebaseDB()
    private val _additionalInfoState = mutableStateOf<List<String>>(emptyList())
    private val _additionalText = mutableStateOf<List<String>>(emptyList())
    val additionalText: State<List<String>> = _additionalText
    private val _additionalRes = mutableStateOf(mutableMapOf<String, String>())
    val additionalRes: State<MutableMap<String, String>> = _additionalRes

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

    fun loadRewards(veteran: Veteran)
    {
        val rewardsList = veteran.rewards
        if(rewardsList.isNotBlank()) {
            _rewardsState.value = rewardsList
                .split(',')
                .mapNotNull { it.toIntOrNull() }
        }
        else {
            _rewardsState.value = emptyList()
        }
    }

    fun loadAdditionalInfo(veteran: Veteran){
        _additionalInfoState.value = veteran.veteransInfo
        val newMap = _additionalRes.value.toMutableMap()
        val newText = _additionalText.value.toMutableList()
        _additionalInfoState.value.forEach { info ->
            if (info.contains("http")){
                if (info.contains("|")){
                    val url = info.split("|").getOrNull(0) ?: ""
                    val describe = info.split("|").getOrNull(1) ?: ""
                    newMap[url] = describe
                }
                else newMap[info] = ""
            }
            else {
                newText += info
            }
        }
        _additionalText.value = newText
        _additionalRes.value = newMap
    }
}