package com.sik.sikencrypt

import kotlin.jvm.Throws

/**
 * 信息摘要接口
 *
 */
interface IEncrypt {
    /**
     * 加密使用十六进制输出
     *
     * @param dataBytes
     * @return
     */
    @Throws(EncryptException::class)
    fun encryptToHex(dataBytes: ByteArray): String

    /**
     * 加密使用Base64输出
     *
     * @param dataBytes
     * @return
     */
    @Throws(EncryptException::class)
    fun encryptToBase64(dataBytes: ByteArray): String

    /**
     * Encrypt to byte array
     * 加密使用byte数组输出
     * @param dataBytes
     * @return
     */
    @Throws(EncryptException::class)
    fun encryptToByteArray(dataBytes: ByteArray):ByteArray

    /**
     * 从十六进制解密
     *
     * @param dataStr
     * @return
     */
    @Throws(EncryptException::class)
    fun decryptFromHex(dataStr: String): String

    /**
     * 从Base64解密
     *
     * @param dataStr
     * @return
     */
    @Throws(EncryptException::class)
    fun decryptFromBase64(dataStr: String): String

    /**
     * Decrypt from byte array
     * 从byte数组解密
     * @param dataBytes
     * @return
     */
    @Throws(EncryptException::class)
    fun decryptFromByteArray(dataBytes: ByteArray):ByteArray
}