package com.sik.sikencrypt.message_digest

import com.sik.sikcore.data.ConvertUtils
import com.sik.sikencrypt.IMessageDigest
import java.security.MessageDigest


/**
 * MD5信息摘要
 *
 */
class MD5MessageDigest : IMessageDigest {
    companion object {
        /**
         * 加密类型
         */
        private const val algorithm = "MD5"
    }


    private fun digest(dataBytes: ByteArray): ByteArray {
        return MessageDigest.getInstance(algorithm).digest(dataBytes)
    }

    override fun digestToHex(dataBytes: ByteArray): String {
        return ConvertUtils.bytesToHex(digest(dataBytes))
    }

    override fun digestToBase64(dataBytes: ByteArray): String {
        return ConvertUtils.bytesToBase64String(digest(dataBytes))
    }
}