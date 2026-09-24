package com.gorman.ourmemoryapp.ui.details.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.gorman.ourmemoryapp.R

enum class Reward(val id: Int, @param:DrawableRes val iconRes: Int, @param:StringRes val nameRes: Int) {
    RED_BANNER(1, R.drawable.red_znamya, R.string.red_znamya),
    SUVOROV_FIRST(2, R.drawable.suvorov_1, R.string.suvorov_1),
    SUVOROV_SECOND(3, R.drawable.suvorov_1, R.string.suvorov_2),
    LENIN(4, R.drawable.lenin, R.string.lenin),
    HERO_USSR(5, R.drawable.geroj_sssr, R.string.geroj_sssr),
    PARTISAN(6, R.drawable.partizan, R.string.partizan),
    VICTORY_OVER_GERMANY(7, R.drawable.za_pobedu_germany, R.string.za_pobedu_germany),
    PATRIOTIC_WAR(8, R.drawable.otech_war, R.string.otech_war),
    RED_STAR(9, R.drawable.red_star, R.string.red_star),
    BADGE_OF_HONOUR(10, R.drawable.orden_znak_pocheta, R.string.orden_znak_pocheta),
    FOR_COURAGE(11, R.drawable.za_otvagu, R.string.za_otvagu),
    PEOPLES_ARTIST(12, R.drawable.narodny_artist, R.string.narodny_artist);

    companion object {
        fun fromId(id: Int) = entries.firstOrNull { it.id == id }
    }
}
