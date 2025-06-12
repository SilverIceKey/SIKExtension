package com.sik.sikcore.device

import android.os.Build
import com.sik.sikcore.shell.ShellResult
import com.sik.sikcore.shell.ShellUtils
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class DeviceUtilsTest {

    private var originalTags: String? = null

    @Before
    fun setUp() {
        originalTags = Build.TAGS
    }

    @After
    fun tearDown() {
        setBuildTags(originalTags)
        unmockkAll()
    }

    private fun setBuildTags(value: String?) {
        val field = Build::class.java.getDeclaredField("TAGS").apply { isAccessible = true }
        field.set(null, value)
    }

    @Test
    fun isDeviceRooted_falseByDefault() {
        setBuildTags("release-keys")
        mockkObject(ShellUtils) {
            every { ShellUtils.execCmd(any(), any(), any()) } returns ShellResult(0, "", "")
            assertFalse(DeviceUtils.isDeviceRooted())
        }
    }

    @Test
    fun isDeviceRooted_trueWhenTestKeys() {
        setBuildTags("test-keys")
        mockkObject(ShellUtils) {
            every { ShellUtils.execCmd(any(), any(), any()) } returns ShellResult(0, "", "")
            assertTrue(DeviceUtils.isDeviceRooted())
        }
    }
}
