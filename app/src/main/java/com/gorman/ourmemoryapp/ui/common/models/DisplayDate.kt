package com.gorman.ourmemoryapp.ui.common.models

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val displayDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

fun Long.toDisplayDate(zoneId: ZoneId = ZoneId.systemDefault()): String =
    displayDateFormatter.format(Instant.ofEpochMilli(this).atZone(zoneId))
