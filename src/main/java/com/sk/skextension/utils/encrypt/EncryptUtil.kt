package com.sk.skextension.utils.encrypt

import android.util.Base64
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * 加密工具类
 */
object EncryptUtil {
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/ECB/PKCS5Padding"
    private const val KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATION_COUNT = 65536
    private const val KEY_LENGTH = 256

    /**
     * 加载so
     */
    init {
        System.loadLibrary("skextension")
    }

    /**
     * AES加密(AES_ECB_PKCS5)
     */
    external fun AESEncode(key: String, content: String): String

    /**
     * AES解密(AES_ECB_PKCS7)
     */
    external fun AESDecode(key: String, content: String): ByteArray

    /**
     * MD5加密
     */
    external fun MD5Encode(content: String): String

    /**
     * key生成
     */
    fun generateKey(uuid: String): SecretKey {
        val factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM)
        val salt = ByteArray(8).apply { SecureRandom().nextBytes(this) }
        val keySpec: KeySpec = PBEKeySpec(uuid.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH)
        val tmp = factory.generateSecret(keySpec)
        return SecretKeySpec(tmp.encoded, ALGORITHM)
    }

    /**
     * AES自带加密
     */
    fun encrypt(data: String, key: SecretKey): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encryptedData = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(encryptedData, Base64.DEFAULT)
    }

    /**
     * AES自带解密
     */
    fun decrypt(data: String, key: SecretKey): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key)
        val decodedData = Base64.decode(data, Base64.DEFAULT)
        val decryptedData = cipher.doFinal(decodedData)
        return String(decryptedData, Charsets.UTF_8)
    }
}