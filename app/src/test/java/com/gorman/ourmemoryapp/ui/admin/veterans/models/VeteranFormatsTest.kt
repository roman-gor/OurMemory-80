package com.gorman.ourmemoryapp.ui.admin.veterans.models

import com.gorman.ourmemoryapp.domain.models.Veteran
import com.gorman.ourmemoryapp.ui.details.models.Reward
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VeteranFormatsTest {

    @Test
    fun infoBlocksRoundTripRealFormat() {
        val info = listOf(
            "Родился в 1905 году.",
            "https://disk.yandex.ru/i/abc|Наградной лист",
            "https://example.com/photo.jpg"
        )

        val blocks = info.toInfoBlocks()

        assertEquals(
            listOf(
                InfoBlock.Paragraph("Родился в 1905 году."),
                InfoBlock.Media("https://disk.yandex.ru/i/abc", "Наградной лист"),
                InfoBlock.Media("https://example.com/photo.jpg", "")
            ),
            blocks
        )
        assertEquals(info, blocks.toVeteransInfo())
    }

    @Test
    fun blankParagraphsAreDroppedOnSave() {
        assertEquals(
            listOf("Текст"),
            listOf(InfoBlock.Paragraph("  "), InfoBlock.Paragraph("Текст")).toVeteransInfo()
        )
    }

    @Test
    fun rewardsRoundTripKeepsRepeatsInIdOrder() {
        val counts = "9,9,11,1".toRewardCounts()

        assertEquals(mapOf(Reward.RED_STAR to 2, Reward.FOR_COURAGE to 1, Reward.RED_BANNER to 1), counts)
        assertEquals("1,9,9,11", counts.toRewardsString())
        assertEquals("", emptyMap<Reward, Int>().toRewardsString())
    }

    @Test
    fun nextVeteranIdIsMaxNumericPlusOne() {
        val veterans = listOf(Veteran(id = "3"), Veteran(id = "15"), Veteran(id = "draft"), Veteran(id = "7"))

        assertEquals("16", veterans.nextVeteranId())
        assertEquals("1", emptyList<Veteran>().nextVeteranId())
    }

    @Test
    fun blocksMoveWithinBounds() {
        val items = listOf("a", "b", "c")

        assertEquals(listOf("b", "a", "c"), items.moved(0, 1))
        assertEquals(listOf("a", "c", "b"), items.moved(2, -1))
        assertEquals(items, items.moved(0, -1))
        assertEquals(items, items.moved(2, 1))
    }

    @Test
    fun datesMustBeIsoOrBlank() {
        assertTrue("".isBlankOrIsoDate())
        assertTrue("1905-08-03".isBlankOrIsoDate())
        assertFalse("03.08.1905".isBlankOrIsoDate())
        assertFalse("1905-02-30".isBlankOrIsoDate())
    }
}
