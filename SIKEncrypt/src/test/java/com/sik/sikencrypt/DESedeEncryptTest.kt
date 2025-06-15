package com.sik.sikencrypt

import com.sik.sikencrypt.enrypt.DESedeEncrypt
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
class DESedeEncryptTest {

    private lateinit var config: IEncryptConfig

    @Before
    fun setUp() {
        Security.addProvider(BouncyCastleProvider())
        config = mockk {
            val key = ByteArray(24) { (it + 1).toByte() }
            every { key() } returns key
            every { algorithm() } returns EncryptAlgorithm.DESede
            every { mode() } returns EncryptMode.CBC
            every { padding() } returns EncryptPadding.PKCS5Padding
            val iv = ByteArray(8) { (0xB0 + it).toByte() }
            every { iv() } returns iv
            every { composeIV } returns false
        }
    }

    @Test
    fun `encryptToHex and decryptFromHex`() {
        val triple = DESedeEncrypt(config)
        val plain = "3DES hex"
        val hex = triple.encryptToHex(plain.toByteArray(StandardCharsets.UTF_8))
        val recovered = triple.decryptFromHex(hex)
        assertEquals(plain, recovered)
    }

    @Test
    fun `encryptToBase64 and decryptFromBase64`() {
        val triple = DESedeEncrypt(config)
        val plain = "3DES base64"
        val b64 = triple.encryptToBase64(plain.toByteArray(StandardCharsets.UTF_8))
        val recovered = triple.decryptFromBase64(b64)
        assertEquals(plain, recovered)
    }

    @Test
    fun `encrypt and decrypt byte array`() {
        val triple = DESedeEncrypt(config)
        val data = "3DES bytes".toByteArray()
        val enc = triple.encryptToByteArray(data)
        val dec = triple.decryptFromByteArray(enc)
        assertArrayEquals(data, dec)
    }
}
