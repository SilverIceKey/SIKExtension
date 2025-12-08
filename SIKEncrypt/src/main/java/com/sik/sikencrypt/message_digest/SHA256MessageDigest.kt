package com.sik.sikencrypt.message_digest

import com.sik.sikcore.data.ConvertUtils
import com.sik.sikcore.extension.file
import com.sik.sikencrypt.EncryptException
import com.sik.sikencrypt.EncryptExceptionEnums
import com.sik.sikencrypt.IMessageDigest
import com.sik.sikencrypt.MessageDigestFileOutType
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.security.MessageDigest

/**
 * SHA256信息摘要
 *
 */
class SHA256MessageDigest : IMessageDigest {
    companion object {

        /**
         * 加密类型
         */
        private const val algorithm = "SHA256"
    }

    private fun digest(inputStream: InputStream): ByteArray {
        return MessageDigest.getInstance(algorithm).let {
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                it.update(buffer, 0, bytesRead)
            }
            it.digest()
        }
    }


    override fun digestToHex(dataBytes: ByteArray): String {
        return ConvertUtils.bytesToHex(digest(ByteArrayInputStream(dataBytes)))
    }

    override fun digestToBase64(dataBytes: ByteArray): String {
        return ConvertUtils.bytesToBase64String(digest(ByteArrayInputStream(dataBytes)))
    }

    override fun digest(dataBytes: ByteArray): ByteArray {
        return digest(ByteArrayInputStream(dataBytes))
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