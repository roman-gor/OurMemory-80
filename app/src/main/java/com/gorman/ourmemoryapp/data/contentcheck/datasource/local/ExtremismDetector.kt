package com.gorman.ourmemoryapp.data.contentcheck.datasource.local

import javax.inject.Inject

class ExtremismDetector @Inject constructor() {

    fun containsExtremism(text: String): Boolean {
        val words = text.lowercase().replace('ё', 'е').split(SEPARATORS).filter { it.isNotEmpty() }
        val normalized = words.joinToString(separator = " ", prefix = " ", postfix = " ")
        return SYMBOLS.any { it in text } ||
            NUMERIC_CODES.containsMatchIn(text) ||
            PHRASES.any { " $it " in normalized }
    }

    companion object {
        private val SEPARATORS = Regex("[^\\p{L}\\p{N}]+")
        private val NUMERIC_CODES = Regex("(?<!\\d)14\\s*[/\\\\-]?\\s*88(?!\\d)")
        private val SYMBOLS = listOf("卐", "卍", "ϟϟ", "ᛋᛋ", "ᛊᛊ")
        private val PHRASES = listOf(
            "хайль", "зиг хайль", "хайль гитлер", "слава гитлеру", "гитлер прав", "гитлер был прав",
            "зига", "зигу", "зиговать", "зигует", "зигуют",
            "heil", "sieg heil", "heil hitler", "white power", "white pride"
        )
    }
}
