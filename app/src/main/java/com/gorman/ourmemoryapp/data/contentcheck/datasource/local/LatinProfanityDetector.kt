package com.gorman.ourmemoryapp.data.contentcheck.datasource.local

import javax.inject.Inject

class LatinProfanityDetector @Inject constructor() {

    fun containsProfanity(text: String) =
        (listOf(text) + MaskedWords.expand(text, LATIN_LETTERS)).any { candidate ->
            normalizedWords(candidate).any { word -> word in EXACT_WORDS || INFIX_ROOTS.any { it in word } }
        }

    private fun normalizedWords(text: String): List<String> {
        val words = text.lowercase()
            .map { LOOKALIKES[it] ?: it }
            .joinToString("")
            .split(NON_LETTERS)
            .filter { it.isNotEmpty() }
        return words + words.map { it.replace(REPEATED_LETTERS, "$1") }
    }

    companion object {
        private const val LATIN_LETTERS = "abcdefghijklmnopqrstuvwxyz"
        private val REPEATED_LETTERS = Regex("(\\p{L})\\1+")
        private val NON_LETTERS = Regex("[^\\p{L}]+")
        private val LOOKALIKES = mapOf(
            'а' to 'a', 'о' to 'o', 'е' to 'e', 'р' to 'p', 'с' to 'c', 'х' to 'x', 'у' to 'y',
            'к' to 'k', 'м' to 'm', 'т' to 't', 'і' to 'i',
            '0' to 'o', '1' to 'i', '3' to 'e', '4' to 'a', '5' to 's', '!' to 'i', '$' to 's'
        )
        private val INFIX_ROOTS = listOf(
            "fuck", "bitch", "whore", "nigger", "nigga", "asshole", "motherf", "bullshit",
            "pizd", "pezd", "blyad", "blyat", "bliat", "xuy", "xuj", "huyn", "nahuy", "nahui",
            "ebal", "eban", "ebat", "zaeb", "naeb", "pidor", "pidar", "mudak"
        )
        private val EXACT_WORDS = setOf(
            "shit", "shitty", "cunt", "cunts", "dick", "slut", "fag", "faggot", "fck", "fuk", "bastard",
            "huy", "huj", "blya", "suka", "suki"
        )
    }
}
