package com.sik.sikcore.thread

import android.os.Handler
import com.sik.sikcore.SIKCore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * 线程工具类，用于在主线程和IO线程执行任务。
 * 提供了多种方式来运行协程任务，包括延迟执行和周期性执行。
 * 支持传入指定的 CoroutineScope，如果未传入则使用全局的 CoroutineScope。
 * 每个运行的任务都会分配一个唯一的运行 ID，便于单独取消或取消所有任务。
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

    // 调度器提供者
    private object DispatcherProvider {
        val io: CoroutineDispatcher = Dispatchers.IO
        val main: CoroutineDispatcher = Dispatchers.Main
    }

    // 全局的 CoroutineScope，用于未指定 CoroutineScope 的情况
    private val globalJob = SupervisorJob()
    private val globalScope = CoroutineScope(DispatcherProvider.io + globalJob)

    // 运行 ID 生成器
    private val runIdCounter = AtomicLong(0)

    // 运行 ID 与 Job 的映射，线程安全
    private val jobs = ConcurrentHashMap<String, Job>()

    /**
     * 生成唯一的运行 ID。
     *
     * @return 唯一的运行 ID
     */
    private fun generateRunId(): String {
        return "run-${runIdCounter.incrementAndGet()}"
    }

    /**
     * 在 IO 线程执行挂起函数。
     *
     * @param block 要执行的挂起函数
     * @param scope 可选的 CoroutineScope，如果未传入则使用全局 CoroutineScope
     * @return 运行 ID
     */
    @JvmStatic
    fun runOnIO(
        block: suspend CoroutineScope.() -> Unit,
        scope: CoroutineScope? = null
    ): String {
        val actualScope = scope ?: globalScope
        val runId = generateRunId()
        val job = actualScope.launch(DispatcherProvider.io) {
            try {
                block()
            } finally {
                jobs.remove(runId)
            }
        }
        jobs[runId] = job
        return runId
    }

    /**
     * 在主线程执行挂起函数。
     *
     * @param block 要执行的挂起函数
     * @param scope 可选的 CoroutineScope，如果未传入则使用全局 CoroutineScope
     * @return 运行 ID
     */
    @JvmStatic
    fun runOnMain(
        block: suspend CoroutineScope.() -> Unit,
        scope: CoroutineScope? = null
    ): String {
        val actualScope = scope ?: globalScope
        val runId = generateRunId()
        val job = actualScope.launch(DispatcherProvider.main) {
            try {
                block()
            } finally {
                jobs.remove(runId)
            }
        }
        jobs[runId] = job
        return runId
    }

    /**
     * 在 IO 线程延迟指定时间后执行挂起函数。
     *
     * @param delayTimeMillis 延迟时间（毫秒）
     * @param block 要执行的挂起函数
     * @param scope 可选的 CoroutineScope，如果未传入则使用全局 CoroutineScope
     * @return 运行 ID
     */
    @JvmStatic
    fun runOnIODelayed(
        delayTimeMillis: Long,
        block: suspend CoroutineScope.() -> Unit,
        scope: CoroutineScope? = null
    ): String {
        val actualScope = scope ?: globalScope
        val runId = generateRunId()
        val job = actualScope.launch(DispatcherProvider.io) {
            try {
                delay(delayTimeMillis)
                block()
            } finally {
                jobs.remove(runId)
            }
        }
        jobs[runId] = job
        return runId
    }

    /**
     * 在主线程延迟指定时间后执行挂起函数。
     *
     * @param delayTimeMillis 延迟时间（毫秒）
     * @param block 要执行的挂起函数
     * @param scope 可选的 CoroutineScope，如果未传入则使用全局 CoroutineScope
     * @return 运行 ID
     */
    @JvmStatic
    fun runOnMainDelayed(
        delayTimeMillis: Long,
        block: suspend CoroutineScope.() -> Unit,
        scope: CoroutineScope? = null
    ): String {
        val actualScope = scope ?: globalScope
        val runId = generateRunId()
        val job = actualScope.launch(DispatcherProvider.main) {
            try {
                delay(delayTimeMillis)
                block()
            } finally {
                jobs.remove(runId)
            }
        }
        jobs[runId] = job
        return runId
    }

    /**
     * 在 IO 线程周期性执行挂起函数。
     *
     * @param intervalMillis 执行间隔（毫秒）
     * @param action 每次执行的挂起函数，参数为执行次数
     * @param scope 可选的 CoroutineScope，如果未传入则使用全局 CoroutineScope
     * @return 运行 ID
     */
    @JvmStatic
    fun runOnIODelayedFlow(
        intervalMillis: Long,
        action: suspend (Long) -> Unit,
        scope: CoroutineScope? = null
    ): String {
        val actualScope = scope ?: globalScope
        val runId = generateRunId()
        val job = actualScope.launch(DispatcherProvider.io) {
            try {
                var count = 0L
                while (isActive) {
                    action(count)
                    delay(intervalMillis)
                    count++
                }
            } finally {
                jobs.remove(runId)
            }
        }
        jobs[runId] = job
        return runId
    }

    /**
     * 在主线程周期性执行挂起函数。
     *
     * @param intervalMillis 执行间隔（毫秒）
     * @param action 每次执行的挂起函数，参数为执行次数
     * @param scope 可选的 CoroutineScope，如果未传入则使用全局 CoroutineScope
     * @return 运行 ID
     */
    @JvmStatic
    fun runOnMainDelayedFlow(
        intervalMillis: Long,
        action: suspend (Long) -> Unit,
        scope: CoroutineScope? = null
    ): String {
        val actualScope = scope ?: globalScope
        val runId = generateRunId()
        val job = actualScope.launch(DispatcherProvider.main) {
            try {
                var count = 0L
                while (isActive) {
                    action(count)
                    delay(intervalMillis)
                    count++
                }
            } finally {
                jobs.remove(runId)
            }
        }
        jobs[runId] = job
        return runId
    }

    /**
     * 取消指定运行 ID 的协程。
     *
     * @param runId 需要取消的运行 ID
     * @return 如果取消成功返回 true，否则返回 false
     */
    @JvmStatic
    fun cancel(runId: String): Boolean {
        val job = jobs.remove(runId)
        return if (job != null) {
            job.cancel()
            true
        } else {
            false
        }
    }

    /**
     * 取消所有由 ThreadUtils 启动的协程。
     * 通常在应用程序退出或不再需要这些协程时调用。
     */
    @JvmStatic
    fun cancelAll() {
        // 创建一个快照以避免并发修改异常
        val currentJobs = jobs.toMap()
        currentJobs.forEach { (runId, job) ->
            job.cancel()
            jobs.remove(runId)
        }
    }

    /**
     * 取消全局的 CoroutineScope，停止所有未指定作用域的协程。
     * 通常在应用程序退出时调用。
     */
    @JvmStatic
    fun cancelGlobal() {
        globalJob.cancel()
    }
}
