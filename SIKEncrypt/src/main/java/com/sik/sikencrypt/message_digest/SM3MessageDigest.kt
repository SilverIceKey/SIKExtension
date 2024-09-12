package com.sik.sikencrypt.message_digest

import com.sik.sikcore.data.ConvertUtils
import com.sik.sikcore.extension.file
import com.sik.sikencrypt.EncryptException
import com.sik.sikencrypt.EncryptExceptionEnums
import com.sik.sikencrypt.IMessageDigest
import com.sik.sikencrypt.MessageDigestFileOutType
import org.bouncycastle.crypto.digests.SM3Digest
import java.io.ByteArrayInputStream
import java.io.InputStream

/**
 * SM3信息摘要
 *
 */
class SM3MessageDigest : IMessageDigest {

    private fun digest(inputStream: InputStream): ByteArray {
        return SM3Digest().let {
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                it.update(buffer, 0, bytesRead)
            }
            val hash = ByteArray(it.digestSize)
            it.doFinal(hash, 0)
            hash
        }
    }

    override fun digestToHex(dataBytes: ByteArray): String {
        return ConvertUtils.bytesToHex(digest(ByteArrayInputStream(dataBytes)))
    }

    override fun digestToBase64(dataBytes: ByteArray): String {
        return ConvertUtils.bytesToBase64String(digest(ByteArrayInputStream(dataBytes)))
    }

    override fun digestFile(srcFile: String, outType: MessageDigestFileOutType): String {
        val tempSrcFile = srcFile.file()
        if (!tempSrcFile.exists()) {
            throw EncryptException(EncryptExceptionEnums.FILE_NOT_FOUND)
        } else {
            return when (outType) {
                MessageDigestFileOutType.HEX -> {
                    ConvertUtils.bytesToHex(digest(tempSrcFile.inputStream()))
                }

                MessageDigestFileOutType.BASE64 -> {
                    ConvertUtils.bytesToBase64String(digest(tempSrcFile.inputStream()))
                }
            }
        }
    }
}