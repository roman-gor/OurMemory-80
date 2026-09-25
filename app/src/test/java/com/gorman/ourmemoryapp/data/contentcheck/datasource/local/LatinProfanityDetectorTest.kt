package com.gorman.ourmemoryapp.data.contentcheck.datasource.local

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LatinProfanityDetectorTest {

    private val detector = LatinProfanityDetector()

    @Test
    fun detectsEnglishProfanity() {
        listOf("fuck", "FUCKING idiot", "f*ck", "sh!t", "fuuuck", "bitch", "fuсk", "b1tch", "a\$\$hole")
            .forEach { assertTrue(it, detector.containsProfanity(it)) }
    }

    @Test
    fun detectsTransliteratedRussian() {
        listOf("pizdec", "blyat", "xuy", "suka", "nahuy")
            .forEach { assertTrue(it, detector.containsProfanity(it)) }
    }

    @Test
    fun ordinaryTextPasses() {
        listOf(
            "Hello",
            "ivan.petrov@mail.ru",
            "Scunthorpe",
            "night",
            "Dickens",
            "shitake",
            "Great Patriotic War",
            "Воспоминания"
        )
            .forEach { assertFalse(it, detector.containsProfanity(it)) }
    }
}
