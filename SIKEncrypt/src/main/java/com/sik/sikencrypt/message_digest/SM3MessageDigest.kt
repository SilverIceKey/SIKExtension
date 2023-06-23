package com.sik.sikencrypt.message_digest

import com.sik.sikcore.data.ConvertUtils
import com.sik.sikencrypt.IMessageDigest

/**
 * SM3信息摘要
 *
 */
class SM3MessageDigest:IMessageDigest {

    private external fun digest(dataBytes: ByteArray): ByteArray

    override fun digestToHex(dataBytes: ByteArray): String {
        return ConvertUtils.bytesToHex(digest(dataBytes))
    }

    override fun digestToBase64(dataBytes: ByteArray): String {
        return ConvertUtils.bytesToBase64String(digest(dataBytes))
    }
}