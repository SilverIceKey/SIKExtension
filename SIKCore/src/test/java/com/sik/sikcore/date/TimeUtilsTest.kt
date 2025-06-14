import com.sik.sikcore.date.TimeUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class TimeUtilsTest {
    @Test
    fun testLeapYear() {
        assertEquals(true, TimeUtils.isLeapYear(2000))
        assertEquals(false, TimeUtils.isLeapYear(1900))
    }
}
