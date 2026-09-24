package com.gorman.ourmemoryapp.domain.models

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class VeteranAnniversariesTest {

    @Test
    fun matchesBirthAndDeathByDayAndMonth() {
        val veteran = Veteran(birthDate = "1905-08-03", deathDate = "1966-08-03")

        assertEquals(
            listOf(Anniversary(AnniversaryKind.BIRTHDAY, 1905), Anniversary(AnniversaryKind.MEMORY_DAY, 1966)),
            veteran.anniversariesOn(LocalDate.of(2026, 8, 3))
        )
    }

    @Test
    fun ignoresOtherDaysAndBrokenDates() {
        val day = LocalDate.of(2026, 8, 3)

        assertEquals(emptyList<Anniversary>(), Veteran(birthDate = "1905-08-03").anniversariesOn(day.plusDays(1)))
        assertEquals(emptyList<Anniversary>(), Veteran(birthDate = "03.08.1905").anniversariesOn(day))
    }
}
