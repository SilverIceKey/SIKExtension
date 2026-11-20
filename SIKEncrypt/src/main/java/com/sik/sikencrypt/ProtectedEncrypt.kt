package com.sik.sikencrypt

import com.sik.sikcore.data.ConvertUtils
import com.sik.sikcore.extension.createNewFile
import com.sik.sikcore.extension.deleteIfExists
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset

/**
 * 通过业务侧 Provider 完成加解密的适配器。
 *
 * 该类的职责只有参数校验、Hex/Base64、文件/流 I/O 和进度回调。
 * 真正的密钥获取和密码学运算由 [IProtectedEncryptProvider] 实现。
 */
class ProtectedEncrypt(
    private val config: IProtectedEncryptConfig,
    private val provider: IProtectedEncryptProvider
) : IEncrypt {

    private var encryptProgressListener: IEncryptProgressListener? = null

    private val blockSize: Int
        get() = when (config.algorithm()) {
            EncryptAlgorithm.DES,
            EncryptAlgorithm.DESede -> 8

            EncryptAlgorithm.AES,
            EncryptAlgorithm.SM4 -> 16

            EncryptAlgorithm.RSA -> 0
        }

    private val hasIVMode: Boolean
        get() = config.mode() != EncryptMode.ECB &&
                config.algorithm() != EncryptAlgorithm.RSA

    init {
        if (hasIVMode && config.iv() == null && !config.ivFromKey) {
            throw EncryptException(EncryptExceptionEnums.NO_IV)
        }
        if (config.ivFromKey && config.composeIV && hasIVMode) {
            throw EncryptException(EncryptExceptionEnums.CONFIG_ERROR)
        }
    }

    override fun encryptToHex(dataBytes: ByteArray): String {
        return ConvertUtils.bytesToHex(encrypt(dataBytes))
    }

    override fun encryptToBase64(dataBytes: ByteArray): String {
        return ConvertUtils.bytesToBase64String(encrypt(dataBytes))
    }

    override fun encryptToByteArray(dataBytes: ByteArray): ByteArray {
        return encrypt(dataBytes)
    }

    override fun encryptFile(srcFile: String, destFile: String) {
        val inputFile = File(srcFile)
        if (!inputFile.exists()) {
            throw EncryptException(EncryptExceptionEnums.FILE_NOT_FOUND)
        }
        val outputFile = File(destFile)
        val iv = config.iv()
        val session = provider.openEncrypt(config, iv)
        try {
            FileInputStream(inputFile).use { fis ->
                FileOutputStream(outputFile).use { fos ->
                    if (config.composeIV && hasIVMode && iv != null) {
                        fos.write(iv)
                    }
                    transformFile(
                        fis,
                        fos,
                        session,
                        inputFile.length(),
                        true
                    )
                }
            }
        } finally {
            session.close()
        }
    }

    override fun encryptSelfFile(srcFile: String) {
        val inputFile = File(srcFile)
        if (!inputFile.exists()) {
            throw EncryptException(EncryptExceptionEnums.FILE_NOT_FOUND)
        }
        val tempFile = "${srcFile}.tmp"
        tempFile.deleteIfExists()
        tempFile.createNewFile()
        encryptFile(srcFile, tempFile)
        if (!inputFile.delete()) {
            tempFile.deleteIfExists()
            throw EncryptException(EncryptExceptionEnums.ENCRYPT_ERROR)
        }
        if (!File(tempFile).renameTo(inputFile)) {
            tempFile.deleteIfExists()
            throw EncryptException(EncryptExceptionEnums.ENCRYPT_ERROR)
        }
    }

    override fun encryptStream(inputStream: InputStream, outputStream: OutputStream) {
        val iv = config.iv()
        val session = provider.openEncrypt(config, iv)
        try {
            if (config.composeIV && hasIVMode && iv != null) {
                outputStream.write(iv)
            }
            transformFile(
                inputStream,
                outputStream,
                session,
                null,
                true
            )
        } finally {
            session.close()
        }
    }

    override fun decryptFromHex(dataStr: String): String {
        return decrypt(ConvertUtils.hexToBytes(dataStr))
            .toString(Charset.defaultCharset())
    }

    override fun decryptFromBase64(dataStr: String): String {
        return decrypt(ConvertUtils.base64StringToBytes(dataStr))
            .toString(Charset.defaultCharset())
    }

    override fun decryptFromByteArray(dataBytes: ByteArray): ByteArray {
        return decrypt(dataBytes)
    }

    override fun decryptFromFile(srcFile: String, destFile: String) {
        val inputFile = File(srcFile)
        if (!inputFile.exists()) {
            throw EncryptException(EncryptExceptionEnums.FILE_NOT_FOUND)
        }
        val outputFile = File(destFile)
        FileInputStream(inputFile).use { fis ->
            val iv = readIvForDecrypt(fis)
            val session = provider.openDecrypt(config, iv)
            try {
                FileOutputStream(outputFile).use { fos ->
                    transformFile(
                        fis,
                        fos,
                        session,
                        inputFile.length(),
                        false
                    )
                }
            } finally {
                session.close()
            }
        }
    }

    override fun decryptSelfFile(srcFile: String) {
        val inputFile = File(srcFile)
        if (!inputFile.exists()) {
            throw EncryptException(EncryptExceptionEnums.FILE_NOT_FOUND)
        }
        val tempFile = "${srcFile}.tmp"
        tempFile.deleteIfExists()
        tempFile.createNewFile()
        decryptFromFile(srcFile, tempFile)
        if (!inputFile.delete()) {
            tempFile.deleteIfExists()
            throw EncryptException(EncryptExceptionEnums.DECRYPT_ERROR)
        }
        if (!File(tempFile).renameTo(inputFile)) {
            tempFile.deleteIfExists()
            throw EncryptException(EncryptExceptionEnums.DECRYPT_ERROR)
        }
    }

    override fun decryptStream(inputStream: InputStream, outputStream: OutputStream) {
        val iv = readIvForDecrypt(inputStream)
        val session = provider.openDecrypt(config, iv)
        try {
            transformFile(
                inputStream,
                outputStream,
                session,
                null,
                false
            )
        } finally {
            session.close()
        }
    }

    override fun addProgressListener(iEncryptProgressListener: IEncryptProgressListener) {
        encryptProgressListener = iEncryptProgressListener
    }

    override fun removeProgressListener() {
        encryptProgressListener = null
    }

    private fun encrypt(dataBytes: ByteArray): ByteArray {
        validateNoPaddingSize(dataBytes)
        val iv = config.iv()
        val session = provider.openEncrypt(config, iv)
        val encrypted = try {
            session.update(dataBytes) + session.doFinal()
        } finally {
            session.close()
        }
        encryptProgressListener?.encryptProgress(100)
        return if (config.composeIV && hasIVMode && iv != null) {
            iv + encrypted
        } else {
            encrypted
        }
    }

    private fun decrypt(dataBytes: ByteArray): ByteArray {
        validateNoPaddingSize(dataBytes)
        val iv = config.iv()
        val body: ByteArray
        val actualIv: ByteArray?
        if (config.composeIV && hasIVMode && iv != null) {
            if (dataBytes.size < iv.size) {
                throw EncryptException(EncryptExceptionEnums.DECRYPT_ERROR)
            }
            actualIv = dataBytes.copyOfRange(0, iv.size)
            body = dataBytes.copyOfRange(iv.size, dataBytes.size)
        } else {
            actualIv = iv
            body = dataBytes
        }
        val session = provider.openDecrypt(config, actualIv)
        return try {
            session.update(body) + session.doFinal()
        } finally {
            session.close()
        }
    }

    private fun readIvForDecrypt(inputStream: InputStream): ByteArray? {
        if (!config.composeIV || !hasIVMode) {
            return config.iv()
        }
        val ivSize = blockSize
        if (ivSize <= 0) {
            return config.iv()
        }
        val iv = ByteArray(ivSize)
        var offset = 0
        while (offset < ivSize) {
            val count = inputStream.read(iv, offset, ivSize - offset)
            if (count < 0) {
                throw EncryptException(EncryptExceptionEnums.DECRYPT_ERROR)
            }
            offset += count
        }
        return iv
    }

    private fun transformFile(
        inputStream: InputStream,
        outputStream: OutputStream,
        session: IProtectedCipherSession,
        totalSize: Long?,
        encrypting: Boolean
    ) {
        var processed = 0L
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (true) {
            val count = inputStream.read(buffer)
            if (count < 0) {
                break
            }
            if (count == 0) {
                continue
            }
            val chunk = if (count == buffer.size) buffer else buffer.copyOf(count)
            val output = session.update(chunk)
            if (output.isNotEmpty()) {
                outputStream.write(output)
            }
            processed += count
            if (totalSize != null && totalSize > 0) {
                val percent = (processed.toDouble() / totalSize * 100).toInt()
                notifyProgress(percent, encrypting)
            } else {
                notifyBytes(processed, encrypting)
            }
        }
        val final = session.doFinal()
        if (final.isNotEmpty()) {
            outputStream.write(final)
        }
        notifyProgress(100, encrypting)
    }

    private fun validateNoPaddingSize(dataBytes: ByteArray) {
        if (config.algorithm() == EncryptAlgorithm.RSA) {
            return
        }
        if (config.padding() == EncryptPadding.NoPadding &&
            blockSize > 0 &&
            dataBytes.size % blockSize != 0
        ) {
            throw EncryptException(EncryptExceptionEnums.PADDING_NOT_SUPPORT_DATA_SIZE)
        }
    }

    private fun notifyProgress(percent: Int, encrypting: Boolean) {
        if (encrypting) {
            encryptProgressListener?.encryptProgress(percent)
        } else {
            encryptProgressListener?.decryptProgress(percent)
        }
    }

    private fun notifyBytes(bytes: Long, encrypting: Boolean) {
        if (encrypting) {
            encryptProgressListener?.encryptBytes(bytes.toInt())
        } else {
            encryptProgressListener?.decryptBytes(bytes.toInt())
        }
    }

    private companion object {
        const val DEFAULT_BUFFER_SIZE = 8 * 1024
    }
}
