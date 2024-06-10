package com.sik.sikencrypt.message_digest

import com.sik.sikcore.data.ConvertUtils
import com.sik.sikencrypt.IMessageDigest
import org.bouncycastle.crypto.digests.SM3Digest

/**
 * SM3信息摘要
 *
 */
class SM3MessageDigest : IMessageDigest {
    companion object {
        /**
         * 加密类型
         */
        private const val algorithm = "SM3"
    }


    private fun digest(dataBytes: ByteArray): ByteArray {
        return SM3Digest().let {
            it.update(dataBytes, 0, dataBytes.size)
            val hash = ByteArray(it.digestSize)
            it.doFinal(hash, 0)
            hash
        }
    }

    override fun digestToHex(dataBytes: ByteArray): String {
        return ConvertUtils.bytesToHex(digest(dataBytes))
    }

    override fun digestToBase64(dataBytes: ByteArray): String {
        return ConvertUtils.bytesToBase64String(digest(dataBytes))
    }
}