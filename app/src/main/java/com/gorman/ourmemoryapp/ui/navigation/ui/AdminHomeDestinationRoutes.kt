package com.gorman.ourmemoryapp.ui.navigation.ui

import com.gorman.ourmemoryapp.domain.models.Screen
import com.gorman.ourmemoryapp.ui.admin.home.models.AdminHomeDestination

fun AdminHomeDestination.route() = when (this) {
    AdminHomeDestination.MODERATION -> Screen.AdminModerationScreen.route
    AdminHomeDestination.FEEDBACK -> Screen.AdminFeedbackScreen.route
    AdminHomeDestination.VETERANS -> Screen.AdminVeteransScreen.route
    AdminHomeDestination.BURIALS -> Screen.AdminBurialsScreen.route
    AdminHomeDestination.TOURS -> Screen.AdminToursScreen.route
    AdminHomeDestination.GUIDE -> Screen.AdminGuideScreen.route
    AdminHomeDestination.ADMINS -> Screen.AdminAdminsScreen.route
}
