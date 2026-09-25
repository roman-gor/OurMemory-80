package com.gorman.ourmemoryapp.data.contentcheck.datasource.local

object MaskedWords {

    private const val MAX_MASK_RUNS = 2
    private val MASK_RUN = Regex("[*#%$@_.\\-]+")
    private val TOKEN_SEPARATORS = Regex("[\\s,!?;:()\"«»]+")
    private val EDGE_PUNCTUATION = charArrayOf('.', '-', '_')

    fun expand(text: String, letters: String): List<String> = text.lowercase()
        .split(TOKEN_SEPARATORS)
        .map { token -> token.trim { it in EDGE_PUNCTUATION } }
        .filter { token -> MASK_RUN.containsMatchIn(token) && token.any(Char::isLetter) }
        .flatMap { token -> expandToken(token, letters) }

    private fun expandToken(token: String, letters: String): List<String> {
        val parts = token.split(MASK_RUN)
        if (parts.size - 1 > MAX_MASK_RUNS) return listOf(parts.joinToString(""))
        val replacements = listOf("") + letters.map(Char::toString)
        return parts.drop(1).fold(listOf(parts.first())) { prefixes, part ->
            prefixes.flatMap { prefix -> replacements.map { prefix + it + part } }
        }
    }
}
