package com.sik.sikcore.color

import android.graphics.Color
import androidx.annotation.ColorInt

/**
 * Color utils
 * 颜色工具类
 * @constructor Create empty Color utils
 */
object ColorUtils {
    /**
     * Color int to hex
     * colorInt转16进制
     * @param colorInt
     * @return
     */
    fun colorIntToHex(@ColorInt colorInt: Int): String {
        val r = Color.red(colorInt)
        val g = Color.green(colorInt)
        val b = Color.blue(colorInt)
        return String.format("#%02X%02X%02X", r, g, b)
    }
}