package com.sik.sikencrypt.enrypt

import com.sik.sikcore.data.ConvertUtils
import com.sik.sikcore.extension.file
import com.sik.sikencrypt.EncryptException
import com.sik.sikencrypt.EncryptExceptionEnums
import com.sik.sikencrypt.EncryptMode
import com.sik.sikencrypt.EncryptPadding
import com.sik.sikencrypt.IEncrypt
import com.sik.sikencrypt.IEncryptConfig
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.*
import java.nio.charset.Charset
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * AES加解密
 *
 */
class AESEncrypt(private val iEncryptConfig: IEncryptConfig) : IEncrypt {
    companion object {
        /**
         * 块大小
         */
        private const val BLOCK_SIZE = 16
    }

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
    }

    @Throws(EncryptException::class)
    override fun encryptToHex(dataBytes: ByteArray): String {
        if (iEncryptConfig.padding() == EncryptPadding.NoPadding && dataBytes.size % BLOCK_SIZE != 0) {
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
        if (iEncryptConfig.padding() == EncryptPadding.NoPadding && dataBytes.size % BLOCK_SIZE != 0) {
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
        if (iEncryptConfig.padding() == EncryptPadding.NoPadding && dataBytes.size % BLOCK_SIZE != 0) {
            throw EncryptException(EncryptExceptionEnums.PADDING_NOT_SUPPORT_DATA_SIZE)
        }
        return encrypt(
            iEncryptConfig.mode().mode,
            iEncryptConfig.padding().padding,
            iEncryptConfig.iv(), dataBytes
        )
    }

    @Throws(EncryptException::class)
    override fun encryptFile(srcFile: String, destFile: String) {
        val inputFile = File(srcFile)
        if (!inputFile.exists()) {
            throw EncryptException(EncryptExceptionEnums.FILE_NOT_FOUND)
        }
        val outputFile = File(destFile)
        val cipher = Cipher.getInstance("${iEncryptConfig.algorithm().name}/${iEncryptConfig.mode().mode}/${iEncryptConfig.padding().padding}", BouncyCastleProvider.PROVIDER_NAME)
        val keySpec = SecretKeySpec(iEncryptConfig.key(), iEncryptConfig.algorithm().name)
        if (iEncryptConfig.iv() != null && iEncryptConfig.mode() != EncryptMode.ECB) {
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, IvParameterSpec(iEncryptConfig.iv()))
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)
        }

        FileInputStream(inputFile).use { fis ->
            FileOutputStream(outputFile).use { fos ->
                CipherOutputStream(fos, cipher).use { cos ->
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while (fis.read(buffer).also { bytesRead = it } != -1) {
                        cos.write(buffer, 0, bytesRead)
                    }
                }
            }
        }
    }

    @Throws(EncryptException::class)
    override fun decryptFromHex(dataStr: String): String {
        if (iEncryptConfig.padding() == EncryptPadding.NoPadding && ConvertUtils.hexToBytes(
                dataStr
            ).size % BLOCK_SIZE != 0
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
            ).size % BLOCK_SIZE != 0
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
        if (iEncryptConfig.padding() == EncryptPadding.NoPadding && dataBytes.size % BLOCK_SIZE != 0
        ) {
            throw EncryptException(EncryptExceptionEnums.PADDING_NOT_SUPPORT_DATA_SIZE)
        }
        return decrypt(
            iEncryptConfig.mode().mode,
            iEncryptConfig.padding().padding,
            iEncryptConfig.iv(), dataBytes
        )
    }

    @Throws(EncryptException::class)
    override fun decryptFromFile(srcFile: String, destFile: String) {
        val inputFile = File(srcFile)
        if (!inputFile.exists()) {
            throw EncryptException(EncryptExceptionEnums.FILE_NOT_FOUND)
        }
        val outputFile = File(destFile)
        val cipher = Cipher.getInstance("${iEncryptConfig.algorithm().name}/${iEncryptConfig.mode().mode}/${iEncryptConfig.padding().padding}", BouncyCastleProvider.PROVIDER_NAME)
        val keySpec = SecretKeySpec(iEncryptConfig.key(), iEncryptConfig.algorithm().name)
        if (iEncryptConfig.iv() != null && iEncryptConfig.mode() != EncryptMode.ECB) {
            cipher.init(Cipher.DECRYPT_MODE, keySpec, IvParameterSpec(iEncryptConfig.iv()))
        } else {
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
        }

        FileInputStream(inputFile).use { fis ->
            FileOutputStream(outputFile).use { fos ->
                CipherInputStream(fis, cipher).use { cis ->
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while (cis.read(buffer).also { bytesRead = it } != -1) {
                        fos.write(buffer, 0, bytesRead)
                    }
                }
            }
        }
    }

    private fun encrypt(
        mode: String,
        padding: String,
        iv: ByteArray?,
        dataBytes: ByteArray
    ): ByteArray {
        val cipher = Cipher.getInstance(
            "${iEncryptConfig.algorithm().name}/$mode/$padding",
            BouncyCastleProvider.PROVIDER_NAME
        )
        val keySpec = SecretKeySpec(iEncryptConfig.key(), iEncryptConfig.algorithm().name)
        if (iv != null && iEncryptConfig.mode() != EncryptMode.ECB) {
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, IvParameterSpec(iv))
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)
        }
        val encryptedData = cipher.doFinal(dataBytes)
        return if (iv != null && iEncryptConfig.composeIV && iEncryptConfig.mode() != EncryptMode.ECB) {
            iv + encryptedData
        } else {
            encryptedData
        }
    }

    private fun decrypt(
        mode: String,
        padding: String,
        iv: ByteArray?,
        dataBytes: ByteArray
    ): ByteArray {
        val cipher = Cipher.getInstance(
            "${iEncryptConfig.algorithm().name}/$mode/$padding",
            BouncyCastleProvider.PROVIDER_NAME
        )
        val keySpec = SecretKeySpec(iEncryptConfig.key(), iEncryptConfig.algorithm().name)
        if (iv != null && iEncryptConfig.composeIV && iEncryptConfig.mode() != EncryptMode.ECB) {
            val actualIv = dataBytes.copyOfRange(0, iv.size)
            val actualData = dataBytes.copyOfRange(iv.size, dataBytes.size)
            cipher.init(Cipher.DECRYPT_MODE, keySpec, IvParameterSpec(actualIv))
            return cipher.doFinal(actualData)
        } else if (iv != null && iEncryptConfig.mode() != EncryptMode.ECB) {
            cipher.init(Cipher.DECRYPT_MODE, keySpec, IvParameterSpec(iv))
            return cipher.doFinal(dataBytes)
        } else {
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            return cipher.doFinal(dataBytes)
        }
    }
}
