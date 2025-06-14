import android.graphics.Matrix
import com.sik.sikimage.MatrixUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class MatrixUtilsTest {
    @Test
    fun testRotateMatrixToAngle() {
        val matrix = Matrix()
        MatrixUtils.rotateMatrixToAngle(matrix, 0f, 0f, 90f)
        val angle = MatrixUtils.getRotationAngleFromMatrix(matrix)
        assertEquals(90f, angle)
    }
}
