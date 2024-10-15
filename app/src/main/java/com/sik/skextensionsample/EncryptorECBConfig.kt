package com.sik.skextensionsample

import com.sik.sikencrypt.EncryptAlgorithm
import com.sik.sikencrypt.EncryptMode
import com.sik.sikencrypt.EncryptPadding
import com.sik.sikencrypt.IEncryptConfig

class EncryptorECBConfig : IEncryptConfig {
    companion object {
        @JvmStatic
        val encryptorConfig: EncryptorECBConfig by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            EncryptorECBConfig()
        }
    }

    override fun algorithm(): EncryptAlgorithm {
        return EncryptAlgorithm.SM4
    }

    override fun key(): ByteArray {
        return "E4D96BE2A522589B".toByteArray()
    }

    override fun mode(): EncryptMode {
        return EncryptMode.ECB
    }

    override fun padding(): EncryptPadding {
        return EncryptPadding.PKCS5Padding
    }

    override val composeIV: Boolean
        get() = false
}