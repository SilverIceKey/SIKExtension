package com.sik.sikencrypt.enrypt

import com.sik.sikcore.data.ConvertUtils
import com.sik.sikencrypt.EncryptException
import com.sik.sikencrypt.EncryptExceptionEnums
import com.sik.sikencrypt.IRSAEncrypt
import com.sik.sikencrypt.IRSAEncryptConfig
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher

/**
 * RSA加解密
 *
 */
class RSAEncrypt(private val config: IRSAEncryptConfig) : IRSAEncrypt {
    // Cipher实例，用于执行加密和解密操作
    private val cipher: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
    private var publicKey: ByteArray = byteArrayOf()
    private var privateKey: ByteArray = byteArrayOf()

    /**
     * 生成公钥和私钥
     *
     * @return
     */
    override fun generateKeyPair(): IRSAEncrypt {
        // 创建KeyPairGenerator对象
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        // 初始化KeyPairGenerator对象
        keyPairGenerator.initialize(2048)
        // 生成KeyPair
        val keyPair = keyPairGenerator.genKeyPair()
        // 获取公钥和私钥的字节数组形式
        publicKey = keyPair.public.encoded
        privateKey = keyPair.private.encoded
        return this
    }

    private fun getPublicKey(): PublicKey {
        if ((config.publicKey().isEmpty() && config.privateKey()
                .isEmpty()) && publicKey.isEmpty()
        ) {
            generateKeyPair()
        } else if (config.publicKey().isEmpty()) {
            throw EncryptException(EncryptExceptionEnums.PUBLIC_KEY_NOT_SET)
        }
        val keySpec = X509EncodedKeySpec(publicKey)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(keySpec)
    }

    private fun getPrivateKey(): PrivateKey {
        if ((config.publicKey().isEmpty() && config.privateKey()
                .isEmpty()) && privateKey.isEmpty()
        ) {
            generateKeyPair()
        } else if (config.publicKey().isEmpty()) {
            throw EncryptException(EncryptExceptionEnums.PRIVATE_KEY_NOT_SET)
        }
        val keySpec = PKCS8EncodedKeySpec(privateKey)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePrivate(keySpec)
    }

    override fun getPublicKeyBytes(): ByteArray {
        if (config.publicKey().isEmpty() && publicKey.isEmpty()) {
            generateKeyPair()
        }
        return publicKey
    }

    override fun getPrivateKeyBytes(): ByteArray {
        if (config.privateKey().isEmpty() && privateKey.isEmpty()) {
            generateKeyPair()
        }
        return privateKey
    }

    @Throws(EncryptException::class)
    override fun encryptToHex(dataBytes: ByteArray): String {
        cipher.init(Cipher.ENCRYPT_MODE, getPublicKey())
        val encrypted = cipher.doFinal(dataBytes)
        return ConvertUtils.bytesToHex(encrypted)
    }

    @Throws(EncryptException::class)
    override fun encryptToBase64(dataBytes: ByteArray): String {
        cipher.init(Cipher.ENCRYPT_MODE, getPublicKey())
        val encrypted = cipher.doFinal(dataBytes)
        return ConvertUtils.bytesToBase64String(encrypted)
    }

    @Throws(EncryptException::class)
    override fun encryptToByteArray(dataBytes: ByteArray): ByteArray {
        cipher.init(Cipher.ENCRYPT_MODE, getPublicKey())
        return cipher.doFinal(dataBytes)
    }

    @Throws(EncryptException::class)
    override fun decryptFromHex(dataStr: String): String {
        val bytes = ConvertUtils.hexToBytes(dataStr)
        cipher.init(Cipher.DECRYPT_MODE, getPrivateKey())
        val decrypted = cipher.doFinal(bytes)
        return String(decrypted, Charsets.UTF_8)
    }

    @Throws(EncryptException::class)
    override fun decryptFromBase64(dataStr: String): String {
        val bytes = ConvertUtils.base64StringToBytes(dataStr)
        cipher.init(Cipher.DECRYPT_MODE, getPrivateKey())
        val decrypted = cipher.doFinal(bytes)
        return String(decrypted, Charsets.UTF_8)
    }

    @Throws(EncryptException::class)
    override fun decryptFromByteArray(dataBytes: ByteArray): ByteArray {
        cipher.init(Cipher.DECRYPT_MODE, getPrivateKey())
        return cipher.doFinal(dataBytes)
    }
}
