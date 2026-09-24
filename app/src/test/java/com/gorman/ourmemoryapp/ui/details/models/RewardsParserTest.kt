package com.gorman.ourmemoryapp.ui.details.models

import org.junit.Assert.assertEquals
import org.junit.Test

class RewardsParserTest {

    @Test
    fun repeatedRewardsAreGroupedWithCountInFirstSeenOrder() {
        val rewards = parseRewards("9, 11,9,5,11,9")

        assertEquals(
            listOf(RewardUi(Reward.RED_STAR, 3), RewardUi(Reward.FOR_COURAGE, 2), RewardUi(Reward.HERO_USSR, 1)),
            rewards
        )
    }

    @Test
    fun unknownAndMalformedIdsAreSkipped() {
        assertEquals(listOf(RewardUi(Reward.LENIN, 1)), parseRewards("4,99,abc,"))
    }

    @Test
    fun emptyStringHasNoRewards() {
        assertEquals(emptyList<RewardUi>(), parseRewards(""))
    }
}
