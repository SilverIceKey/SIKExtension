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
        val handle = NativeCipher.openSession(
            keyId = config.keyId(),
            algorithm = config.algorithm().name,
            mode = config.mode().mode,
            padding = config.padding().padding,
            iv = iv,
            ivFromKey = config.ivFromKey,
            encrypt = encrypt
        )
        if (handle == 0L) {
            throw EncryptException(EncryptExceptionEnums.ENCRYPT_ERROR)
        }
        return NativeCipherSession(handle)
    }

    private class NativeCipherSession(
        private val handle: Long
    ) : IProtectedCipherSession {
        private var closed = false

        override fun update(data: ByteArray): ByteArray {
            checkOpen()
            return NativeCipher.update(handle, data)
        }

        override fun doFinal(): ByteArray {
            checkOpen()
            return NativeCipher.doFinal(handle)
        }

        override fun close() {
            if (!closed) {
                closed = true
                NativeCipher.close(handle)
            }
        }

        private fun checkOpen() {
            if (closed) {
                throw EncryptException(EncryptExceptionEnums.ENCRYPT_ERROR)
            }
        }
    }
}
