package com.gorman.ourmemoryapp.ui.admin.burials.models

import com.gorman.ourmemoryapp.domain.models.Burial
import com.gorman.ourmemoryapp.ui.common.models.BurialType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class BurialFormTest {

    @Test
    fun burialRoundTripsThroughForm() {
        val burial = Burial(
            id = "b_001",
            latitude = 53.90881,
            longitude = 27.58612,
            section = "1",
            row = "2",
            place = "4",
            type = "MASS_GRAVE"
        )

        val form = burial.toForm()

        assertEquals(BurialType.MASS_GRAVE, form.type)
        assertEquals("53.908810", form.latitude)
        assertEquals(burial, form.toBurial())
    }

    @Test
    fun coordinatesAcceptCommaAndRejectOutOfRange() {
        assertEquals(53.9, "53,9".toLatitudeOrNull())
        assertNull("91".toLatitudeOrNull())
        assertNull("abc".toLongitudeOrNull())
        assertFalse(BurialForm(latitude = "", longitude = "27.5").isValid)
    }
}
