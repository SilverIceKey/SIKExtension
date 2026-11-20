package com.sik.sikencrypt

import java.io.Closeable

/**
 * 受保护加密的 Native/业务侧实现入口。
 *
 * SIKEncrypt 只负责参数、文件和流封装，不负责取 Key，也不接收 Key 字节。
 * Provider 的实现应在业务侧完成，真正的密钥和 Cipher 可以完全留在 Native 层。
 */
interface IProtectedEncryptProvider {
    fun openEncrypt(
        config: IProtectedEncryptConfig,
        iv: ByteArray?
    ): IProtectedCipherSession

    fun openDecrypt(
        config: IProtectedEncryptConfig,
        iv: ByteArray?
    ): IProtectedCipherSession
}

/**
 * 一次加解密会话。
 *
 * 文件、流和 GCM 等有状态模式必须使用同一个 session 连续 update，
 * 不能每处理一个分块就重新创建 Cipher。
 */
interface IProtectedCipherSession : Closeable {
    fun update(data: ByteArray): ByteArray

    fun doFinal(): ByteArray
}
