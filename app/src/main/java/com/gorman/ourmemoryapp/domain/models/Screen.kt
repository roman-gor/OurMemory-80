package com.gorman.ourmemoryapp.domain.models

sealed class Screen(val route: String) {
    object HomeScreen : Screen("homescreen")
    object DetailScreen : Screen("detailscreen")
    object InfoScreen : Screen("infoscreen")
    object IntroScreen : Screen("introscreen")
    object MapScreen : Screen("mapscreen")
    object BurialMapScreen : Screen("burialmap") {
        const val BURIAL_ID_ARG = "burialId"
        val pattern = "$route/{$BURIAL_ID_ARG}"
        fun withBurial(burialId: String) = "$route/$burialId"
    }
}
