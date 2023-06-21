package com.sik.sikencrypt

/**
 * 信息摘要接口
 *
 */
interface IMessageDigest {
    /**
     * 获取信息摘要使用十六进制输出
     *
     * @param dataBytes
     * @return
     */
    fun digestToHex(dataBytes: ByteArray): String

    /**
     * 获取信息摘要使用Base64输出
     *
     * @param dataBytes
     * @return
     */
    fun digestToBase64(dataBytes: ByteArray): String
}