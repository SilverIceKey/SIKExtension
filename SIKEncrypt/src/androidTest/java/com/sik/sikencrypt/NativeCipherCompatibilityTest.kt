package com.sik.sikencrypt

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sik.sikencrypt.enrypt.AESEncrypt
import com.sik.sikencrypt.enrypt.DESEncrypt
import com.sik.sikencrypt.enrypt.DESedeEncrypt
import com.sik.sikencrypt.enrypt.RSAEncrypt
import com.sik.sikencrypt.enrypt.SM4Encrypt
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.security.KeyPairGenerator

@RunWith(AndroidJUnit4::class)
class NativeCipherCompatibilityTest {

    @Before
    fun setUp() {
        NativeCipher.registerKeyForTest("native-des", DES_KEY)
        NativeCipher.registerKeyForTest("native-aes", AES_KEY)
    }

    @Test
    fun nativeDesMatchesLegacyDes() {
        val plain = "native DES compatibility".toByteArray(StandardCharsets.UTF_8)
        val legacy = DESEncrypt(TestConfig(
            rawKey = DES_KEY,
            rawIv = DES_IV,
            cipherAlgorithm = EncryptAlgorithm.DES,
            cipherMode = EncryptMode.CBC,
            cipherPadding = EncryptPadding.PKCS5Padding
        ))
        val native = EncryptUtils.getProtectedAlgorithm(
            TestProtectedConfig(
                id = "native-des",
                cipherAlgorithm = EncryptAlgorithm.DES,
                cipherMode = EncryptMode.CBC,
                cipherPadding = EncryptPadding.PKCS5Padding,
                vector = DES_IV
            ),
            NativeCipherProvider()
        )

        assertArrayEquals(legacy.encryptToByteArray(plain), native.encryptToByteArray(plain))
        assertEquals(legacy.encryptToBase64(plain), native.encryptToBase64(plain))
        assertArrayEquals(plain, native.decryptFromByteArray(native.encryptToByteArray(plain)))
    }

    @Test
    fun nativeAesMatchesLegacyAesAndStreaming() {
        val plain = "native AES stream compatibility 测试".toByteArray(StandardCharsets.UTF_8)
        val legacy = AESEncrypt(TestConfig(
            rawKey = AES_KEY,
            rawIv = AES_IV,
            cipherAlgorithm = EncryptAlgorithm.AES,
            cipherMode = EncryptMode.CBC,
            cipherPadding = EncryptPadding.PKCS5Padding
        ))
        val native = EncryptUtils.getProtectedAlgorithm(
            TestProtectedConfig(
                id = "native-aes",
                cipherAlgorithm = EncryptAlgorithm.AES,
                cipherMode = EncryptMode.CBC,
                cipherPadding = EncryptPadding.PKCS5Padding,
                vector = AES_IV
            ),
            NativeCipherProvider()
        )

        assertArrayEquals(legacy.encryptToByteArray(plain), native.encryptToByteArray(plain))
        val encrypted = ByteArrayOutputStream()
        native.encryptStream(ByteArrayInputStream(plain), encrypted)
        val decrypted = ByteArrayOutputStream()
        native.decryptStream(ByteArrayInputStream(encrypted.toByteArray()), decrypted)
        assertArrayEquals(plain, decrypted.toByteArray())
    }

