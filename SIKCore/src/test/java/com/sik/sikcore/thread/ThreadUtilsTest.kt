package com.sik.sikcore.thread

import android.app.Application
import com.sik.sikcore.SIKCore
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class ThreadUtilsTest {

    @Before
    fun setup() {
        val app = Application()
        val field = SIKCore::class.java.getDeclaredField("application")
        field.isAccessible = true
        field.set(null, app)
    }

    @Test
    fun runOnIO_executes() {
        val latch = CountDownLatch(1)
        ThreadUtils.runOnIO { latch.countDown() }
        assertTrue(latch.await(1, TimeUnit.SECONDS))
    }

    @Test
    fun cancelPreventsExecution() {
        val latch = CountDownLatch(1)
        val runId = ThreadUtils.runOnIODelayed(300) { latch.countDown() }
        ThreadUtils.cancel(runId)
        assertFalse(latch.await(600, TimeUnit.MILLISECONDS))
    }
}
