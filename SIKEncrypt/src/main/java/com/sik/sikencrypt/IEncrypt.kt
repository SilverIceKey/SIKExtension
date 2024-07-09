package com.sik.sikencrypt

/**
 * 加解密接口
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
    fun encryptToByteArray(dataBytes: ByteArray): ByteArray

    /**
     * 加密文件
     *
     * @param srcFile
     * @param destFile
     */
    @Throws(EncryptException::class)
    fun encryptFile(srcFile: String, destFile: String)

    /**
     * 文件自加密
     */
    @Throws(EncryptException::class)
    fun encryptSelfFile(srcFile: String)

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
    fun decryptFromByteArray(dataBytes: ByteArray): ByteArray

    /**
     * 解密文件
     *
     * @param srcFile
     * @param destFile
     * @return
     */
    @Throws(EncryptException::class)
    fun decryptFromFile(srcFile: String, destFile: String)

    /**
     * 文件自解密
     */
    @Throws(EncryptException::class)
    fun decryptSelfFile(srcFile: String)
}