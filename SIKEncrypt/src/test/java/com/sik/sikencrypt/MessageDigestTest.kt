import com.sik.sikencrypt.message_digest.MD5MessageDigest
import com.sik.sikencrypt.message_digest.SHA256MessageDigest
import com.sik.sikencrypt.message_digest.SM3MessageDigest
import org.junit.Assert.assertEquals
import org.junit.Test

class MessageDigestTest {
    @Test
    fun testMD5() {
        val md5 = MD5MessageDigest()
        assertEquals("900150983cd24fb0d6963f7d28e17f72", md5.digestToHex("abc".toByteArray()))
    }

    @Test
    fun testSHA256() {
        val sha256 = SHA256MessageDigest()
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            sha256.digestToHex("abc".toByteArray())
        )
    }

    @Test
    fun testSM3() {
        val sm3 = SM3MessageDigest()
        assertEquals(
            "66c7f0f462eeedd9d1f2d46bdc10e4e24167c4875cf2f7a2297da02b8f4ba8e0",
            sm3.digestToHex("abc".toByteArray())
        )
    }
}
