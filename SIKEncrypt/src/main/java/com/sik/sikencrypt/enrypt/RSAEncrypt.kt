package com.sik.sikencrypt.enrypt

import com.sik.sikcore.data.ConvertUtils
import com.sik.sikencrypt.EncryptException
import com.sik.sikencrypt.EncryptExceptionEnums
import com.sik.sikencrypt.IRSAEncrypt
import com.sik.sikencrypt.IRSAEncryptConfig
import java.io.*
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

class RSAEncrypt(private val config: IRSAEncryptConfig) : IRSAEncrypt {
    private val cipher: Cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
    private var publicKey: ByteArray = byteArrayOf()
    private var privateKey: ByteArray = byteArrayOf()

    override fun generateKeyPair(): IRSAEncrypt {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        val keyPair = keyPairGenerator.genKeyPair()
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
        } else if (config.privateKey().isEmpty()) {
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
        return encrypt(dataBytes).let {
            ConvertUtils.bytesToHex(it)
        }
    }

    @Throws(EncryptException::class)
    override fun encryptToBase64(dataBytes: ByteArray): String {
        return encrypt(dataBytes).let {
            ConvertUtils.bytesToBase64String(it)
        }
    }

    @Throws(EncryptException::class)
    override fun encryptToByteArray(dataBytes: ByteArray): ByteArray {
        return encrypt(dataBytes)
    }

    @Throws(EncryptException::class)
    override fun encryptFile(srcFile: String, destFile: String) {
        throw EncryptException(EncryptExceptionEnums.FILE_ENCRYPT_NOT_SUPPORT)
    }

    override fun encryptSelfFile(srcFile: String) {
        throw EncryptException(EncryptExceptionEnums.FILE_ENCRYPT_NOT_SUPPORT)
    }

    @Throws(EncryptException::class)
    override fun decryptFromHex(dataStr: String): String {
        val bytes = ConvertUtils.hexToBytes(dataStr)
        return decrypt(bytes).let {
            String(it, Charsets.UTF_8)
        }
    }

    @Throws(EncryptException::class)
    override fun decryptFromBase64(dataStr: String): String {
        val bytes = ConvertUtils.base64StringToBytes(dataStr)
        return decrypt(bytes).let {
            String(it, Charsets.UTF_8)
        }
    }

    @Throws(EncryptException::class)
    override fun decryptFromByteArray(dataBytes: ByteArray): ByteArray {
        return decrypt(dataBytes)
    }

    @Throws(EncryptException::class)
    override fun decryptFromFile(srcFile: String, destFile: String) {
        throw EncryptException(EncryptExceptionEnums.FILE_ENCRYPT_NOT_SUPPORT)
    }

    override fun decryptSelfFile(srcFile: String) {
        throw EncryptException(EncryptExceptionEnums.FILE_ENCRYPT_NOT_SUPPORT)
    }

    private fun encrypt(dataBytes: ByteArray): ByteArray {
        cipher.init(Cipher.ENCRYPT_MODE, getPublicKey())
        return cipher.doFinal(dataBytes)
    }

    private fun decrypt(dataBytes: ByteArray): ByteArray {
        cipher.init(Cipher.DECRYPT_MODE, getPrivateKey())
        return cipher.doFinal(dataBytes)
    }
}
