import android.graphics.Bitmap
import com.sik.sikimage.ImageConvertUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ImageConvertUtilsTest {
    @Test
    fun testBitmapBase64Conversion() {
        val bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
        val base64 = ImageConvertUtils.bitmapToBase64(bitmap)
        val result = ImageConvertUtils.base64ToBitmap(base64)
        assertNotNull(result)
        assertEquals(bitmap.width, result!!.width)
        assertEquals(bitmap.height, result.height)
    }
}
