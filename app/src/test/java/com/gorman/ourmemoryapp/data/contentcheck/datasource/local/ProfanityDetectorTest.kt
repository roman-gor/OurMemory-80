package com.gorman.ourmemoryapp.data.contentcheck.datasource.local

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfanityDetectorTest {

    private val detector = ProfanityDetector()

    @Test
    fun detectsPlainAndInflectedWords() {
        listOf("хуй", "Пиздец какой-то", "суками", "сукин сын", "бляць", "ты мразь", "ублюдки", "падла")
            .forEach { assertTrue(it, detector.containsProfanity(it)) }
    }

    @Test
    fun detectsMaskedWords() {
        listOf("х*й", "бл@ть", "п.зда", "бл*ть", "с_у_к_а", "сук@", "е#ать", "н@х")
            .forEach { assertTrue(it, detector.containsProfanity(it)) }
    }

    @Test
    fun detectsLookalikeSpelling() {
        listOf("xуй", "хuй", "пuзда", "cукa", "мpaзь", "6ля", "с у к а")
            .forEach { assertTrue(it, detector.containsProfanity(it)) }
    }

    @Test
    fun ordinaryMemorialTextPasses() {
        listOf(
            "Барсук", "сукно", "команда", "страхуй", "хулиган", "Воевал против фашистов, дошёл до Берлина",
            "Дом 88, кв 14", "1941-1945", "т.е. по-моему", "Северо-Западный фронт", "кто-нибудь из-за",
            "ivan.petrov@mail.ru", "+375 29 123-45-67", "г.р. 1923", "Орден Красной Звезды", "потребовать",
            "небо", "хлебал", "употребление", "Кузьма-Демьян", "e-mail", "сучок"
        ).forEach { assertFalse(it, detector.containsProfanity(it)) }
    }
}
