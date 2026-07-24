package com.example

import com.example.entitlements.UnlockCodeHasher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class UnlockCodeHasherTest {
    @Test
    fun `hash is lowercase hex and normalizes input`() {
        val a = UnlockCodeHasher.sha256Hex("  Family-Code  ")
        val b = UnlockCodeHasher.sha256Hex("family-code")
        assertEquals(a, b)
        assertEquals(64, a.length)
        assertEquals(a, a.lowercase())
    }

    @Test
    fun `different codes differ`() {
        assertNotEquals(
            UnlockCodeHasher.sha256Hex("one"),
            UnlockCodeHasher.sha256Hex("two")
        )
    }
}
