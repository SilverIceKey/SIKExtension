package com.sik.sikencrypt

import java.security.SecureRandom

/**
 * 加解密配置接口
 *
 */
interface IEncryptConfig {
    /**
     * 密钥
     *
     * @return
     */
    fun key(): ByteArray

    /**
     * 偏移
     *
     * @return
     */
    fun iv(): ByteArray? {
        val iv =
            ByteArray(
                if (algorithm() in listOf(
                        EncryptAlgorithm.DES,
                        EncryptAlgorithm.DESede
                    )
                ) 8 else 16
            ) // 8-byte IV for DES, 16-byte IV for AES
        SecureRandom().nextBytes(iv)
        return iv
    }

    /**
     * 加解密方式
     *
     * @return
     */
    fun algorithm(): EncryptAlgorithm

    /**
     * 加解密模式
     *
     * @return
     */
    fun mode(): EncryptMode

    /**
     * 加解密填充
     *
     * @return
     */
    fun padding(): EncryptPadding

    /**
     * 在头部组合IV和加密结果
     */
    val composeIV: Boolean
        get() = true
}