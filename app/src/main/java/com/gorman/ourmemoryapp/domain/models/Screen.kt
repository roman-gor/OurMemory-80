package com.gorman.ourmemoryapp.domain.models

sealed class Screen(val route: String) {
    object HomeScreen : Screen("homescreen")
    object DetailScreen : Screen("detailscreen")
    object InfoScreen : Screen("infoscreen")
    object IntroScreen : Screen("introscreen")
    object MapScreen : Screen("mapscreen")
    object MoreScreen : Screen("morescreen")
    object AdminLoginScreen : Screen("adminlogin")
    object AdminHomeScreen : Screen("adminhome")
    object AdminFeedbackScreen : Screen("adminfeedback")
    object AdminModerationScreen : Screen("adminmoderation")
    object AdminVeteransScreen : Screen("adminveterans")
    object AdminBurialsScreen : Screen("adminburials")
    object AdminToursScreen : Screen("admintours")
    object AdminTourEditorScreen : Screen("admintour") {
        const val TOUR_ID_ARG = "tourId"
        val pattern = "$route?$TOUR_ID_ARG={$TOUR_ID_ARG}"
        fun forTour(tourId: String) = "$route?$TOUR_ID_ARG=$tourId"
    }
    object AdminBurialEditorScreen : Screen("adminburial") {
        const val BURIAL_ID_ARG = "burialId"
        val pattern = "$route?$BURIAL_ID_ARG={$BURIAL_ID_ARG}"
        fun forBurial(burialId: String) = "$route?$BURIAL_ID_ARG=$burialId"
    }
    object AdminVeteranEditorScreen : Screen("adminveteran") {
        const val VETERAN_ID_ARG = "veteranId"
        val pattern = "$route?$VETERAN_ID_ARG={$VETERAN_ID_ARG}"
        fun forVeteran(veteranId: String) = "$route?$VETERAN_ID_ARG=$veteranId"
    }
    object AdminSubmissionScreen : Screen("adminsubmission") {
        const val SUBMISSION_ID_ARG = "submissionId"
        val pattern = "$route/{$SUBMISSION_ID_ARG}"
        fun withSubmission(submissionId: String) = "$route/$submissionId"
    }
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
    object FeedbackScreen : Screen("feedback") {
        const val VETERAN_ID_ARG = "veteranId"
        val pattern = "$route?$VETERAN_ID_ARG={$VETERAN_ID_ARG}"
        fun forVeteran(veteranId: String) = "$route?$VETERAN_ID_ARG=$veteranId"
    }
}
