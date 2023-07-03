package com.sik.sikencrypt

/**
 * RSA加解密接口
 *
 */
interface IRSAEncrypt : IEncrypt {
    /**
     * 生成公钥私钥对
     *
     * @return
     */
    fun generateKeyPair(): IRSAEncrypt

    /**
     * 获取公钥
     *
     * @return
     */
    fun getPublicKeyBytes(): ByteArray

    /**
     * 获取私钥
     *
     * @return
     */
    fun getPrivateKeyBytes(): ByteArray
}