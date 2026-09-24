package com.gorman.ourmemoryapp.ui.details.models

fun parseRewards(rewards: String): List<RewardUi> {
    return rewards
        .split(REWARDS_SEPARATOR)
        .mapNotNull { it.trim().toIntOrNull()?.let(Reward::fromId) }
        .groupingBy { it }
        .eachCount()
        .map { (reward, count) -> RewardUi(reward = reward, count = count) }
}

private const val REWARDS_SEPARATOR = ","
