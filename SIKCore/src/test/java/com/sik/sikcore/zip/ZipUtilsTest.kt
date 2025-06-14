import com.sik.sikcore.zip.ZipUtils
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ZipUtilsTest {
    @Test
    fun testZipAndUnzip() {
        val tempDir = createTempDir()
        val file1 = File(tempDir, "file1.txt").apply { writeText("hello") }
        val file2 = File(tempDir, "file2.txt").apply { writeText("world") }
        val zipFile = File(tempDir, "archive.zip")
        ZipUtils.zip(file1, file2, destFile = zipFile)
        val destDir = File(tempDir, "unzipped")
        ZipUtils.unzip(zipFile, destDir)
        assertTrue(File(destDir, "file1.txt").exists())
        assertTrue(File(destDir, "file2.txt").exists())
    }
}
