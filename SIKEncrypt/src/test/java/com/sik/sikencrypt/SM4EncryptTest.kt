package com.sik.sikencrypt

import com.sik.sikencrypt.enrypt.SM4Encrypt
import io.mockk.every
import io.mockk.mockk
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.nio.charset.StandardCharsets
import java.security.Security

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class SM4EncryptTest {

    private lateinit var config: IEncryptConfig

    @Before
    fun setUp() {
        Security.addProvider(BouncyCastleProvider())
        config = mockk {
            val key = ByteArray(16) { (0x10 + it).toByte() }
            every { key() } returns key
            every { algorithm() } returns EncryptAlgorithm.SM4
            every { mode() } returns EncryptMode.CBC
            every { padding() } returns EncryptPadding.PKCS5Padding
            val iv = ByteArray(16) { (0x20 + it).toByte() }
            every { iv() } returns iv
            every { composeIV } returns false
        }
    }

    @Test
    fun `encryptToHex and decryptFromHex`() {
        val sm4 = SM4Encrypt(config)
        val plain = "SM4 hex"
        val hex = sm4.encryptToHex(plain.toByteArray(StandardCharsets.UTF_8))
        val recovered = sm4.decryptFromHex(hex)
        assertEquals(plain, recovered)
    }

    @Test
    fun `encryptToBase64 and decryptFromBase64`() {
        val sm4 = SM4Encrypt(config)
        val plain = "SM4 base64"
        val b64 = sm4.encryptToBase64(plain.toByteArray(StandardCharsets.UTF_8))
        val recovered = sm4.decryptFromBase64(b64)
        assertEquals(plain, recovered)
    }

    @Test
    fun `encrypt and decrypt byte array`() {
        val sm4 = SM4Encrypt(config)
        val data = "SM4 bytes".toByteArray()
        val enc = sm4.encryptToByteArray(data)
        val dec = sm4.decryptFromByteArray(enc)
        assertArrayEquals(data, dec)
    }

    @Test
    fun `composeIV round-trip`() {
        val composeConfig = mockk<IEncryptConfig> {
            every { key() } returns config.key()
            every { algorithm() } returns EncryptAlgorithm.SM4
            every { mode() } returns EncryptMode.CBC
            every { padding() } returns EncryptPadding.PKCS5Padding
            val iv = ByteArray(16) { (0xAB + it).toByte() }
            every { iv() } returns iv
            every { composeIV } returns true
        }
        val sm4Compose = SM4Encrypt(composeConfig)
        val text = "ComposeIV option"
        val cipher = sm4Compose.encryptToByteArray(text.toByteArray(StandardCharsets.UTF_8))
        val plain = sm4Compose.decryptFromByteArray(cipher)
        assertEquals(text, String(plain, StandardCharsets.UTF_8))
    }
}
