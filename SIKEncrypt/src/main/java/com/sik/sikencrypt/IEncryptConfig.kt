package com.sik.sikencrypt

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
    fun iv(): ByteArray?

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
}