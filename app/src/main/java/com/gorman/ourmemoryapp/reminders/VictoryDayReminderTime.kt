package com.gorman.ourmemoryapp.reminders

import java.time.Month
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

fun nextVictoryDayReminder(now: ZonedDateTime): ZonedDateTime {
    val thisYear = now
        .withMonth(Month.MAY.value)
        .withDayOfMonth(VICTORY_DAY)
        .truncatedTo(ChronoUnit.DAYS)
        .withHour(REMINDER_HOUR)
    return if (thisYear.isAfter(now)) thisYear else thisYear.plusYears(1)
}

private const val VICTORY_DAY = 9
private const val REMINDER_HOUR = 10
