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
        return EncryptPadding.PKCS5Padding
    }

    override val composeIV: Boolean
        get() = super.composeIV
}