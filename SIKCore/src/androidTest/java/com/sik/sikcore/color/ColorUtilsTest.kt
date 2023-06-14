package com.sik.sikcore.color

import org.junit.Test


internal class ColorUtilsTest {

    @Test
    fun colorIntToHex() {
        assert("#FFFFFF".equals(ColorUtils.colorIntToHex(-1)))
    }
}