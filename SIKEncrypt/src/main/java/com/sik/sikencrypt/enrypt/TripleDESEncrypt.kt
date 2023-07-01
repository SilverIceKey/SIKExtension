package com.sik.sikencrypt.enrypt

import com.sik.sikcore.data.ConvertUtils
import com.sik.sikencrypt.EncryptException
import com.sik.sikencrypt.EncryptExceptionEnums
import com.sik.sikencrypt.EncryptMode
import com.sik.sikencrypt.IEncrypt
import com.sik.sikencrypt.IEncryptConfig
import java.nio.charset.Charset
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec

/**
 * TripleDES加解密
 *
 */
class TripleDESEncrypt(private val iEncryptConfig: IEncryptConfig) : IEncrypt {
    private var secretKey: SecretKey? = null

    init {
        if (iEncryptConfig.key().size != 24) {
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
    private fun initDES(key: ByteArray) {
        // 创建一个 DESKeySpec 对象，使用密钥
        val desKeySpec = DESKeySpec(key)
        // 创建一个密钥工厂
        val keyFactory = SecretKeyFactory.getInstance("DES")
        // 从密钥工厂中，根据 DESKeySpec 对象，生成一个 SecretKey 对象
        secretKey = keyFactory.generateSecret(desKeySpec)
    }

    @Throws(EncryptException::class)
    override fun encryptToHex(dataBytes: ByteArray): String {
        if (secretKey == null) {
            throw EncryptException(EncryptExceptionEnums.INIT_KEY_FIRST)
        }
        // 创建一个 Cipher 对象，并设置它的模式（加密或解密）和密钥
        val cipher =
            Cipher.getInstance("DESede/${iEncryptConfig.mode().mode}/${iEncryptConfig.padding().padding}")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val outputBytes = cipher.doFinal(dataBytes)
        // 将加密后的字节数组转换为 Base64 编码的字符串
        return ConvertUtils.bytesToHex(outputBytes)
    }

    @Throws(EncryptException::class)
    override fun encryptToBase64(dataBytes: ByteArray): String {
        // 创建一个 Cipher 对象，并设置它的模式（加密或解密）和密钥
        val cipher =
            Cipher.getInstance("DESede/${iEncryptConfig.mode().mode}/${iEncryptConfig.padding().padding}")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val outputBytes = cipher.doFinal(dataBytes)
        // 将加密后的字节数组转换为 Base64 编码的字符串
        return ConvertUtils.bytesToBase64String(outputBytes)
    }

    @Throws(EncryptException::class)
    override fun encryptToByteArray(dataBytes: ByteArray): ByteArray {
        if (secretKey == null) {
            throw EncryptException(EncryptExceptionEnums.INIT_KEY_FIRST)
        }
        // 创建一个 Cipher 对象，并设置它的模式（加密或解密）和密钥
        val cipher =
            Cipher.getInstance("DES/${iEncryptConfig.mode().mode}/${iEncryptConfig.padding().padding}")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        // 将加密后的字节数组转换为 Base64 编码的字符串
        return cipher.doFinal(dataBytes)
    }

    @Throws(EncryptException::class)
    override fun decryptFromHex(dataStr: String): String {
        if (secretKey == null) {
            throw EncryptException(EncryptExceptionEnums.INIT_KEY_FIRST)
        }
        // 创建一个 Cipher 对象，并设置它的模式（加密或解密）和密钥
        val cipher =
            Cipher.getInstance("DES/${iEncryptConfig.mode().mode}/${iEncryptConfig.padding().padding}")
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        val outputBytes = cipher.doFinal(ConvertUtils.hexToBytes(dataStr))
        return String(outputBytes, Charset.defaultCharset())
    }

    @Throws(EncryptException::class)
    override fun decryptFromBase64(dataStr: String): String {
        if (secretKey == null) {
            throw EncryptException(EncryptExceptionEnums.INIT_KEY_FIRST)
        }
        // 创建一个 Cipher 对象，并设置它的模式（加密或解密）和密钥
        val cipher =
            Cipher.getInstance("DES/${iEncryptConfig.mode().mode}/${iEncryptConfig.padding().padding}")
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        val outputBytes = cipher.doFinal(ConvertUtils.base64StringToBytes(dataStr))
        return String(outputBytes, Charset.defaultCharset())
    }

    @Throws(EncryptException::class)
    override fun decryptFromByteArray(dataBytes: ByteArray): ByteArray {
        if (secretKey == null) {
            throw EncryptException(EncryptExceptionEnums.INIT_KEY_FIRST)
        }
        // 创建一个 Cipher 对象，并设置它的模式（加密或解密）和密钥
        val cipher =
            Cipher.getInstance("DES/${iEncryptConfig.mode().mode}/${iEncryptConfig.padding().padding}")
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        return cipher.doFinal(dataBytes)
    }
}