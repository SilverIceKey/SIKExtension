import android.hardware.SensorEvent
import com.sik.siksensors.SensorMathUtils
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito

class SensorMathUtilsTest {
    private fun mockEvent(vararg values: Float): SensorEvent {
        val event = Mockito.mock(SensorEvent::class.java)
        Mockito.`when`(event.values).thenReturn(values)
        return event
    }

    @Test
    fun testCalculateAccelerationMagnitude() {
        val event = mockEvent(3f, 4f, 0f)
        assertEquals(5f, SensorMathUtils.calculateAccelerationMagnitude(event))
    }
}
