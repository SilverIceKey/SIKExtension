package com.sik.skextensionsample.config

import com.sik.sikencrypt.EncryptAlgorithm
import com.sik.sikencrypt.EncryptMode
import com.sik.sikencrypt.EncryptPadding
import com.sik.sikencrypt.IEncryptConfig

class EncryptorCBCConfig : IEncryptConfig {
    companion object {
        @JvmStatic
        val encryptorConfig: EncryptorCBCConfig by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            EncryptorCBCConfig()
        }
    }

    var iv: ByteArray = byteArrayOf()

    override fun iv(): ByteArray? {
        return if (iv.isEmpty()) super.iv() else iv
    }

    override fun algorithm(): EncryptAlgorithm {
        return EncryptAlgorithm.SM4
    }

    override fun key(): ByteArray {
        return "02a32b6c1ab1fcda".toByteArray()
    }

    override fun mode(): EncryptMode {
        return EncryptMode.CBC
    }

    override fun padding(): EncryptPadding {
        return EncryptPadding.PKCS5Padding
    }

    override val composeIV: Boolean
        get() = true
}