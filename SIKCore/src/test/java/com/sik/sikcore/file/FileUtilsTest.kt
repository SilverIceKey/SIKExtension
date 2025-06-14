import com.sik.sikcore.file.FileUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class FileUtilsTest {
    @Test
    fun testFormatBytes() {
        assertEquals("500 B", FileUtils.formatBytes(500))
        assertEquals("1.000 KB", FileUtils.formatBytes(1024))
    }
}
