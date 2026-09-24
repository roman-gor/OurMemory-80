package com.gorman.ourmemoryapp.ui.admin.guide.models

import androidx.annotation.ArrayRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.gorman.ourmemoryapp.R

enum class GuideSection(
    @param:StringRes val titleRes: Int,
    @param:DrawableRes val iconRes: Int,
    @param:ArrayRes val stepsRes: Int,
    @param:StringRes val tipRes: Int
) {
    BURIAL(R.string.burial_place, R.drawable.monument, R.array.burial_place_steps, R.string.check_new_marker_msg),
    VETERAN(R.string.veteran, R.drawable.person, R.array.veteran_steps, R.string.save_button_disabled_msg),
    TOUR(R.string.tour, R.drawable.directions_walk, R.array.tour_steps, R.string.stop_order_sets_route_msg),
    VOICE_OVER(R.string.voice_over, R.drawable.mic, R.array.voice_over_steps, R.string.check_stresses_before_msg),
    REQUESTS(
        R.string.requests_and_messages,
        R.drawable.mail,
        R.array.requests_steps,
        R.string.reply_shown_to_author_msg
    ),
    TROUBLESHOOTING(
        R.string.if_something_is_wrong,
        R.drawable.help,
        R.array.troubleshooting_steps,
        R.string.deleted_items_cannot_be_restored_msg
    )
}
