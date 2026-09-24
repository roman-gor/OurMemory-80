package com.gorman.ourmemoryapp.reminders

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class VictoryDayReminderTimeTest {

    private val minsk = ZoneId.of("Europe/Minsk")

    private fun at(text: String) = ZonedDateTime.of(java.time.LocalDateTime.parse(text), minsk)

    @Test
    fun beforeVictoryDayRemindsThisYear() {
        assertEquals(at("2026-05-09T10:00"), nextVictoryDayReminder(at("2026-03-01T12:00")))
    }

    @Test
    fun onVictoryDayMorningRemindsSameDay() {
        assertEquals(at("2026-05-09T10:00"), nextVictoryDayReminder(at("2026-05-09T09:59")))
    }

    @Test
    fun afterReminderTimeRemindsNextYear() {
        assertEquals(at("2027-05-09T10:00"), nextVictoryDayReminder(at("2026-05-09T10:00")))
        assertEquals(at("2027-05-09T10:00"), nextVictoryDayReminder(at("2026-09-24T12:00")))
    }
}
