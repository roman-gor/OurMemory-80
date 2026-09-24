package com.gorman.ourmemoryapp.ui.admin.veterans.models

import java.time.LocalDate
import java.time.format.DateTimeParseException

fun String.isBlankOrIsoDate(): Boolean = isBlank() || toIsoDateOrNull() != null

fun String.toIsoDateOrNull(): LocalDate? = try {
    LocalDate.parse(this)
} catch (_: DateTimeParseException) {
    null
}
