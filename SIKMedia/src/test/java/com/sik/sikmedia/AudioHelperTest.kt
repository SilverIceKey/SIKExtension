import com.sik.sikmedia.AudioHelper
import com.sik.sikmedia.AudioRecorderType
import com.sik.sikmedia.AudioRecordImpl
import com.sik.sikmedia.MediaRecorderImpl
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioHelperTest {
    @Test
    fun testSetRecorderType() {
        AudioHelper.instance.setRecorderType(AudioRecorderType.MEDIA_RECORDER)
        val field = AudioHelper::class.java.getDeclaredField("audioRecorder")
        field.isAccessible = true
        assertTrue(field.get(AudioHelper.instance) is MediaRecorderImpl)

        AudioHelper.instance.setRecorderType(AudioRecorderType.AUDIO_RECORD)
        assertTrue(field.get(AudioHelper.instance) is AudioRecordImpl)
    }
}
