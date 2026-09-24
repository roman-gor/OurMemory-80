package com.gorman.ourmemoryapp.ui.admin.veterans.models

import com.gorman.ourmemoryapp.ui.details.models.Reward
import com.gorman.ourmemoryapp.ui.details.models.parseRewards

private const val REWARDS_SEPARATOR = ","

fun String.toRewardCounts(): Map<Reward, Int> = parseRewards(this).associate { it.reward to it.count }

fun Map<Reward, Int>.toRewardsString(): String = Reward.entries
    .flatMap { reward -> List(get(reward) ?: 0) { reward.id } }
    .joinToString(REWARDS_SEPARATOR)
