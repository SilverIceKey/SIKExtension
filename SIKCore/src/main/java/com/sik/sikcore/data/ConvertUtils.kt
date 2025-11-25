package com.sik.sikcore.data

import android.util.Base64

/**
 * 转换工具类
 */
object ConvertUtils {
    /**
     * byte数组转十六进制字符串
     *
     * @param byteArray
     * @return
     */
    @JvmStatic
    fun bytesToHex(byteArray: ByteArray): String {
        val sb = StringBuilder()
        for (b in byteArray) {
            sb.append(String.format("%02X", b))
        }
        return sb.toString()
    }

    /**
     * 十六进制字符串转数组
     *
     * @param hex
     * @return
     */
    @JvmStatic
    fun hexToBytes(hex: String): ByteArray {
        val length: Int = hex.length
        return ByteArray(length / 2).apply {
            var i = 0
            while (i < length) {
                this[i / 2] = ((Character.digit(hex[i], 16) shl 4)
                        + Character.digit(hex[i + 1], 16)).toByte()
                i += 2
            }
        }
    }

    /**
     * byte数组转base64
     *
     * @param byteArray
     * @return
     */
    @JvmStatic
    fun bytesToBase64String(byteArray: ByteArray): String {
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    /**
     * Base64字符串转byte数组
     *
     * @param base64Str
     * @return
     */
    @JvmStatic
    fun base64StringToBytes(base64Str:String):ByteArray{
        return Base64.decode(base64Str,Base64.NO_WRAP)
    }
}