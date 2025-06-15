package com.sik.sikencrypt

import com.sik.sikencrypt.enrypt.DESEncrypt
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
class DESEncryptTest {

    private lateinit var config: IEncryptConfig

    @Before
    fun setUp() {
        Security.addProvider(BouncyCastleProvider())
        config = mockk {
            val key = ByteArray(8) { it.toByte() }
            every { key() } returns key
            every { algorithm() } returns EncryptAlgorithm.DES
            every { mode() } returns EncryptMode.CBC
            every { padding() } returns EncryptPadding.PKCS5Padding
            val iv = ByteArray(8) { (0xA0 + it).toByte() }
            every { iv() } returns iv
            every { composeIV } returns false
        }
    }

    @Test
    fun `encryptToHex and decryptFromHex`() {
        val des = DESEncrypt(config)
        val plain = "DES hex test"
        val hex = des.encryptToHex(plain.toByteArray(StandardCharsets.UTF_8))
        val recovered = des.decryptFromHex(hex)
        assertEquals(plain, recovered)
    }

    @Test
    fun `encryptToBase64 and decryptFromBase64`() {
        val des = DESEncrypt(config)
        val plain = "DES base64"
        val b64 = des.encryptToBase64(plain.toByteArray(StandardCharsets.UTF_8))
        val recovered = des.decryptFromBase64(b64)
        assertEquals(plain, recovered)
    }

    @Test
    fun `encrypt and decrypt byte array`() {
        val des = DESEncrypt(config)
        val data = "DES bytes".toByteArray()
        val enc = des.encryptToByteArray(data)
        val dec = des.decryptFromByteArray(enc)
        assertArrayEquals(data, dec)
    }
}
