package com.gorman.ourmemoryapp.ui.navigation.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.domain.models.Screen

enum class TopLevelTab(
    val screen: Screen,
    @param:DrawableRes val iconRes: Int,
    @param:StringRes val labelRes: Int
) {
    VETERANS(Screen.HomeScreen, R.drawable.person, R.string.veterans),
    MAP(Screen.MapScreen, R.drawable.map, R.string.map),
    ABOUT(Screen.InfoScreen, R.drawable.monument, R.string.about_cemetery),
    MORE(Screen.MoreScreen, R.drawable.more_horiz, R.string.more),
    ADMIN(Screen.AdminHomeScreen, R.drawable.shield, R.string.admin)
}
