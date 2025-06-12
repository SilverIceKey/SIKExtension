package com.sik.sikcore.string

import org.junit.Assert.*
import org.junit.Test

class StringUtilsTest {
    @Test
    fun isBase64_valid() {
        val valid = "SGVsbG8gd29ybGQ="
        assertTrue(StringUtils.isBase64(valid))
    }

    @Test
    fun isBase64_invalid() {
        val invalid = "not base64*"
        assertFalse(StringUtils.isBase64(invalid))
    }

    @Test
    fun isHex_valid() {
        val hex = "0A1B2C3D4E"
        assertTrue(StringUtils.isHex(hex))
    }

    @Test
    fun isHex_invalid() {
        val invalid = "XYZ123"
        assertFalse(StringUtils.isHex(invalid))
    }
}
