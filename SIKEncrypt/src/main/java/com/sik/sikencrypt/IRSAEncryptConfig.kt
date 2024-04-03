package com.sik.sikencrypt

/**
 * RSA加密配置
 *
 */
interface IRSAEncryptConfig : IEncryptConfig {
    /**
     * RSA专用公钥
     *
     * @return
     */
    fun publicKey(): ByteArray

    /**
     * RSA专用私钥
     *
     * @return
     */
    fun privateKey(): ByteArray
}