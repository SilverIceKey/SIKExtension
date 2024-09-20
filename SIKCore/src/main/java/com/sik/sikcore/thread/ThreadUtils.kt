package com.sik.sikcore.thread

import android.os.Handler
import android.os.Looper
import com.sik.sikcore.SIKCore
import kotlinx.coroutines.*

/**
 * 线程工具类，用于在主线程和IO线程执行任务。
 * 提供了多种方式来运行协程任务，包括延迟执行和周期性执行。
 */
object ThreadUtils {

    // 主线程的Handler
    private val mainHandler: Handler by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        Handler(SIKCore.getApplication().mainLooper)
    }

    /**
     * 获取主线程的Handler。
     *
     * @return 主线程Handler
     */
    @JvmStatic
    fun mainHandler(): Handler = mainHandler

    // 定义一个内部CoroutineScope，使用SupervisorJob以防止一个子协程失败影响其他协程
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * 在IO线程执行挂起函数。
     *
     * @param block 要执行的挂起函数
     */
    @JvmStatic
    fun runOnIO(block: suspend CoroutineScope.() -> Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            block()
        }
    }

    /**
     * 在主线程执行挂起函数。
     *
     * @param block 要执行的挂起函数
     */
    @JvmStatic
    fun runOnMain(block: suspend CoroutineScope.() -> Unit) {
        coroutineScope.launch(Dispatchers.Main) {
            block()
        }
    }

    /**
     * 在IO线程延迟指定时间后执行挂起函数。
     *
     * @param delayTimeMillis 延迟时间（毫秒）
     * @param block 要执行的挂起函数
     */
    @JvmStatic
    fun runOnIODelayed(delayTimeMillis: Long, block: suspend CoroutineScope.() -> Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            delay(delayTimeMillis)
            block()
        }
    }

    /**
     * 在主线程延迟指定时间后执行挂起函数。
     *
     * @param delayTimeMillis 延迟时间（毫秒）
     * @param block 要执行的挂起函数
     */
    @JvmStatic
    fun runOnMainDelayed(delayTimeMillis: Long, block: suspend CoroutineScope.() -> Unit) {
        coroutineScope.launch(Dispatchers.Main) {
            delay(delayTimeMillis)
            block()
        }
    }

    /**
     * 在IO线程执行挂起函数，并返回结果。
     *
     * @param block 要执行的挂起函数
     * @return 挂起函数的结果
     */
    @JvmStatic
    suspend fun <T> withIO(block: suspend CoroutineScope.() -> T): T {
        return withContext(Dispatchers.IO) {
            block()
        }
    }

    /**
     * 在主线程执行挂起函数，并返回结果。
     *
     * @param block 要执行的挂起函数
     * @return 挂起函数的结果
     */
    @JvmStatic
    suspend fun <T> withMain(block: suspend CoroutineScope.() -> T): T {
        return withContext(Dispatchers.Main) {
            block()
        }
    }

    /**
     * 在IO线程周期性执行挂起函数。
     *
     * @param intervalMillis 执行间隔（毫秒）
     * @param action 每次执行的挂起函数，参数为执行次数
     * @return 协程的Job，可以用于取消
     */
    @JvmStatic
    fun runOnIODelayedFlow(intervalMillis: Long, action: suspend (Long) -> Unit): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            var count = 0L
            while (isActive) {
                action(count)
                delay(intervalMillis)
                count++
            }
        }
    }

    /**
     * 在主线程周期性执行挂起函数。
     *
     * @param intervalMillis 执行间隔（毫秒）
     * @param action 每次执行的挂起函数，参数为执行次数
     * @return 协程的Job，可以用于取消
     */
    @JvmStatic
    fun runOnMainDelayedFlow(intervalMillis: Long, action: suspend (Long) -> Unit): Job {
        return coroutineScope.launch(Dispatchers.Main) {
            var count = 0L
            while (isActive) {
                action(count)
                delay(intervalMillis)
                count++
            }
        }
    }

    /**
     * 取消所有由ThreadUtils启动的协程。
     * 通常在应用程序退出或不再需要这些协程时调用。
     */
    @JvmStatic
    fun cancelAll() {
        coroutineScope.cancel()
    }
}
