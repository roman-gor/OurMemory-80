package com.gorman.ourmemoryapp.ui.home.models

import com.gorman.ourmemoryapp.domain.models.AnniversaryKind
import com.gorman.ourmemoryapp.domain.models.Veteran
import com.gorman.ourmemoryapp.domain.models.anniversariesOn
import java.time.LocalDate

fun List<Veteran>.anniversariesOn(today: LocalDate): List<AnniversaryUi> = flatMap { veteran ->
    veteran.anniversariesOn(today).map { anniversary ->
        AnniversaryUi(
            veteranId = veteran.id,
            name = veteran.name,
            portrait = veteran.portrait,
            isBirthday = anniversary.kind == AnniversaryKind.BIRTHDAY,
            year = anniversary.year
        )
    }
}
