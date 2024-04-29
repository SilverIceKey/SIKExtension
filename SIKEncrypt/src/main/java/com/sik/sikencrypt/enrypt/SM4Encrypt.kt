package com.sik.sikencrypt.enrypt

import com.sik.sikcore.data.ConvertUtils
import com.sik.sikcore.log.LogUtils
import com.sik.sikencrypt.EncryptException
import com.sik.sikencrypt.EncryptExceptionEnums
import com.sik.sikencrypt.EncryptMode
import com.sik.sikencrypt.EncryptPadding
import com.sik.sikencrypt.IEncrypt
import com.sik.sikencrypt.IEncryptConfig
import com.sik.sikencrypt.PaddingUtils
import java.nio.charset.Charset
import java.util.Arrays

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

    private val logger = LogUtils.getLogger(SM4Encrypt::class)

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
        initKey(iEncryptConfig.key())
    }

    /**
     * Init key
     * 初始化密钥
     * @param key
     */
    private external fun initKey(key: ByteArray)

    /**
     * Encrypt
     * 加密
     * @param dataBytes
     * @return
     */
    private fun encrypt(
        mode: String,
        padding: String,
        iv: ByteArray?, dataBytes: ByteArray
    ): ByteArray {
        return when (mode) {
            "ECB" -> {
                val paddingData =
                    if (padding == EncryptPadding.PKCS5Padding.padding) {
                        PaddingUtils.applyPKCS5Padding(dataBytes, SM4_BLOCK_SIZE)
                    } else {
                        dataBytes
                    }
                logger.i("待加密数据:${paddingData.contentToString()}")
                encryptECB(paddingData)
            }

            "CBC" -> {
                encryptCBC(iv, dataBytes)
            }

            else -> {
                throw EncryptException(EncryptExceptionEnums.MODE_NOT_SUPPORT)
            }
        }
    }

    /**
     * Decrypt
     * 解密
     * @param dataBytes
     * @return
     */
    private fun decrypt(
        mode: String,
        padding: String,
        iv: ByteArray?, dataBytes: ByteArray
    ): ByteArray {
        return when (mode) {
            "ECB" -> {
                val decryptData = decryptECB(dataBytes)
                logger.i("解密数据:${decryptData.contentToString()}")
                if (padding == EncryptPadding.PKCS5Padding.padding) {
                    PaddingUtils.removePKCS5Padding(decryptData)
                } else {
                    decryptData
                }
            }

            "CBC" -> {
                decryptCBC(iv, dataBytes)
            }

            else -> {
                throw EncryptException(EncryptExceptionEnums.MODE_NOT_SUPPORT)
            }
        }
    }

    /**
     * Encrypt
     * 加密ecb
     * @param dataBytes
     * @return
     */
    private external fun encryptECB(dataBytes: ByteArray): ByteArray

    /**
     * Decrypt
     * 解密 核心
     * @param dataBytes
     * @return
     */
    private external fun decryptECB(dataBytes: ByteArray): ByteArray

    /**
     * Encrypt
     * 加密ecb
     * @param dataBytes
     * @return
     */
    private external fun encryptCBC(iv: ByteArray?, dataBytes: ByteArray): ByteArray

    /**
     * Decrypt
     * 解密 核心
     * @param dataBytes
     * @return
     */
    private external fun decryptCBC(iv: ByteArray?, dataBytes: ByteArray): ByteArray

    @Throws(EncryptException::class)
    override fun encryptToHex(dataBytes: ByteArray): String {
        if (iEncryptConfig.padding() == EncryptPadding.NoPadding && dataBytes.size % SM4_BLOCK_SIZE != 0) {
            throw EncryptException(EncryptExceptionEnums.PADDING_NOT_SUPPORT_DATA_SIZE)
        }
        return ConvertUtils.bytesToHex(
            encrypt(
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
            iEncryptConfig.mode().mode,
            iEncryptConfig.padding().padding,
            iEncryptConfig.iv(),
            dataBytes
        )
    }

    @Throws(EncryptException::class)
    override fun decryptFromHex(dataStr: String): String {
        return decrypt(
            iEncryptConfig.mode().mode,
            iEncryptConfig.padding().padding,
            iEncryptConfig.iv(),
            ConvertUtils.hexToBytes(dataStr)
        ).toString(Charset.defaultCharset())
    }

    @Throws(EncryptException::class)
    override fun decryptFromBase64(dataStr: String): String {
        return decrypt(
            iEncryptConfig.mode().mode,
            iEncryptConfig.padding().padding,
            iEncryptConfig.iv(),
            ConvertUtils.base64StringToBytes(dataStr)
        ).toString(Charset.defaultCharset())

    }

    @Throws(EncryptException::class)
    override fun decryptFromByteArray(dataBytes: ByteArray): ByteArray {
        return decrypt(
            iEncryptConfig.mode().mode,
            iEncryptConfig.padding().padding,
            iEncryptConfig.iv(),
            dataBytes
        )
    }
}