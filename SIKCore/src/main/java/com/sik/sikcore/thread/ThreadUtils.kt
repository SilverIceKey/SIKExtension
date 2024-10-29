package com.sik.sikcore.thread

import android.os.Handler
import com.sik.sikcore.SIKCore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 线程工具类，用于在主线程和IO线程执行任务。
 * 提供了多种方式来运行协程任务，包括延迟执行和周期性执行。
 */
object ThreadUtils {

    // 主线程的 Handler
    private val mainHandler: Handler by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        Handler(SIKCore.getApplication().mainLooper)
    }

    /**
     * 获取主线程的 Handler。
     *
     * @return 主线程 Handler
     */
    @JvmStatic
    fun mainHandler(): Handler = mainHandler

    // 内部调度器提供者，集中管理调度器的使用
    private object DispatcherProvider {
        val io: CoroutineDispatcher = Dispatchers.IO
        val main: CoroutineDispatcher = Dispatchers.Main
    }

    // 定义独立的 CoroutineScopes，分别用于 IO 和 Main 线程
    @Volatile
    private var ioScope: CoroutineScope = CoroutineScope(SupervisorJob() + DispatcherProvider.io)

    @Volatile
    private var mainScope: CoroutineScope =
        CoroutineScope(SupervisorJob() + DispatcherProvider.main)

    /**
     * 在 IO 线程执行挂起函数。
     *
     * @param block 要执行的挂起函数
     */
    @JvmStatic
    fun runOnIO(block: suspend CoroutineScope.() -> Unit) {
        ioScope.launch {
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
        mainScope.launch {
            block()
        }
    }

    /**
     * 在 IO 线程延迟指定时间后执行挂起函数。
     *
     * @param delayTimeMillis 延迟时间（毫秒）
     * @param block 要执行的挂起函数
     */
    @JvmStatic
    fun runOnIODelayed(delayTimeMillis: Long, block: suspend CoroutineScope.() -> Unit) {
        ioScope.launch {
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
        mainScope.launch {
            delay(delayTimeMillis)
            block()
        }
    }

    /**
     * 在 IO 线程周期性执行挂起函数。
     *
     * @param intervalMillis 执行间隔（毫秒）
     * @param action 每次执行的挂起函数，参数为执行次数
     * @return 协程的 Job，可以用于取消
     */
    @JvmStatic
    fun runOnIODelayedFlow(intervalMillis: Long, action: suspend (Long) -> Unit): Job {
        return ioScope.launch {
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
     * @return 协程的 Job，可以用于取消
     */
    @JvmStatic
    fun runOnMainDelayedFlow(intervalMillis: Long, action: suspend (Long) -> Unit): Job {
        return mainScope.launch {
            var count = 0L
            while (isActive) {
                action(count)
                delay(intervalMillis)
                count++
            }
        }
    }

    /**
     * 取消所有由 ThreadUtils 启动的协程。
     * 通常在应用程序退出或不再需要这些协程时调用。
     */
    @JvmStatic
    fun cancelAll() {
        ioScope.cancel()
        mainScope.cancel()
        // 重新初始化 CoroutineScopes
        ioScope = CoroutineScope(SupervisorJob() + DispatcherProvider.io)
        mainScope = CoroutineScope(SupervisorJob() + DispatcherProvider.main)
    }
}
