package com.gorman.ourmemoryapp.ui.common.models

object VeteranLink {
    const val BASE_URL = "https://chatroom-85fb8.web.app/veteran"
    private const val QUERY_START = '?'
    private const val PATH_SEPARATOR = '/'

    fun forId(veteranId: String) = "$BASE_URL$PATH_SEPARATOR$veteranId"

    fun parseVeteranId(rawValue: String): String? = rawValue.trim()
        .takeIf { it.startsWith("$BASE_URL$PATH_SEPARATOR") }
        ?.removePrefix("$BASE_URL$PATH_SEPARATOR")
        ?.substringBefore(QUERY_START)
        ?.trimEnd(PATH_SEPARATOR)
        ?.takeIf { it.isNotBlank() && PATH_SEPARATOR !in it }
}
