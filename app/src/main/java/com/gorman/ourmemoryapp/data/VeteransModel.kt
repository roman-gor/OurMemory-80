package com.gorman.ourmemoryapp.data

data class Veteran(
    val id: String = "",
    val name: String = "",
    val portrait: String = "",
    val baseInfo: String = "",
    val allInfo: String = "",
    val years: String= "",
    val category: String = "",
    val rewards: String = "",
    val veteransInfo: List<String> = emptyList()
)

sealed class VeteranUiState {
    object Loading : VeteranUiState()
    data class Success(val veterans: List<Veteran>) : VeteranUiState()
    data class Error(val message: String) : VeteranUiState()
}

sealed class Screen(val route: String)
{
    object HomeScreen: Screen("homescreen")
    object DetailScreen: Screen("detailscreen")
    object InfoScreen: Screen("infoscreen")
    object IntroScreen: Screen("introscreen")
}