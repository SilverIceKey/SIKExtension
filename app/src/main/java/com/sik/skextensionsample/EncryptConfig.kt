package com.sik.skextensionsample

import com.sik.sikencrypt.EncryptAlgorithm
import com.sik.sikencrypt.EncryptMode
import com.sik.sikencrypt.EncryptPadding
import com.sik.sikencrypt.IEncryptConfig

class EncryptConfig : IEncryptConfig {
    override fun key(): ByteArray {
        return "FA0E80E4E736214CCED4A3732BE35CD4".substring(0, 16).toByteArray()
    }

    override fun iv(): ByteArray? {
        return "CB136CDA4C676998E4AEC36B19707CC0".substring(0,16).toByteArray()
    }

    override fun algorithm(): EncryptAlgorithm {
        return EncryptAlgorithm.AES
    }

    override fun mode(): EncryptMode {
        return EncryptMode.GCM
    }

    override fun padding(): EncryptPadding {
        return EncryptPadding.PKCS5Padding
    }

    override val composeIV: Boolean
        get() = super.composeIV
}