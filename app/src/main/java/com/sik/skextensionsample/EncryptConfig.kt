package com.sik.skextensionsample

import com.sik.sikencrypt.EncryptAlgorithm
import com.sik.sikencrypt.EncryptMode
import com.sik.sikencrypt.EncryptPadding
import com.sik.sikencrypt.IEncryptConfig
import java.util.UUID

class EncryptConfig : IEncryptConfig {
    private val uuidKey = UUID.randomUUID().toString().replace("-", "").toByteArray()
    private val uuidIv = UUID.randomUUID().toString().replace("-", "").toByteArray()
    override fun key(): ByteArray {
        return uuidKey
    }

    override fun iv(): ByteArray? {
        return uuidIv
    }

    override fun algorithm(): EncryptAlgorithm {
        return EncryptAlgorithm.AES
    }

    override fun mode(): EncryptMode {
        return EncryptMode.ECB
    }

    override fun padding(): EncryptPadding {
        return EncryptPadding.PKCS5Padding
    }
}