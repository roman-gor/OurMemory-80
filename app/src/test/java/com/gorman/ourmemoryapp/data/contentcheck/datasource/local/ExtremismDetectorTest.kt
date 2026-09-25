package com.gorman.ourmemoryapp.data.contentcheck.datasource.local

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExtremismDetectorTest {

    private val detector = ExtremismDetector()

    @Test
    fun detectsNaziSlogansCodesAndSymbols() {
        listOf("Зиг хайль!", "Sieg Heil", "хайль, гитлер", "1488", "14/88", "14 88", "卐", "white power")
            .forEach { assertTrue(it, detector.containsExtremism(it)) }
    }

    @Test
    fun historicalTextPasses() {
        listOf(
            "Гитлер напал на СССР 22 июня 1941 года",
            "Воевал против фашистов, освобождал Минск от войск СС",
            "Родился в 1914, умер в 1988",
            "Дом 14, 88 лет",
            "Зигзаг траншеи"
        ).forEach { assertFalse(it, detector.containsExtremism(it)) }
    }
}
