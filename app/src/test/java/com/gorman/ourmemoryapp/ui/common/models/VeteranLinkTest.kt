package com.gorman.ourmemoryapp.ui.common.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VeteranLinkTest {

    @Test
    fun parsesVeteranIdFromOurLink() {
        assertEquals("10", VeteranLink.parseVeteranId("https://chatroom-85fb8.web.app/veteran/10"))
        assertEquals("10", VeteranLink.parseVeteranId(" https://chatroom-85fb8.web.app/veteran/10/?utm=qr "))
    }

    @Test
    fun rejectsForeignAndEmptyCodes() {
        assertNull(VeteranLink.parseVeteranId("https://example.com/veteran/10"))
        assertNull(VeteranLink.parseVeteranId("https://chatroom-85fb8.web.app/veteran/"))
        assertNull(VeteranLink.parseVeteranId(""))
        assertNull(VeteranLink.parseVeteranId("https://chatroom-85fb8.web.app/veteran/10/photos"))
    }
}
