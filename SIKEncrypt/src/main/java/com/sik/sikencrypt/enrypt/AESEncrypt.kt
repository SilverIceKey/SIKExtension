package com.sik.sikencrypt.enrypt

import com.sik.sikcore.data.ConvertUtils
import com.sik.sikencrypt.EncryptException
import com.sik.sikencrypt.EncryptExceptionEnums
import com.sik.sikencrypt.EncryptMode
import com.sik.sikencrypt.EncryptPadding
import com.sik.sikencrypt.IEncrypt
import com.sik.sikencrypt.IEncryptConfig
import java.nio.charset.Charset

/**
 * AES加解密
 *
 */
class AESEncrypt(private val iEncryptConfig: IEncryptConfig) : IEncrypt {
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
        initAES(iEncryptConfig.key())
    }

    /**
     * Init aes
     * 初始化AES
     */
    private external fun initAES(key: ByteArray)

    /**
     * Encrypt
     * 加密
     * @param dataBytes
     * @return
     */
    private external fun encrypt(
        mode: String,
        padding: String,
        iv: ByteArray?,
        dataBytes: ByteArray
    ): ByteArray

    /**
     * Decrypt
     * 解密
     * @param dataBytes
     * @return
     */
    private external fun decrypt(
        mode: String,
        padding: String,
        iv: ByteArray?,
        dataBytes: ByteArray
    ): ByteArray

    @Throws(EncryptException::class)
    override fun encryptToHex(dataBytes: ByteArray): String {
        if (iEncryptConfig.padding() == EncryptPadding.NoPadding && dataBytes.size % 16 != 0) {
            throw EncryptException(EncryptExceptionEnums.PADDING_NOT_SUPPORT_DATA_SIZE)
        }
        return ConvertUtils.bytesToHex(
            encrypt(
                iEncryptConfig.mode().mode,
                iEncryptConfig.padding().padding,
                iEncryptConfig.iv(), dataBytes
            )
        )
    }

    @Throws(EncryptException::class)
    override fun encryptToBase64(dataBytes: ByteArray): String {
        if (iEncryptConfig.padding() == EncryptPadding.NoPadding && dataBytes.size % 16 != 0) {
            throw EncryptException(EncryptExceptionEnums.PADDING_NOT_SUPPORT_DATA_SIZE)
        }
        return ConvertUtils.bytesToBase64String(
            encrypt(
                iEncryptConfig.mode().mode,
                iEncryptConfig.padding().padding,
                iEncryptConfig.iv(), dataBytes
            )
        )
    }

    @Throws(EncryptException::class)
    override fun encryptToByteArray(dataBytes: ByteArray): ByteArray {
        if (iEncryptConfig.padding() == EncryptPadding.NoPadding && dataBytes.size % 16 != 0) {
            throw EncryptException(EncryptExceptionEnums.PADDING_NOT_SUPPORT_DATA_SIZE)
        }
        return encrypt(
            iEncryptConfig.mode().mode,
            iEncryptConfig.padding().padding,
            iEncryptConfig.iv(), dataBytes
        )
    }

    @Throws(EncryptException::class)
    override fun decryptFromHex(dataStr: String): String {
        if (iEncryptConfig.padding() == EncryptPadding.NoPadding && ConvertUtils.hexToBytes(
                dataStr
            ).size % 16 != 0
        ) {
            throw EncryptException(EncryptExceptionEnums.PADDING_NOT_SUPPORT_DATA_SIZE)
        }
        return decrypt(
            iEncryptConfig.mode().mode,
            iEncryptConfig.padding().padding,
            iEncryptConfig.iv(), ConvertUtils.hexToBytes(dataStr)
        ).toString(Charset.defaultCharset())
    }

    @Throws(EncryptException::class)
    override fun decryptFromBase64(dataStr: String): String {
        if (iEncryptConfig.padding() == EncryptPadding.NoPadding && ConvertUtils.base64StringToBytes(
                dataStr
            ).size % 16 != 0
        ) {
            throw EncryptException(EncryptExceptionEnums.PADDING_NOT_SUPPORT_DATA_SIZE)
        }
        return decrypt(
            iEncryptConfig.mode().mode,
            iEncryptConfig.padding().padding,
            iEncryptConfig.iv(), ConvertUtils.base64StringToBytes(dataStr)
        ).toString(Charset.defaultCharset())
    }

    @Throws(EncryptException::class)
    override fun decryptFromByteArray(dataBytes: ByteArray): ByteArray {
        if (iEncryptConfig.padding() == EncryptPadding.NoPadding && dataBytes.size % 16 != 0
        ) {
            throw EncryptException(EncryptExceptionEnums.PADDING_NOT_SUPPORT_DATA_SIZE)
        }
        return decrypt(
            iEncryptConfig.mode().mode,
            iEncryptConfig.padding().padding,
            iEncryptConfig.iv(), dataBytes
        )
    }
}