package com.gorman.ourmemoryapp.data.contentcheck.datasource.local

import javax.inject.Inject

class ProfanityDetector @Inject constructor() {

    fun containsProfanity(text: String) =
        (listOf(text) + MaskedWords.expand(text, CYRILLIC_LETTERS)).any { candidate ->
            LOOKALIKE_VARIANTS.any { lookalikes -> normalizedWords(candidate, lookalikes).any(::isProfane) }
        }

    private fun isProfane(word: String): Boolean {
        val cleaned = SAFE_FRAGMENTS.fold(word) { current, safe -> current.replace(safe, "") }
        return cleaned in EXACT_WORDS ||
            PREFIX_ROOTS.any { cleaned.startsWith(it) } ||
            INFIX_ROOTS.any { it in cleaned }
    }

    private fun normalizedWords(text: String, lookalikes: Map<Char, Char>): List<String> {
        val normalized = text.lowercase()
            .map { lookalikes[it] ?: it }
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
        private const val CYRILLIC_LETTERS = "абвгдежзийклмнопрстуфхцчшщъыьэюя"
        private val REPEATED_LETTERS = Regex("(\\p{L})\\1+")
        private val NON_LETTERS = Regex("[^\\p{L}]+")
        private val LOOKALIKES = mapOf(
            'a' to 'а', 'o' to 'о', 'e' to 'е', 'p' to 'р', 'c' to 'с', 'x' to 'х', 'y' to 'у',
            'k' to 'к', 'm' to 'м', 'b' to 'в', 'h' to 'н', 't' to 'т', 'u' to 'и', 'n' to 'п',
            'r' to 'г', 'i' to 'и', 'і' to 'и', 'ё' to 'е',
            '3' to 'з', '0' to 'о', '6' to 'б', '4' to 'ч'
        )
        private val LOOKALIKE_VARIANTS = listOf(LOOKALIKES, LOOKALIKES + ('u' to 'у'))
        private val SAFE_FRAGMENTS = listOf(
            "страху", "хлеб", "хулиган", "команд", "мандарин", "мандат", "греб", "колеб", "небан", "небал", "ребал"
        )
        private val INFIX_ROOTS = listOf(
            "хуй", "хуе", "хуя", "хуи", "пизд", "пезд", "бляд", "ебан", "ебал", "ебат", "ебац", "ебуч",
            "ебну", "еблан", "ебло", "заеб", "наеб", "выеб", "уеб", "поеб", "съеб", "сьеб", "залуп",
            "мудак", "мудил", "мудозвон", "гандон", "пидор", "пидар", "пидр", "шлюх", "херн", "долбоеб",
            "ублюд"
        )
        private val PREFIX_ROOTS = listOf(
            "сука", "суки", "сукам", "сукин", "сучар", "сучий", "сучье", "падл", "шалав", "мразот"
        )
        private val EXACT_WORDS = setOf(
            "бля", "блять", "бляць", "сука", "суки", "сучка", "хули", "хер", "манда", "курва", "пох", "нах",
            "ебу", "еби", "ебет", "ебешь", "ебут", "ебись",
            "мразь", "мрази", "тварь", "твари", "чмо", "урод", "уроды", "уродина", "дебил", "дебилы",
            "выродок", "выродки", "гнида", "гниды"
        )
    }
}
