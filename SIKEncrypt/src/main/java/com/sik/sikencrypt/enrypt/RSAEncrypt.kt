package com.sik.sikencrypt.enrypt

import com.sik.sikcore.data.ConvertUtils
import com.sik.sikcore.extension.createNewFile
import com.sik.sikcore.extension.file
import com.sik.sikcore.io.IOUtils
import com.sik.sikencrypt.EncryptException
import com.sik.sikencrypt.EncryptExceptionEnums
import com.sik.sikencrypt.EncryptPadding
import com.sik.sikencrypt.EncryptProgressImpl
import com.sik.sikencrypt.IEncryptProgressListener
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
    private val cipher: Cipher =
        Cipher.getInstance("${config.algorithm()}/${config.mode()}/${config.padding()}")
    private var publicKey: ByteArray = byteArrayOf()
    private var privateKey: ByteArray = byteArrayOf()

    /**
     * 加解密进度监听器
     */
    private var encryptProgressListener: IEncryptProgressListener = EncryptProgressImpl()

    override fun generateKeyPair(): IRSAEncrypt {
        if (config.publicKey().size !in arrayOf(1024, 2048, 4096)) {
            throw EncryptException(EncryptExceptionEnums.PRIVATE_KEY_SIZE_SET_ERROR)
        }
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(config.privateKeySize())
        val keyPair = keyPairGenerator.genKeyPair()
        publicKey = keyPair.public.encoded
        privateKey = keyPair.private.encoded
        return this
    }

    private fun getPublicKey(): PublicKey {
        when {
            (config.publicKey().isEmpty() && config.privateKey()
                .isEmpty()) && publicKey.isEmpty() -> {
                generateKeyPair()
            }

            config.publicKey().isEmpty() -> {
                throw EncryptException(EncryptExceptionEnums.PUBLIC_KEY_NOT_SET)
            }

            config.privateKey().size != config.privateKeySize() -> {
                throw EncryptException(EncryptExceptionEnums.PRIVATE_KEY_SIZE_ERROR)
            }
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
        val data = IOUtils.readFileAsString(srcFile.file())
        val maxBlockSize = getMaxEncryptBlockSize()
        if (data.length > maxBlockSize) {
            throw EncryptException(EncryptExceptionEnums.ENCRYPT_BLOCK_SIZE_EXCEED)
        }
        val encryptedData = encrypt(data.toByteArray())
        destFile.createNewFile()
        IOUtils.writeBytesToFile(destFile.file(), encryptedData)
    }

    override fun encryptSelfFile(srcFile: String) {
        val data = IOUtils.readFileAsString(srcFile.file())
        val maxBlockSize = getMaxEncryptBlockSize()
        if (data.length > maxBlockSize) {
            throw EncryptException(EncryptExceptionEnums.ENCRYPT_BLOCK_SIZE_EXCEED)
        }
        val encryptedData = encrypt(data.toByteArray())
        IOUtils.writeBytesToFile(srcFile.file(), encryptedData)
    }

    override fun encryptStream(inputStream: InputStream, outputStream: OutputStream) {
        val maxBlockSize = getMaxEncryptBlockSize()
        val buffer = ByteArray(maxBlockSize)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            if (bytesRead > maxBlockSize) {
                throw EncryptException(EncryptExceptionEnums.ENCRYPT_BLOCK_SIZE_EXCEED)
            }
            val encryptedBlock = encrypt(buffer.copyOf(bytesRead))
            outputStream.write(encryptedBlock)
        }
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
        val data = IOUtils.readFileAsString(srcFile.file())
        val maxBlockSize = getMaxDecryptBlockSize()
        if (data.length > maxBlockSize) {
            throw EncryptException(EncryptExceptionEnums.DECRYPT_BLOCK_SIZE_EXCEED)
        }
        val decryptedData = decrypt(data.toByteArray())
        destFile.createNewFile()
        IOUtils.writeBytesToFile(destFile.file(), decryptedData)
    }

    override fun decryptSelfFile(srcFile: String) {
        val data = IOUtils.readFileAsString(srcFile.file())
        val maxBlockSize = getMaxDecryptBlockSize()
        if (data.length > maxBlockSize) {
            throw EncryptException(EncryptExceptionEnums.DECRYPT_BLOCK_SIZE_EXCEED)
        }
        val decryptedData = decrypt(data.toByteArray())
        IOUtils.writeBytesToFile(srcFile.file(), decryptedData)
    }

    override fun decryptStream(inputStream: InputStream, outputStream: OutputStream) {
        val maxBlockSize = getMaxDecryptBlockSize()
        val buffer = ByteArray(maxBlockSize)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            if (bytesRead > maxBlockSize) {
                throw EncryptException(EncryptExceptionEnums.DECRYPT_BLOCK_SIZE_EXCEED)
            }
            val decryptedBlock = decrypt(buffer.copyOf(bytesRead))
            outputStream.write(decryptedBlock)
        }
    }
    
    private fun encrypt(dataBytes: ByteArray): ByteArray {
        cipher.init(Cipher.ENCRYPT_MODE, getPublicKey())
        return cipher.doFinal(dataBytes)
    }

    private fun decrypt(dataBytes: ByteArray): ByteArray {
        cipher.init(Cipher.DECRYPT_MODE, getPrivateKey())
        return cipher.doFinal(dataBytes)
    }

    /**
     * 根据填充获取最大加密的块长度
     */
    private fun getMaxEncryptBlockSize(): Int {
        val keySizeInBytes = config.privateKeySize() / 8
        return when (config.padding()) {
            EncryptPadding.NoPadding -> keySizeInBytes
            EncryptPadding.PKCS5Padding -> keySizeInBytes - 11
            EncryptPadding.OAEPWithSHA256AndMGF1Padding -> keySizeInBytes - 42
        }
    }

    /**
     * 获取最大解密的块长度
     */
    private fun getMaxDecryptBlockSize(): Int {
        return config.privateKeySize() / 8
    }

    override fun addProgressListener(iEncryptProgressListener: IEncryptProgressListener) {
        this.encryptProgressListener = iEncryptProgressListener
    }
}
