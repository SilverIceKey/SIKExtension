package com.sik.sikencrypt.enrypt

import com.sik.sikcore.data.ConvertUtils
import com.sik.sikcore.extension.createNewFile
import com.sik.sikcore.extension.deleteIfExists
import com.sik.sikencrypt.EncryptException
import com.sik.sikencrypt.EncryptExceptionEnums
import com.sik.sikencrypt.EncryptMode
import com.sik.sikencrypt.EncryptPadding
import com.sik.sikencrypt.EncryptProgressImpl
import com.sik.sikencrypt.IEncrypt
import com.sik.sikencrypt.IEncryptConfig
import com.sik.sikencrypt.IEncryptProgressListener
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.*
import java.nio.charset.Charset
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * DES加解密
 *
 */
class DESEncrypt(private val iEncryptConfig: IEncryptConfig) : IEncrypt {
    companion object {
        /**
         * 块大小
         */
        private const val BLOCK_SIZE = 8
    }

    /**
     * 加解密进度监听器
     */
    private var encryptProgressListener: IEncryptProgressListener? = null

    init {
        if (iEncryptConfig.key().size != 8) {
            throw EncryptException(EncryptExceptionEnums.KEY_SIZE_ERROR)
        }
        if (iEncryptConfig.mode() != EncryptMode.ECB && iEncryptConfig.iv() == null) {
            throw EncryptException(EncryptExceptionEnums.NO_IV)
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
        val cipher = try {
            Cipher.getInstance(
                "${iEncryptConfig.algorithm().name}/${iEncryptConfig.mode().mode}/${iEncryptConfig.padding().padding}",
                BouncyCastleProvider.PROVIDER_NAME
            )
        } catch (e: NoSuchAlgorithmException) {
            throw EncryptException(EncryptExceptionEnums.MODE_NOT_SUPPORT)
        }
        val keySpec = SecretKeySpec(iEncryptConfig.key(), iEncryptConfig.algorithm().name)
        val iv = iEncryptConfig.iv()
        if (iEncryptConfig.iv() != null && iEncryptConfig.mode() != EncryptMode.ECB) {
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, IvParameterSpec(iv))
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)
        }

