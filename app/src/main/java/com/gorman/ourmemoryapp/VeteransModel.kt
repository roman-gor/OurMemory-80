package com.gorman.ourmemoryapp

data class Veteran(
    val id: String = "",
    val name: String = "",
    val portrait: String = "",
    val baseInfo: String = "",
    val allInfo: String = "",
    val years: String= ""
)

sealed class VeteranUiState {
    object Loading : VeteranUiState()
    data class Success(val veterans: List<Veteran>) : VeteranUiState()
    data class Error(val message: String) : VeteranUiState()
}