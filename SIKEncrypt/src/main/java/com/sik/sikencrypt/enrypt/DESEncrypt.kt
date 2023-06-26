package com.sik.sikencrypt.enrypt

import com.sik.sikencrypt.EncryptException
import com.sik.sikencrypt.IEncrypt
import com.sik.sikencrypt.IEncryptConfig
import kotlin.jvm.Throws

/**
 * DES加解密
 *
 */
class DESEncrypt(val iEncryptConfig: IEncryptConfig):IEncrypt {
    @Throws(EncryptException::class)
    override fun encryptToHex(dataBytes: ByteArray): String {
        return ""
    }

    @Throws(EncryptException::class)
    override fun encryptToBase64(dataBytes: ByteArray): String {
        return ""
    }

    @Throws(EncryptException::class)
    override fun encryptToByteArray(dataBytes: ByteArray): ByteArray {
        return ByteArray(0)
    }

    @Throws(EncryptException::class)
    override fun decryptFromHex(dataStr: String): String {
        return ""
    }

    @Throws(EncryptException::class)
    override fun decryptFromBase64(dataStr: String): String {
        return ""
    }

    @Throws(EncryptException::class)
    override fun decryptFromByteArray(dataBytes: ByteArray): ByteArray {
        return ByteArray(0)
    }
}