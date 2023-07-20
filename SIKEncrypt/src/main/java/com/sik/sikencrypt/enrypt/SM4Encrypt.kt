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
 * SM4加解密
 *
 */
class SM4Encrypt(private val iEncryptConfig: IEncryptConfig) : IEncrypt {
    companion object {
        /**
         * Sm4 Block Size
         * SM4块大小
         */
        const val SM4_BLOCK_SIZE = 16
    }

    init {
        if (iEncryptConfig.key().size < 16) {
            throw EncryptException(EncryptExceptionEnums.KEY_SIZE_ERROR)
        }
        if (iEncryptConfig.mode() != EncryptMode.ECB && iEncryptConfig.iv() == null) {
            throw EncryptException(EncryptExceptionEnums.NO_IV)
        }
        if (iEncryptConfig.mode() == EncryptMode.GCM) {
            throw EncryptException(EncryptExceptionEnums.MODE_NOT_SUPPORT)
        }
    }

    /**
     * Encrypt
     * 加密
     * @param dataBytes
     * @return
     */
    private external fun encrypt(
        key: ByteArray,
        mode: String,
        padding: String,
        iv: ByteArray?, dataBytes: ByteArray
    ): ByteArray

    /**
     * Decrypt
     * 解密
     * @param dataBytes
     * @return
     */
    private external fun decrypt(
        key: ByteArray,
        mode: String,
        padding: String,
        iv: ByteArray?, dataBytes: ByteArray
    ): ByteArray

    @Throws(EncryptException::class)
    override fun encryptToHex(dataBytes: ByteArray): String {
        if (iEncryptConfig.padding() == EncryptPadding.NoPadding && dataBytes.size % SM4_BLOCK_SIZE != 0) {
            throw EncryptException(EncryptExceptionEnums.PADDING_NOT_SUPPORT_DATA_SIZE)
        }
        return ConvertUtils.bytesToHex(
            encrypt(
                iEncryptConfig.key(),
                iEncryptConfig.mode().mode,
                iEncryptConfig.padding().padding,
                iEncryptConfig.iv(),
                dataBytes
            )
        )
    }

    @Throws(EncryptException::class)
    override fun encryptToBase64(dataBytes: ByteArray): String {
        if (iEncryptConfig.padding() == EncryptPadding.NoPadding && dataBytes.size % SM4_BLOCK_SIZE != 0) {
            throw EncryptException(EncryptExceptionEnums.PADDING_NOT_SUPPORT_DATA_SIZE)
        }
        return ConvertUtils.bytesToBase64String(
            encrypt(
                iEncryptConfig.key(),
                iEncryptConfig.mode().mode,
                iEncryptConfig.padding().padding,
                iEncryptConfig.iv(),
                dataBytes
            )
        )
    }

    @Throws(EncryptException::class)
    override fun encryptToByteArray(dataBytes: ByteArray): ByteArray {
        if (iEncryptConfig.padding() == EncryptPadding.NoPadding && dataBytes.size % SM4_BLOCK_SIZE != 0) {
            throw EncryptException(EncryptExceptionEnums.PADDING_NOT_SUPPORT_DATA_SIZE)
        }
        return encrypt(
            iEncryptConfig.key(),
            iEncryptConfig.mode().mode,
            iEncryptConfig.padding().padding,
            iEncryptConfig.iv(),
            dataBytes
        )
    }

    @Throws(EncryptException::class)
    override fun decryptFromHex(dataStr: String): String {
        return decrypt(
            iEncryptConfig.key(),
            iEncryptConfig.mode().mode,
            iEncryptConfig.padding().padding,
            iEncryptConfig.iv(),
            ConvertUtils.hexToBytes(dataStr)
        ).toString(Charset.defaultCharset())
    }

    @Throws(EncryptException::class)
    override fun decryptFromBase64(dataStr: String): String {
        return decrypt(
            iEncryptConfig.key(),
            iEncryptConfig.mode().mode,
            iEncryptConfig.padding().padding,
            iEncryptConfig.iv(),
            ConvertUtils.base64StringToBytes(dataStr)
        ).toString(Charset.defaultCharset())

    }

    @Throws(EncryptException::class)
    override fun decryptFromByteArray(dataBytes: ByteArray): ByteArray {
        return decrypt(
            iEncryptConfig.key(),
            iEncryptConfig.mode().mode,
            iEncryptConfig.padding().padding,
            iEncryptConfig.iv(),
            dataBytes
        )
    }
}