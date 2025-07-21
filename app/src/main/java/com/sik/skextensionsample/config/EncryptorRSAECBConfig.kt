package com.sik.skextensionsample.config

import com.sik.sikencrypt.EncryptAlgorithm
import com.sik.sikencrypt.EncryptMode
import com.sik.sikencrypt.EncryptPadding
import com.sik.sikencrypt.IEncryptConfig
import com.sik.sikencrypt.IRSAEncryptConfig

class EncryptorRSAECBConfig : IRSAEncryptConfig() {
    companion object {
        @JvmStatic
        val encryptorConfig: EncryptorRSAECBConfig by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            EncryptorRSAECBConfig()
        }
    }

    override fun algorithm(): EncryptAlgorithm {
        return EncryptAlgorithm.RSA
    }

    override fun publicKey(): ByteArray {
        return byteArrayOf()
    }

    override fun privateKey(): ByteArray {
        return  byteArrayOf()
    }
}