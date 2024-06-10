package com.sik.skextensionsample

import com.sik.sikencrypt.IRSAEncryptConfig

class RSAEncryptConfig : IRSAEncryptConfig() {
    override fun publicKey(): ByteArray {
        return ByteArray(0)
    }

    override fun privateKey(): ByteArray {
        return ByteArray(0)
    }
}