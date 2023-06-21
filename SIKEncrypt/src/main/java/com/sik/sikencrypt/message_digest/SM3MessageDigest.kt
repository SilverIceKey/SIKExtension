package com.sik.sikencrypt.message_digest

import com.sik.sikencrypt.IMessageDigest

/**
 * SM3信息摘要
 *
 */
class SM3MessageDigest:IMessageDigest {

    override fun digestToHex(dataBytes: ByteArray): String {
        return ""
    }

    override fun digestToBase64(dataBytes: ByteArray): String {
        return ""
    }
}