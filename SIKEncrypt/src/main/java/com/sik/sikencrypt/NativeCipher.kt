package com.sik.sikencrypt

/**
 * SIKEncrypt Native cipher JNI bridge.
 *
 * keyId 只是不透明标识。测试和迁移阶段可以用 [registerKeyForTest] 注入
 * Native 内存密钥；业务正式接入时应由 App 自己的 KeyVault 在 Native 侧
 * 注册密钥，避免密钥从 Java 传入。
 */
object NativeCipher {
    init {
        System.loadLibrary("sikencrypt_crypto")
    }

    @JvmStatic
    external fun registerKeyForTest(
        keyId: String,
        key: ByteArray
    )

    @JvmStatic
    external fun openSession(
        keyId: String,
        algorithm: String,
        mode: String,
        padding: String,
        iv: ByteArray?,
        ivFromKey: Boolean,
        encrypt: Boolean
    ): Long

    @JvmStatic
    external fun update(handle: Long, data: ByteArray): ByteArray

    @JvmStatic
    external fun doFinal(handle: Long): ByteArray

    @JvmStatic
    external fun close(handle: Long)
}
