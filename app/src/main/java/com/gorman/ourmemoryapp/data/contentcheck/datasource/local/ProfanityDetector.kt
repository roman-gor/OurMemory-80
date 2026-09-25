package com.gorman.ourmemoryapp.data.contentcheck.datasource.local

import javax.inject.Inject

class ProfanityDetector @Inject constructor() {

    fun containsProfanity(text: String) = normalizedWords(text).any { word ->
        val cleaned = SAFE_FRAGMENTS.fold(word) { current, safe -> current.replace(safe, "") }
        cleaned in EXACT_WORDS || INFIX_ROOTS.any { it in cleaned }
    }

    private fun normalizedWords(text: String): List<String> {
        val normalized = text.lowercase()
            .map { LOOKALIKES[it] ?: it }
            .joinToString("")
            .replace(REPEATED_LETTERS, "$1")
        val words = normalized.split(NON_LETTERS).filter { it.isNotEmpty() }
        return words + joinSingleLetters(words)
    }

    private fun joinSingleLetters(words: List<String>) = words
        .fold(mutableListOf(StringBuilder())) { groups, word ->
            if (word.length == 1) groups.last().append(word) else groups.add(StringBuilder())
            groups
        }
        .map { it.toString() }
        .filter { it.length >= MIN_JOINED_LENGTH }

    companion object {
        private const val MIN_JOINED_LENGTH = 3
        private val REPEATED_LETTERS = Regex("(\\p{L})\\1+")
        private val NON_LETTERS = Regex("[^\\p{L}]+")
        private val LOOKALIKES = mapOf(
            'a' to 'а', 'o' to 'о', 'e' to 'е', 'p' to 'р', 'c' to 'с', 'x' to 'х', 'y' to 'у',
            'k' to 'к', 'm' to 'м', 'b' to 'в', 'h' to 'н', 't' to 'т', 'ё' to 'е',
            '3' to 'з', '0' to 'о', '6' to 'б', '@' to 'а'
        )
        private val SAFE_FRAGMENTS = listOf(
            "страху", "хлеб", "хулиган", "команд", "мандарин", "мандат", "греб", "колеб", "небан", "небал", "ребал"
        )
        private val INFIX_ROOTS = listOf(
            "хуй", "хуе", "хуя", "хуи", "пизд", "пезд", "бляд", "ебан", "ебал", "ебат", "ебуч",
            "ебну", "еблан", "ебло", "заеб", "наеб", "выеб", "уеб", "поеб", "съеб", "сьеб", "залуп",
            "мудак", "мудил", "мудозвон", "гандон", "пидор", "пидар", "пидр", "шлюх", "херн", "долбоеб"
        )
        private val EXACT_WORDS = setOf(
            "бля", "блять", "сука", "суки", "сучка", "сучара", "хули", "хер", "манда", "курва", "пох", "нах",
            "ебу", "еби", "ебет", "ебешь", "ебут", "ебись"
        )
    }
}
