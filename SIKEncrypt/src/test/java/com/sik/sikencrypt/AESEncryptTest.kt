package com.sik.sikencrypt

import com.sik.sikencrypt.enrypt.AESEncrypt
import io.mockk.every
import io.mockk.mockk
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.*
import java.nio.charset.StandardCharsets
import java.security.Security

class AESEncryptTest {

    private lateinit var config: IEncryptConfig

    @Before
    fun setUp() {
        // 确保 BouncyCastle provider 可用
        Security.addProvider(BouncyCastleProvider())

        // Mock 一个基础的 AES/CBC/PKCS5Padding 配置
        config = mockk {
            // 128-bit key
            val key = ByteArray(16) { it.toByte() }
            every { key() } returns key

            every { algorithm() } returns EncryptAlgorithm.AES
            every { mode() } returns EncryptMode.CBC
            every { padding() } returns EncryptPadding.PKCS5Padding

            // 初始化向量(IV)
            val iv = ByteArray(16) { (0xF0 + it).toByte() }
            every { iv() } returns iv

            // 不把 IV 直接拼到密文前面
            every { composeIV } returns false
        }
    }

    @Test(expected = EncryptException::class)
    fun `wrong key size should throw`() {
        val badConfig = mockk<IEncryptConfig> {
            every { key() } returns ByteArray(5)  // 错误长度
            every { mode() } returns EncryptMode.ECB
            every { algorithm() } returns EncryptAlgorithm.AES
            every { padding() } returns EncryptPadding.PKCS5Padding
            every { iv() } returns null
            every { composeIV } returns false
        }
        AESEncrypt(badConfig)
    }

    @Test
    fun `encryptToHex and decryptFromHex round-trip`() {
        val aes = AESEncrypt(config)
        val plain = "Hello 单元测试!"
        val cipherHex = aes.encryptToHex(plain.toByteArray(StandardCharsets.UTF_8))
        val recovered = aes.decryptFromHex(cipherHex)
        assertEquals(plain, recovered)
    }

    @Test
    fun `encryptToBase64 and decryptFromBase64 round-trip`() {
        val aes = AESEncrypt(config)
        val plain = "Base64 测试 😊"
        val cipherB64 = aes.encryptToBase64(plain.toByteArray(StandardCharsets.UTF_8))
        val recovered = aes.decryptFromBase64(cipherB64)
        assertEquals(plain, recovered)
    }

    @Test
    fun `encrypt and decrypt byte array`() {
        val aes = AESEncrypt(config)
        val data = "ByteArray 测试".toByteArray()
        val cipher = aes.encryptToByteArray(data)
        val recovered = aes.decryptFromByteArray(cipher)
        assertArrayEquals(data, recovered)
    }

    @Test
    fun `encryptStream and decryptStream round-trip`() {
        val aes = AESEncrypt(config)
        val plain = "Stream 测试流式加解密"
        val input = ByteArrayInputStream(plain.toByteArray(StandardCharsets.UTF_8))
        val encryptedOut = ByteArrayOutputStream()
        aes.encryptStream(input, encryptedOut)

        val encryptedBytes = encryptedOut.toByteArray()
        val decryptedOut = ByteArrayOutputStream()
        aes.decryptStream(ByteArrayInputStream(encryptedBytes), decryptedOut)

        val recovered = decryptedOut.toString(StandardCharsets.UTF_8.name())
        assertEquals(plain, recovered)
    }

    @Test
    fun `encryptFile and decryptFromFile round-trip`() {
        val aes = AESEncrypt(config)
        // 1. 创建临时明文文件
        val plainFile = File.createTempFile("plain", ".txt")
        plainFile.writeText("文件加解密测试12345", StandardCharsets.UTF_8)

        // 2. 加密到新文件
        val encFile = File.createTempFile("enc", ".dat")
        aes.encryptFile(plainFile.absolutePath, encFile.absolutePath)

        // 3. 解密到另一个文件
        val decFile = File.createTempFile("dec", ".txt")
        aes.decryptFromFile(encFile.absolutePath, decFile.absolutePath)

        // 4. 验证内容一致
        val recovered = decFile.readText(StandardCharsets.UTF_8)
        assertEquals(plainFile.readText(StandardCharsets.UTF_8), recovered)

        // 清理
        plainFile.delete()
        encFile.delete()
        decFile.delete()
    }

    @Test(expected = EncryptException::class)
    fun `encryptToHex no-padding invalid length should throw`() {
        // 准备 NoPadding 配置
        val noPadConfig = mockk<IEncryptConfig> {
            every { key() } returns config.key()
            every { algorithm() } returns EncryptAlgorithm.AES
            every { mode() } returns EncryptMode.ECB
            every { padding() } returns EncryptPadding.NoPadding
            every { iv() } returns null
            every { composeIV } returns false
        }
        val aesNoPad = AESEncrypt(noPadConfig)
        // 输入长度非 16 的倍数
        aesNoPad.encryptToHex("bad".toByteArray())
    }
}
