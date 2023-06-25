package com.sik.sikencrypt

import kotlin.jvm.Throws

/**
 * 信息摘要接口
 *
 */
interface IEncrypt {
    /**
     * 获取信息摘要使用十六进制输出
     *
     * @param dataBytes
     * @return
     */
    @Throws(EncryptException::class)
    fun encryptToHex(dataBytes: ByteArray): String

    /**
     * 获取信息摘要使用Base64输出
     *
     * @param dataBytes
     * @return
     */
    @Throws(EncryptException::class)
    fun encryptToBase64(dataBytes: ByteArray): String

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
}