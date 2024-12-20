package com.sik.sikencrypt

/**
 * 加解密进度监听器
 */
interface IEncryptProgressListener {
    /**
     * 加密进度
     */
    fun encryptProgress(progress: Int)

    /**
     * 加密进度
     */
    fun encryptBytes(bytes: Int)

    /**
     * 解密进度
     */
    fun decryptProgress(progress: Int)

    /**
     * 解密进度
     */
    fun decryptBytes(bytes: Int)
}