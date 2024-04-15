package com.sik.sikcore.thread

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * 线程工具
 */
object ThreadUtils {
    /**
     * 主线程
     */
    private val mainHandler by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        Handler(Looper.getMainLooper())
    }

    /**
     * 主线程
     */
    @JvmStatic
    fun mainHandler(): Handler {
        return mainHandler
    }

    /**
     * Custom scope
     * 自定义作用域
     * @property context
     * @constructor Create empty Custom scope
     */
    private class CustomScope(private val context: CoroutineContext) : CoroutineScope {
        private var job = Job()
        override val coroutineContext: CoroutineContext
            get() = context + job

        fun cancel() {
            job.cancel()
        }
    }

    /**
     * Io scope
     * io作用域
     */
    private val ioScope = CustomScope(Dispatchers.IO)

    /**
     * Main scope
     * 主线程作用域
     */
    private val mainScope = CustomScope(Dispatchers.Main)

    /**
     * Run on i o
     * 运行在io线程
     * @param block
     * @receiver
     */
    @JvmStatic
    fun runOnIO(block: suspend CoroutineScope.() -> Unit) {
        ioScope.launch {
            block()
        }
    }

    /**
     * Run on main
     * 运行在主线程
     * @param block
     * @receiver
     */
    @JvmStatic
    fun runOnMain(block: suspend CoroutineScope.() -> Unit) {
        mainScope.launch {
            block()
        }
    }

    /**
     * Run on i o delayed
     * 运行在io线程并且延迟
     * @param delayTimeMillis
     * @param block
     * @receiver
     */
    @JvmStatic
    fun runOnIODelayed(delayTimeMillis: Long, block: suspend CoroutineScope.() -> Unit) {
        ioScope.launch {
            delay(delayTimeMillis)
            block()
        }
    }

    /**
     * Run on main delayed
     * 运行在主线程并且延迟
     * @param delayTimeMillis
     * @param block
     * @receiver
     */
    @JvmStatic
    fun runOnMainDelayed(delayTimeMillis: Long, block: suspend CoroutineScope.() -> Unit) {
        mainScope.launch {
            delay(delayTimeMillis)
            block()
        }
    }

    /**
     * With i o
     * 切换到io线程
     * @param T
     * @param block
     * @receiver
     * @return
     */
    @JvmStatic
    suspend fun <T> withIO(block: suspend CoroutineScope.() -> T): T {
        return withContext(ioScope.coroutineContext) {
            block()
        }
    }

    /**
     * With main
     * 切换到主线程
     * @param T
     * @param block
     * @receiver
     * @return
     */
    @JvmStatic
    suspend fun <T> withMain(block: suspend CoroutineScope.() -> T): T {
        return withContext(mainScope.coroutineContext) {
            block()
        }
    }

    /**
     * Cancel all
     * 取消所有
     */
    @JvmStatic
    fun cancelAll() {
        ioScope.cancel()
        mainScope.cancel()
    }
}