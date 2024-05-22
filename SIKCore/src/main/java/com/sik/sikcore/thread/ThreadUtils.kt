package com.sik.sikcore.thread

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.*

/**
 * 线程工具
 */
object ThreadUtils {
    /**
     * 主线程Handler
     */
    private val mainHandler by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        Handler(Looper.getMainLooper())
    }

    /**
     * 获取主线程Handler
     */
    @JvmStatic
    fun mainHandler(): Handler {
        return mainHandler
    }

    /**
     * 运行在IO线程
     * @param block
     */
    @JvmStatic
    fun runOnIO(block: suspend CoroutineScope.() -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            block()
        }
    }

    /**
     * 运行在主线程
     * @param block
     */
    @JvmStatic
    fun runOnMain(block: suspend CoroutineScope.() -> Unit) {
        GlobalScope.launch(Dispatchers.Main) {
            block()
        }
    }

    /**
     * 运行在IO线程并延迟执行
     * @param delayTimeMillis
     * @param block
     */
    @JvmStatic
    fun runOnIODelayed(delayTimeMillis: Long, block: suspend CoroutineScope.() -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            delay(delayTimeMillis)
            block()
        }
    }

    /**
     * 运行在主线程并延迟执行
     * @param delayTimeMillis
     * @param block
     */
    @JvmStatic
    fun runOnMainDelayed(delayTimeMillis: Long, block: suspend CoroutineScope.() -> Unit) {
        GlobalScope.launch(Dispatchers.Main) {
            delay(delayTimeMillis)
            block()
        }
    }

    /**
     * 在IO线程中运行并返回结果
     * @param block
     * @return
     */
    @JvmStatic
    suspend fun <T> withIO(block: suspend CoroutineScope.() -> T): T {
        return withContext(Dispatchers.IO) {
            block()
        }
    }

    /**
     * 在主线程中运行并返回结果
     * @param block
     * @return
     */
    @JvmStatic
    suspend fun <T> withMain(block: suspend CoroutineScope.() -> T): T {
        return withContext(Dispatchers.Main) {
            block()
        }
    }
}
