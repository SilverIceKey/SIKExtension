package com.sik.sikcore.thread

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.*

object ThreadUtils {
    private val mainHandler by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        Handler(Looper.getMainLooper())
    }

    @JvmStatic
    fun mainHandler(): Handler {
        return mainHandler
    }

    @JvmStatic
    fun runOnIO(block: suspend CoroutineScope.() -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            block()
        }
    }

    @JvmStatic
    fun runOnMain(block: suspend CoroutineScope.() -> Unit) {
        GlobalScope.launch(Dispatchers.Main) {
            block()
        }
    }

    @JvmStatic
    fun runOnIODelayed(delayTimeMillis: Long, block: suspend CoroutineScope.() -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            delay(delayTimeMillis)
            block()
        }
    }

    @JvmStatic
    fun runOnMainDelayed(delayTimeMillis: Long, block: suspend CoroutineScope.() -> Unit) {
        GlobalScope.launch(Dispatchers.Main) {
            delay(delayTimeMillis)
            block()
        }
    }

    @JvmStatic
    suspend fun <T> withIO(block: suspend CoroutineScope.() -> T): T {
        return withContext(Dispatchers.IO) {
            block()
        }
    }

    @JvmStatic
    suspend fun <T> withMain(block: suspend CoroutineScope.() -> T): T {
        return withContext(Dispatchers.Main) {
            block()
        }
    }

    @JvmStatic
    fun runOnIODelayedFlow(intervalMillis: Long, action: suspend (Long) -> Unit): Job {
        return GlobalScope.launch(Dispatchers.IO) {
            var count = 0L
            while (isActive) {
                action(count)
                delay(intervalMillis)
                count++
            }
        }
    }

    @JvmStatic
    fun runOnMainDelayedFlow(intervalMillis: Long, action: suspend (Long) -> Unit): Job {
        return GlobalScope.launch(Dispatchers.Main) {
            var count = 0L
            while (isActive) {
                action(count)
                delay(intervalMillis)
                count++
            }
        }
    }
}
