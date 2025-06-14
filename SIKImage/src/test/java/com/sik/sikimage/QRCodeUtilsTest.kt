import com.sik.sikimage.QRCodeUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class QRCodeUtilsTest {
    @Test
    fun testCreateAndRead() {
        val text = "hello"
        val qr = QRCodeUtils.createQRCode(text, 200)
        val result = QRCodeUtils.readQRCodeString(qr)
        assertEquals(text, result)
    }
}
