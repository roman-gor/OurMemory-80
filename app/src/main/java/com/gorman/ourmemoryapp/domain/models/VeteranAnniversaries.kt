package com.gorman.ourmemoryapp.domain.models

import java.time.LocalDate
import java.time.MonthDay
import java.time.format.DateTimeParseException

fun Veteran.anniversariesOn(date: LocalDate): List<Anniversary> = listOfNotNull(
    birthDate.anniversaryOn(date, AnniversaryKind.BIRTHDAY),
    deathDate.anniversaryOn(date, AnniversaryKind.MEMORY_DAY)
)

private fun String.anniversaryOn(date: LocalDate, kind: AnniversaryKind): Anniversary? {
    val parsed = try {
        LocalDate.parse(this)
    } catch (_: DateTimeParseException) {
        return null
    }
    return Anniversary(kind = kind, year = parsed.year).takeIf { MonthDay.from(parsed) == MonthDay.from(date) }
}