    @Test
    fun nativeSm4AndDesedeMatchLegacy() {
        val sm4Key = ByteArray(16) { (0x90 + it).toByte() }
        val sm4Iv = ByteArray(16) { (0xA0 + it).toByte() }
        val plain = "SM4 and DESede compatibility".toByteArray(StandardCharsets.UTF_8)
        NativeCipher.registerKeyForTest("native-sm4", sm4Key)

        val legacySm4 = SM4Encrypt(TestConfig(
            rawKey = sm4Key,
            rawIv = sm4Iv,
            cipherAlgorithm = EncryptAlgorithm.SM4,
            cipherMode = EncryptMode.CBC,
            cipherPadding = EncryptPadding.PKCS5Padding
        ))
        val nativeSm4 = EncryptUtils.getProtectedAlgorithm(
            TestProtectedConfig(
                id = "native-sm4",
                cipherAlgorithm = EncryptAlgorithm.SM4,
                cipherMode = EncryptMode.CBC,
                cipherPadding = EncryptPadding.PKCS5Padding,
                vector = sm4Iv
            ),
            NativeCipherProvider()
        )
        assertArrayEquals(legacySm4.encryptToByteArray(plain), nativeSm4.encryptToByteArray(plain))
        assertArrayEquals(plain, nativeSm4.decryptFromByteArray(nativeSm4.encryptToByteArray(plain)))

        val desedeKey = ByteArray(24) { (0xB0 + it).toByte() }
        val desedeIv = ByteArray(8) { (0xC0 + it).toByte() }
        NativeCipher.registerKeyForTest("native-desede", desedeKey)
        val legacyDesede = DESedeEncrypt(TestConfig(
            rawKey = desedeKey,
            rawIv = desedeIv,
            cipherAlgorithm = EncryptAlgorithm.DESede,
            cipherMode = EncryptMode.CBC,
            cipherPadding = EncryptPadding.PKCS5Padding
        ))
        val nativeDesede = EncryptUtils.getProtectedAlgorithm(
            TestProtectedConfig(
                id = "native-desede",
                cipherAlgorithm = EncryptAlgorithm.DESede,
                cipherMode = EncryptMode.CBC,
                cipherPadding = EncryptPadding.PKCS5Padding,
                vector = desedeIv
            ),
            NativeCipherProvider()
        )
        assertArrayEquals(
            legacyDesede.encryptToByteArray(plain),
            nativeDesede.encryptToByteArray(plain)
        )
        assertArrayEquals(plain, nativeDesede.decryptFromByteArray(nativeDesede.encryptToByteArray(plain)))
    }

    @Test
    fun nativeRsaCanDecryptLegacyCipherAndLegacyCanDecryptNativeCipher() {
        val keyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
        NativeCipher.registerKeyForTest("native-rsa-public", keyPair.public.encoded)
        NativeCipher.registerKeyForTest("native-rsa-private", keyPair.private.encoded)
        val plain = "RSA compatibility".toByteArray(StandardCharsets.UTF_8)
        val legacy = RSAEncrypt(RsaConfig(keyPair.public.encoded, keyPair.private.encoded))
        val nativeEncrypt = EncryptUtils.getProtectedAlgorithm(
            TestProtectedConfig(
                id = "native-rsa-public",
                cipherAlgorithm = EncryptAlgorithm.RSA,
                cipherMode = EncryptMode.ECB,
                cipherPadding = EncryptPadding.PKCS1Padding,
                vector = null
            ),
            NativeCipherProvider()
        )
        val nativeDecrypt = EncryptUtils.getProtectedAlgorithm(
            TestProtectedConfig(
                id = "native-rsa-private",
                cipherAlgorithm = EncryptAlgorithm.RSA,
                cipherMode = EncryptMode.ECB,
                cipherPadding = EncryptPadding.PKCS1Padding,
                vector = null
            ),
            NativeCipherProvider()
        )

        assertArrayEquals(plain, nativeDecrypt.decryptFromByteArray(legacy.encryptToByteArray(plain)))
        assertArrayEquals(plain, legacy.decryptFromByteArray(nativeEncrypt.encryptToByteArray(plain)))
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
        private val cipherAlgorithm: EncryptAlgorithm,
        private val cipherMode: EncryptMode,
        private val cipherPadding: EncryptPadding,
        private val vector: ByteArray?
    ) : IProtectedEncryptConfig {
        override fun keyId(): String = id
        override fun algorithm(): EncryptAlgorithm = cipherAlgorithm
        override fun mode(): EncryptMode = cipherMode
        override fun padding(): EncryptPadding = cipherPadding
        override fun iv(): ByteArray? = vector
        override val composeIV: Boolean = false
    }

    private companion object {
        val DES_KEY = ByteArray(8) { (0x10 + it).toByte() }
        val DES_IV = ByteArray(8) { (0x20 + it).toByte() }
        val AES_KEY = ByteArray(16) { (0x30 + it).toByte() }
        val AES_IV = ByteArray(16) { (0x40 + it).toByte() }
    }

    private class RsaConfig(
        private val publicKeyBytes: ByteArray,
        private val privateKeyBytes: ByteArray
    ) : IRSAEncryptConfig() {
        override fun publicKey(): ByteArray = publicKeyBytes
        override fun privateKey(): ByteArray = privateKeyBytes
        override fun privateKeySize(): Int = 2048
        override fun padding(): EncryptPadding = EncryptPadding.PKCS1Padding
    }
}
