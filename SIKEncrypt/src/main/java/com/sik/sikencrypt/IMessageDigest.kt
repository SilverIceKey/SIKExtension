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

    /**
     * 获取信息摘要使用字节数组输出
     *
     * @param dataBytes
     * @return
     */
    fun digest(dataBytes: ByteArray): ByteArray

    /**
     * 获取文件的信息摘要，选择输出类型，默认十六进制字符串
     * @param srcFile 文件地址
     * @param outType 输出文本类型
     */
    fun digestFile(
        srcFile: String,
        outType: MessageDigestFileOutType = MessageDigestFileOutType.HEX
    ): String
}