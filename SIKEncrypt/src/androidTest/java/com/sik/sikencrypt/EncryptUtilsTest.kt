package com.sik.sikencrypt

import org.junit.Test
import java.util.UUID


internal class EncryptUtilsTest {
    @Test
    fun aesEncryptTest() {
        val encryptConfig = EncryptConfig()
        println(String(encryptConfig.key()))
        println(String(encryptConfig.iv() ?: ByteArray(0)))
        val encryptResult =
            EncryptUtils.getAlgorithm(encryptConfig).encryptToBase64("123".toByteArray())
        println(encryptResult)
        println(EncryptUtils.getAlgorithm(encryptConfig).decryptFromBase64(encryptResult))
    }

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
}