        val encryptFileSize = inputFile.length()
        var currentEncryptSize = 0
        FileInputStream(inputFile).use { fis ->
            FileOutputStream(outputFile).use { fos ->
                if (iEncryptConfig.composeIV && iEncryptConfig.mode() != EncryptMode.ECB) {
                    fos.write(iv)
                }
                CipherOutputStream(fos, cipher).use { cos ->
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while (fis.read(buffer).also { bytesRead = it } != -1) {
                        cos.write(buffer, 0, bytesRead)
                        currentEncryptSize += bytesRead
                        encryptProgressListener?.encryptProgress((currentEncryptSize.toFloat() / encryptFileSize * 100).toInt())
                    }
                }
            }
        }
    }

    override fun encryptSelfFile(srcFile: String) {
        val inputFile = File(srcFile)
        val tempFile = "${srcFile}.tmp"
        if (!inputFile.exists()) {
            throw EncryptException(EncryptExceptionEnums.FILE_NOT_FOUND)
        }
        val outputFile = File(tempFile)
        tempFile.deleteIfExists()
        tempFile.createNewFile()
        val cipher = try {
            Cipher.getInstance(
                "${iEncryptConfig.algorithm().name}/${iEncryptConfig.mode().mode}/${iEncryptConfig.padding().padding}",
                BouncyCastleProvider.PROVIDER_NAME
            )
        } catch (e: NoSuchAlgorithmException) {
            throw EncryptException(EncryptExceptionEnums.MODE_NOT_SUPPORT)
        }
        val keySpec = SecretKeySpec(iEncryptConfig.key(), iEncryptConfig.algorithm().name)
        val iv = iEncryptConfig.iv()
        if (iEncryptConfig.iv() != null && iEncryptConfig.mode() != EncryptMode.ECB) {
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, IvParameterSpec(iv))
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)
        }

        val encryptFileSize = inputFile.length()
        var currentEncryptSize = 0
        FileInputStream(inputFile).use { fis ->
            FileOutputStream(outputFile).use { fos ->
                if (iEncryptConfig.composeIV && iEncryptConfig.mode() != EncryptMode.ECB) {
                    fos.write(iv)
                }
                CipherOutputStream(fos, cipher).use { cos ->
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while (fis.read(buffer).also { bytesRead = it } != -1) {
                        cos.write(buffer, 0, bytesRead)
                        currentEncryptSize += bytesRead
                        encryptProgressListener?.encryptProgress((currentEncryptSize.toFloat() / encryptFileSize * 100).toInt())
                    }
                }
            }
        }
        // 替换原文件
        if (inputFile.delete()) {
            outputFile.renameTo(inputFile)
        } else {
            throw EncryptException(EncryptExceptionEnums.ENCRYPT_ERROR)
        }
    }

    override fun encryptStream(inputStream: InputStream, outputStream: OutputStream) {
        val cipher = try {
            Cipher.getInstance(
                "${iEncryptConfig.algorithm().name}/${iEncryptConfig.mode().mode}/${iEncryptConfig.padding().padding}",
                BouncyCastleProvider.PROVIDER_NAME
            )
        } catch (e: NoSuchAlgorithmException) {
            throw EncryptException(EncryptExceptionEnums.MODE_NOT_SUPPORT)
        }
        val keySpec = SecretKeySpec(iEncryptConfig.key(), iEncryptConfig.algorithm().name)
        if (iEncryptConfig.iv() != null && iEncryptConfig.mode() != EncryptMode.ECB) {
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, IvParameterSpec(iEncryptConfig.iv()))
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)
        }

        var currentEncryptSize = 0
        inputStream.use { fis ->
            outputStream.use { fos ->
                if (iEncryptConfig.composeIV && iEncryptConfig.mode() != EncryptMode.ECB) {
                    fos.write(iEncryptConfig.iv())
                }
                CipherOutputStream(fos, cipher).use { cos ->
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while (fis.read(buffer).also { bytesRead = it } != -1) {
                        cos.write(buffer, 0, bytesRead)
                        currentEncryptSize += bytesRead
                        encryptProgressListener?.encryptBytes(currentEncryptSize)
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
        val cipher = try {
            Cipher.getInstance(
                "${iEncryptConfig.algorithm().name}/${iEncryptConfig.mode().mode}/${iEncryptConfig.padding().padding}",
                BouncyCastleProvider.PROVIDER_NAME
            )
        } catch (e: NoSuchAlgorithmException) {
            throw EncryptException(EncryptExceptionEnums.MODE_NOT_SUPPORT)
        }

        val decryptFileSize = inputFile.length()
        var currentDecryptSize = 0
        FileInputStream(inputFile).use { fis ->
            val keySpec = SecretKeySpec(iEncryptConfig.key(), iEncryptConfig.algorithm().name)
            if (iEncryptConfig.iv() != null && iEncryptConfig.mode() != EncryptMode.ECB) {
                if (iEncryptConfig.composeIV) {
                    val fileIv = ByteArray(16)
                    fis.read(fileIv, 0, 16)
                    cipher.init(Cipher.DECRYPT_MODE, keySpec, IvParameterSpec(fileIv))
                    encryptProgressListener?.decryptProgress((currentDecryptSize.toFloat() / decryptFileSize * 100).toInt())
                } else {
                    cipher.init(Cipher.DECRYPT_MODE, keySpec, IvParameterSpec(iEncryptConfig.iv()))
                    encryptProgressListener?.decryptProgress((currentDecryptSize.toFloat() / decryptFileSize * 100).toInt())
                }
            } else {
                cipher.init(Cipher.DECRYPT_MODE, keySpec)
            }
            FileOutputStream(outputFile).use { fos ->
                CipherInputStream(fis, cipher).use { cis ->
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while (cis.read(buffer).also { bytesRead = it } != -1) {
                        fos.write(buffer, 0, bytesRead)
                        currentDecryptSize += bytesRead
                        encryptProgressListener?.decryptProgress((currentDecryptSize.toFloat() / decryptFileSize * 100).toInt())
                    }
                }
            }
        }
    }

    override fun decryptSelfFile(srcFile: String) {
        val inputFile = File(srcFile)
        val tempFile = "${srcFile}.tmp"
        if (!inputFile.exists()) {
            throw EncryptException(EncryptExceptionEnums.FILE_NOT_FOUND)
        }
        val outputFile = File(tempFile)
        tempFile.deleteIfExists()
        tempFile.createNewFile()
        val cipher = try {
            Cipher.getInstance(
                "${iEncryptConfig.algorithm().name}/${iEncryptConfig.mode().mode}/${iEncryptConfig.padding().padding}",
                BouncyCastleProvider.PROVIDER_NAME
            )
        } catch (e: NoSuchAlgorithmException) {
            throw EncryptException(EncryptExceptionEnums.MODE_NOT_SUPPORT)
        }

        val decryptFileSize = inputFile.length()
        var currentDecryptSize = 0
        FileInputStream(inputFile).use { fis ->
            val keySpec = SecretKeySpec(iEncryptConfig.key(), iEncryptConfig.algorithm().name)
            if (iEncryptConfig.iv() != null && iEncryptConfig.mode() != EncryptMode.ECB) {
                if (iEncryptConfig.composeIV) {
                    val fileIv = ByteArray(16)
                    fis.read(fileIv, 0, 16)
                    cipher.init(Cipher.DECRYPT_MODE, keySpec, IvParameterSpec(fileIv))
                    currentDecryptSize = 16
                    encryptProgressListener?.decryptProgress((currentDecryptSize.toFloat() / decryptFileSize * 100).toInt())
                } else {
                    cipher.init(Cipher.DECRYPT_MODE, keySpec, IvParameterSpec(iEncryptConfig.iv()))
                    encryptProgressListener?.decryptProgress((currentDecryptSize.toFloat() / decryptFileSize * 100).toInt())
                }
            } else {
                cipher.init(Cipher.DECRYPT_MODE, keySpec)
            }
            FileOutputStream(outputFile).use { fos ->
                CipherInputStream(fis, cipher).use { cis ->
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while (cis.read(buffer).also { bytesRead = it } != -1) {
                        fos.write(buffer, 0, bytesRead)
                        currentDecryptSize += bytesRead
                        encryptProgressListener?.decryptProgress((currentDecryptSize.toFloat() / decryptFileSize * 100).toInt())
                    }
                }
            }
        }
        // 替换原文件
        if (inputFile.delete()) {
            outputFile.renameTo(inputFile)
        } else {
            throw EncryptException(EncryptExceptionEnums.ENCRYPT_ERROR)
        }
    }

    override fun decryptStream(inputStream: InputStream, outputStream: OutputStream) {
        val cipher = try {
            Cipher.getInstance(
                "${iEncryptConfig.algorithm().name}/${iEncryptConfig.mode().mode}/${iEncryptConfig.padding().padding}",
                BouncyCastleProvider.PROVIDER_NAME
            )
        } catch (e: NoSuchAlgorithmException) {
            throw EncryptException(EncryptExceptionEnums.MODE_NOT_SUPPORT)
        }

        var currentDecryptBytes = 0
        inputStream.use { fis ->
            val keySpec = SecretKeySpec(iEncryptConfig.key(), iEncryptConfig.algorithm().name)
            if (iEncryptConfig.iv() != null && iEncryptConfig.mode() != EncryptMode.ECB) {
                if (iEncryptConfig.composeIV) {
                    val fileIv = ByteArray(16)
                    fis.read(fileIv, 0, 16)
                    cipher.init(Cipher.DECRYPT_MODE, keySpec, IvParameterSpec(fileIv))
                } else {
                    cipher.init(Cipher.DECRYPT_MODE, keySpec, IvParameterSpec(iEncryptConfig.iv()))
                }
                currentDecryptBytes = 16
                encryptProgressListener?.encryptBytes(currentDecryptBytes)
            } else {
                cipher.init(Cipher.DECRYPT_MODE, keySpec)
                encryptProgressListener?.encryptBytes(currentDecryptBytes)
            }
            outputStream.use { fos ->
                CipherInputStream(fis, cipher).use { cis ->
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while (cis.read(buffer).also { bytesRead = it } != -1) {
                        fos.write(buffer, 0, bytesRead)
                        currentDecryptBytes += bytesRead
                        encryptProgressListener?.encryptBytes(currentDecryptBytes)
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
        val cipher = try {
            Cipher.getInstance(
                "${iEncryptConfig.algorithm().name}/${iEncryptConfig.mode().mode}/${iEncryptConfig.padding().padding}",
                BouncyCastleProvider.PROVIDER_NAME
            )
        } catch (e: NoSuchAlgorithmException) {
            throw EncryptException(EncryptExceptionEnums.MODE_NOT_SUPPORT)
        }
        val keySpec = SecretKeySpec(iEncryptConfig.key(), iEncryptConfig.algorithm().name)
        if (iv != null && iEncryptConfig.mode() != EncryptMode.ECB) {
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, IvParameterSpec(iv))
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)
        }
        encryptProgressListener?.encryptProgress(0)
        val encryptedData = cipher.doFinal(dataBytes)
        encryptProgressListener?.encryptProgress(100)
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
        val cipher = try {
            Cipher.getInstance(
                "${iEncryptConfig.algorithm().name}/${iEncryptConfig.mode().mode}/${iEncryptConfig.padding().padding}",
                BouncyCastleProvider.PROVIDER_NAME
            )
        } catch (e: NoSuchAlgorithmException) {
            throw EncryptException(EncryptExceptionEnums.MODE_NOT_SUPPORT)
        }
        val keySpec = SecretKeySpec(iEncryptConfig.key(), iEncryptConfig.algorithm().name)
        encryptProgressListener?.decryptProgress(0)
        val result = if (iv != null && iEncryptConfig.composeIV && iEncryptConfig.mode() != EncryptMode.ECB) {
            val actualIv = dataBytes.copyOfRange(0, iv.size)
            val actualData = dataBytes.copyOfRange(iv.size, dataBytes.size)
            cipher.init(Cipher.DECRYPT_MODE, keySpec, IvParameterSpec(actualIv))
            cipher.doFinal(actualData)
        } else if (iv != null && iEncryptConfig.mode() != EncryptMode.ECB) {
            cipher.init(Cipher.DECRYPT_MODE, keySpec, IvParameterSpec(iv))
            cipher.doFinal(dataBytes)
        } else {
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            cipher.doFinal(dataBytes)
        }
        encryptProgressListener?.decryptProgress(100)
        return result
    }

    override fun addProgressListener(iEncryptProgressListener: IEncryptProgressListener) {
        this.encryptProgressListener = iEncryptProgressListener
    }
}
