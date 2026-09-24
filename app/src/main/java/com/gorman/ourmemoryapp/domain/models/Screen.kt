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
    object TourScreen : Screen("tour") {
        const val TOUR_ID_ARG = "tourId"
        val pattern = "$route/{$TOUR_ID_ARG}"
        fun withTour(tourId: String) = "$route/$tourId"
    }
    object SubmissionScreen : Screen("submission") {
        const val VETERAN_ID_ARG = "veteranId"
        val pattern = "$route/{$VETERAN_ID_ARG}"
        fun forVeteran(veteranId: String) = "$route/$veteranId"
    }
}
