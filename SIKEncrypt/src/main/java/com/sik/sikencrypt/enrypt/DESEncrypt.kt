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

    }

    @Throws(EncryptException::class)
    override fun encryptToBase64(dataBytes: ByteArray): String {

    }

    @Throws(EncryptException::class)
    override fun decryptFromHex(dataStr: String): String {

    }

    @Throws(EncryptException::class)
    override fun decryptFromBase64(dataStr: String): String {

    }
}