package com.gorman.ourmemoryapp.ui.home.models

import com.gorman.ourmemoryapp.domain.models.Veteran
import java.time.LocalDate

fun List<Veteran>.anniversariesOn(today: LocalDate): List<AnniversaryUi> = flatMap { veteran ->
    listOfNotNull(
        veteran.anniversaryOf(veteran.birthDate, today, isBirthday = true),
        veteran.anniversaryOf(veteran.deathDate, today, isBirthday = false)
    )
}

private fun Veteran.anniversaryOf(date: String, today: LocalDate, isBirthday: Boolean): AnniversaryUi? {
    val parsed = runCatching { LocalDate.parse(date) }.getOrNull() ?: return null
    if (parsed.month != today.month || parsed.dayOfMonth != today.dayOfMonth) return null
    return AnniversaryUi(
        veteranId = id,
        name = name,
        portrait = portrait,
        isBirthday = isBirthday,
        year = parsed.year
    )
}
