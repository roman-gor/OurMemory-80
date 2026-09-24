package com.gorman.ourmemoryapp.data.submissions.datasource.local

import org.junit.Assert.assertEquals
import org.junit.Test

class ScaledSizeTest {

    @Test
    fun smallPhotoKeepsItsSize() {
        assertEquals(1200 to 900, scaledSize(1200, 900, 2048))
    }

    @Test
    fun landscapePhotoIsScaledByWidth() {
        assertEquals(2048 to 1536, scaledSize(4000, 3000, 2048))
    }

    @Test
    fun portraitPhotoIsScaledByHeight() {
        assertEquals(1152 to 2048, scaledSize(2250, 4000, 2048))
    }
}
