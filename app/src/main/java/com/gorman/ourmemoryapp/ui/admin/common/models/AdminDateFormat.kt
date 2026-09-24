package com.gorman.ourmemoryapp.ui.admin.common.models

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val adminDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

fun Long.toAdminDate(zoneId: ZoneId = ZoneId.systemDefault()): String =
    adminDateFormatter.format(Instant.ofEpochMilli(this).atZone(zoneId))
