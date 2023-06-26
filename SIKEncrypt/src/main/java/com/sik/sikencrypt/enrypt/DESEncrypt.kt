package com.sik.sikencrypt.enrypt

import com.sik.sikencrypt.EncryptException
import com.sik.sikencrypt.EncryptExceptionEnums
import com.sik.sikencrypt.EncryptMode
import com.sik.sikencrypt.IEncrypt
import com.sik.sikencrypt.IEncryptConfig
import kotlin.jvm.Throws

/**
 * DES加解密
 *
 */
class DESEncrypt(val iEncryptConfig: IEncryptConfig):IEncrypt {
    init {
        if (iEncryptConfig.key().size != 16 && iEncryptConfig.key().size != 24 && iEncryptConfig.key().size != 32) {
            throw EncryptException(EncryptExceptionEnums.KEY_SIZE_ERROR)
        }
        if (iEncryptConfig.mode() != EncryptMode.ECB && iEncryptConfig.iv() == null) {
            throw EncryptException(EncryptExceptionEnums.NO_IV)
        }
        if (iEncryptConfig.mode() == EncryptMode.GCM || iEncryptConfig.mode() == EncryptMode.CTR) {
            throw EncryptException(EncryptExceptionEnums.MODE_NOT_SUPPORT)
        }
        initDES(iEncryptConfig.key())
    }

    /**
     * Init DES
     * 初始化DES
     * @param key
     */
    private external fun initDES(key: ByteArray)

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