package com.sik.sikencrypt

import com.sik.sikencrypt.enrypt.AESEncrypt
import com.sik.sikencrypt.enrypt.DESEncrypt
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets
import java.security.Security
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class ProtectedEncryptTest {

    @Before
    fun setUp() {
        Security.addProvider(BouncyCastleProvider())
    }

    @Test
    fun `protected DES path matches legacy DES output`() {
        val key = ByteArray(8) { (0x10 + it).toByte() }
        val iv = ByteArray(8) { (0x20 + it).toByte() }
        val data = "DES protected compatibility".toByteArray(StandardCharsets.UTF_8)

        val legacy = DESEncrypt(TestConfig(
            rawKey = key,
            rawIv = iv,
            cipherAlgorithm = EncryptAlgorithm.DES,
            cipherMode = EncryptMode.CBC,
            cipherPadding = EncryptPadding.PKCS5Padding
        ))
        val protected = ProtectedEncrypt(
            TestProtectedConfig("des-test", EncryptAlgorithm.DES, EncryptMode.CBC, EncryptPadding.PKCS5Padding, iv, false),
            FakeProvider(key)
        )

        assertEquals(legacy.encryptToBase64(data), protected.encryptToBase64(data))
        assertArrayEquals(
            legacy.encryptToByteArray(data),
            protected.encryptToByteArray(data)
        )
        assertEquals(
            String(legacy.decryptFromByteArray(legacy.encryptToByteArray(data)), StandardCharsets.UTF_8),
            protected.decryptFromBase64(protected.encryptToBase64(data))
        )
    }

    @Test
    fun `protected AES path matches legacy AES output`() {
        val key = ByteArray(16) { (0x30 + it).toByte() }
        val iv = ByteArray(16) { (0x40 + it).toByte() }
        val data = "AES protected compatibility 测试".toByteArray(StandardCharsets.UTF_8)

        val legacy = AESEncrypt(TestConfig(
            rawKey = key,
            rawIv = iv,
            cipherAlgorithm = EncryptAlgorithm.AES,
            cipherMode = EncryptMode.CBC,
            cipherPadding = EncryptPadding.PKCS5Padding
        ))
        val protected = ProtectedEncrypt(
            TestProtectedConfig("aes-test", EncryptAlgorithm.AES, EncryptMode.CBC, EncryptPadding.PKCS5Padding, iv, false),
            FakeProvider(key)
        )

        assertEquals(legacy.encryptToHex(data), protected.encryptToHex(data))
        assertArrayEquals(
            legacy.encryptToByteArray(data),
            protected.encryptToByteArray(data)
        )
    }

    @Test
    fun `protected file and stream paths round trip`() {
        val key = ByteArray(16) { (0x50 + it).toByte() }
        val iv = ByteArray(16) { (0x60 + it).toByte() }
        val protected = ProtectedEncrypt(
            TestProtectedConfig("aes-file", EncryptAlgorithm.AES, EncryptMode.CBC, EncryptPadding.PKCS5Padding, iv, true),
            FakeProvider(key)
        )
        val plain = "file and stream compatibility 测试".toByteArray(StandardCharsets.UTF_8)

        val input = ByteArrayInputStream(plain)
        val encrypted = ByteArrayOutputStream()
        protected.encryptStream(input, encrypted)

        val decrypted = ByteArrayOutputStream()
        protected.decryptStream(ByteArrayInputStream(encrypted.toByteArray()), decrypted)
        assertArrayEquals(plain, decrypted.toByteArray())

        val source = File.createTempFile("sik-encrypt-src", ".txt")
        val target = File.createTempFile("sik-encrypt-enc", ".dat")
        val recovered = File.createTempFile("sik-encrypt-dec", ".txt")
        try {
            source.writeBytes(plain)
            protected.encryptFile(source.absolutePath, target.absolutePath)
            protected.decryptFromFile(target.absolutePath, recovered.absolutePath)
            assertArrayEquals(plain, recovered.readBytes())
        } finally {
            source.delete()
            target.delete()
            recovered.delete()
        }
    }

    private data class TestConfig(
        val rawKey: ByteArray,
        val rawIv: ByteArray?,
        val cipherAlgorithm: EncryptAlgorithm,
        val cipherMode: EncryptMode,
        val cipherPadding: EncryptPadding
    ) : IEncryptConfig {
        override fun key(): ByteArray = rawKey
        override fun iv(): ByteArray? = rawIv
        override fun algorithm(): EncryptAlgorithm = cipherAlgorithm
        override fun mode(): EncryptMode = cipherMode
        override fun padding(): EncryptPadding = cipherPadding
        override val composeIV: Boolean = false
    }

    private data class TestProtectedConfig(
        private val id: String,
        private val alg: EncryptAlgorithm,
        private val cipherMode: EncryptMode,
        private val cipherPadding: EncryptPadding,
        private val vector: ByteArray?,
        override val composeIV: Boolean
    ) : IProtectedEncryptConfig {
        override fun keyId(): String = id
        override fun algorithm(): EncryptAlgorithm = alg
        override fun mode(): EncryptMode = cipherMode
        override fun padding(): EncryptPadding = cipherPadding
        override fun iv(): ByteArray? = vector
    }

    private class FakeProvider(private val key: ByteArray) : IProtectedEncryptProvider {
        override fun openEncrypt(
            config: IProtectedEncryptConfig,
            iv: ByteArray?
        ): IProtectedCipherSession = open(config, iv, Cipher.ENCRYPT_MODE)

        override fun openDecrypt(
            config: IProtectedEncryptConfig,
            iv: ByteArray?
        ): IProtectedCipherSession = open(config, iv, Cipher.DECRYPT_MODE)

        private fun open(
            config: IProtectedEncryptConfig,
            iv: ByteArray?,
            opmode: Int
        ): IProtectedCipherSession {
            val cipher = Cipher.getInstance(
                "${config.algorithm().name}/${config.mode().mode}/${config.padding().padding}",
                BouncyCastleProvider()
            )
            val keySpec = SecretKeySpec(key, config.algorithm().name)
            if (iv != null && config.mode() != EncryptMode.ECB) {
                cipher.init(opmode, keySpec, IvParameterSpec(iv))
            } else {
                cipher.init(opmode, keySpec)
            }
            return Session(cipher)
        }
    }

    private class Session(private val cipher: Cipher) : IProtectedCipherSession {
        override fun update(data: ByteArray): ByteArray = cipher.update(data) ?: ByteArray(0)
        override fun doFinal(): ByteArray = cipher.doFinal()
        override fun close() = Unit
    }
}
