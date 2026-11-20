package com.sik.sikencrypt

/**
 * 基于 SIKEncrypt Native cipher 的 Provider。
 *
 * 该类不包含任何业务 keyId，也不包含业务密钥。
 */
class NativeCipherProvider : IProtectedEncryptProvider {

    override fun openEncrypt(
        config: IProtectedEncryptConfig,
        iv: ByteArray?
    ): IProtectedCipherSession {
        return open(config, iv, true)
    }

    override fun openDecrypt(
        config: IProtectedEncryptConfig,
        iv: ByteArray?
    ): IProtectedCipherSession {
        return open(config, iv, false)
    }

    private fun open(
        config: IProtectedEncryptConfig,
        iv: ByteArray?,
        encrypt: Boolean
    ): IProtectedCipherSession {
        val handle = try {
            NativeCipher.openSession(
                keyId = config.keyId(),
                algorithm = config.algorithm().name,
                mode = config.mode().mode,
                padding = config.padding().padding,
                iv = iv,
                ivFromKey = config.ivFromKey,
                encrypt = encrypt
            )
        } catch (error: Throwable) {
            throw encryptException(encrypt, error)
        }
        if (handle == 0L) {
            throw encryptException(encrypt)
        }
        return NativeCipherSession(handle, encrypt)
    }

    private class NativeCipherSession(
        private val handle: Long,
        private val encrypt: Boolean
    ) : IProtectedCipherSession {
        private var closed = false

        @Synchronized
        override fun update(data: ByteArray): ByteArray {
            checkOpen()
            return try {
                NativeCipher.update(handle, data)
            } catch (error: Throwable) {
                throw encryptException(encrypt, error)
            }
        }

        @Synchronized
        override fun doFinal(): ByteArray {
            checkOpen()
            return try {
                NativeCipher.doFinal(handle)
            } catch (error: Throwable) {
                throw encryptException(encrypt, error)
            }
        }

        @Synchronized
        override fun close() {
            if (!closed) {
                try {
                    NativeCipher.close(handle)
                } finally {
                    closed = true
                }
            }
        }

        private fun checkOpen() {
            if (closed) {
                throw EncryptException(EncryptExceptionEnums.ENCRYPT_ERROR)
            }
        }
    }

    private companion object {
        fun encryptException(encrypt: Boolean, cause: Throwable? = null): EncryptException {
            return EncryptException(
                if (encrypt) {
                    EncryptExceptionEnums.ENCRYPT_ERROR
                } else {
                    EncryptExceptionEnums.DECRYPT_ERROR
                },
                cause
            )
        }
    }
}
