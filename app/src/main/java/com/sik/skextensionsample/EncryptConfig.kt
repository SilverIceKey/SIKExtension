package com.sik.skextensionsample

import com.sik.sikencrypt.EncryptAlgorithm
import com.sik.sikencrypt.EncryptMode
import com.sik.sikencrypt.EncryptPadding
import com.sik.sikencrypt.IEncryptConfig
import java.util.UUID

class EncryptConfig : IEncryptConfig {
    override fun key(): ByteArray {
        return "273eabc706254bd7".toByteArray()
    }

    override fun iv(): ByteArray? {
        return "131fd14cd4be40bf".toByteArray()
    }

    override fun algorithm(): EncryptAlgorithm {
        return EncryptAlgorithm.SM4
    }

    override fun mode(): EncryptMode {
        return EncryptMode.CBC
    }

    override fun padding(): EncryptPadding {
        return EncryptPadding.PKCS5Padding
    }
}