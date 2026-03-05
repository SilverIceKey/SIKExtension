package com.sik.sikencrypt

/**
 * RSA加密配置
 *
 */
abstract class IRSAEncryptConfig : IEncryptConfig {
    /**
     * RSA专用公钥
     *
     * @return
     */
    abstract fun publicKey(): ByteArray

    /**
     * RSA专用私钥
     *
     * @return
     */
    abstract fun privateKey(): ByteArray

    /**
     * 密钥长度 有三种长度分别是:1024/2048/4096, 默认2048
     */
    open fun privateKeySize(): Int = 2048

    override fun key(): ByteArray {
        return ByteArray(0)
    }

    override fun iv(): ByteArray? {
        return ByteArray(0)
    }

    override fun algorithm(): EncryptAlgorithm {
        return EncryptAlgorithm.RSA
    }

    override fun mode(): EncryptMode {
        return EncryptMode.ECB
    }

    override fun padding(): EncryptPadding {
        return EncryptPadding.OAEPWithSHA256AndMGF1Padding
    }

    override val composeIV: Boolean
        get() = super.composeIV

    /**
     * 转换Pem为byte数组
     */
    protected fun convertPemToBytes(pem: String): ByteArray{
        val base64 = pem
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\\s".toRegex(), "")

        return Base64.decode(base64, Base64.DEFAULT)
    }
}