package com.gorman.ourmemoryapp.ui.tours.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TourProgressTest {

    @Test
    fun freshTourSelectsNothing() {
        assertNull(resumeStopIndex(stopCount = 4, visitedStops = emptySet()))
    }

    @Test
    fun resumesAtFirstUnvisitedStop() {
        assertEquals(2, resumeStopIndex(stopCount = 4, visitedStops = setOf(0, 1)))
        assertEquals(1, resumeStopIndex(stopCount = 4, visitedStops = setOf(0, 2)))
    }

    @Test
    fun finishedTourSelectsNothing() {
        assertNull(resumeStopIndex(stopCount = 2, visitedStops = setOf(0, 1)))
    }
}
