package com.sik.sikcore.thread

import android.os.Handler
import android.os.Looper
import com.sik.sikcore.SIKCore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

/**
 * ================================================================
 *  ThreadUtils.configurable.kt
 *
 *  背景：你希望不要靠 `typealias` 在编译期切换，而是**运行期可配置**。
 *  设计：
 *   - 暴露一个稳定的接口 `ThreadUtilsApi`（与原 API 等价）。
 *   - `ThreadUtils` 是门面（facade），内部持有可切换的实现（协程版 / 线程版）。
 *   - 通过 `ThreadUtils.configure(backend, options)` 在 **Application.onCreate** 早期调用即可。
 *   - 仍保留主线程 Handler、取消、周期任务等能力；所有方法签名与原版一致，完全可替换。
 *
 *  选择策略：
 *   - Backend.COROUTINES（默认）：结构化并发，推荐大多数场景。
 *   - Backend.THREADS：更贴近经典线程模型；内部用 `runBlocking` 桥接 `suspend` API（不依赖 coroutines-android，仅 coroutines-core）。
 *
 *  线程/硬件友好增强：协程版包含可选的“设备专线”单线程调度器（Options.useDeviceDispatcher）。
 * ================================================================
 */

// ======================== 对外可见：配置与门面 ========================

/** 运行期可切换的后端实现 */
enum class Backend { COROUTINES, THREADS }

/** 可选参数 */
data class Options(
    /** 协程版：是否提供单线程“设备专线”调度器并用于 IO（串行硬件 I/O 建议开启） */
    val useDeviceDispatcher: Boolean = false,
    /** 设备专线线程名 */
    val deviceThreadName: String = "DeviceWorker",
    /** 线程版：IO 线程池大小上限（0 表示自动） */
    val threadsIoMax: Int = 0
)

/** 与原始 API 等价的稳定接口 */
interface ThreadUtilsApi {
    fun mainHandler(): Handler

    fun runOnIO(
        scope: CoroutineScope? = null,
        block: suspend CoroutineScope.() -> Unit
    ): String

    fun runOnMain(
        scope: CoroutineScope? = null,
        block: suspend CoroutineScope.() -> Unit
    ): String

    fun runOnIODelayed(
        delayTimeMillis: Long,
        scope: CoroutineScope? = null,
        block: suspend CoroutineScope.() -> Unit
    ): String

    fun runOnMainDelayed(
        delayTimeMillis: Long,
        scope: CoroutineScope? = null,
        block: suspend CoroutineScope.() -> Unit
    ): String

    fun runOnIODelayedFlow(
        intervalMillis: Long,
        scope: CoroutineScope? = null,
        action: suspend (Long) -> Unit
    ): String

    fun runOnMainDelayedFlow(
        intervalMillis: Long,
        scope: CoroutineScope? = null,
        action: suspend (Long) -> Unit
    ): String

    fun cancel(runId: String): Boolean
    fun cancelAll()
    fun cancelGlobal()
}

/**
 * 门面：对外仍然叫 ThreadUtils。内部以可变实现委托，支持运行期切换。
 * 使用方式：在 Application.onCreate() 中调用：
 *   ThreadUtils.configure(Backend.THREADS) // 或默认 COROUTINES
 */
object ThreadUtils : ThreadUtilsApi {

    // 默认使用协程后端
    @Volatile private var impl: ThreadUtilsApi = ThreadUtilsCoroutines()

    @Synchronized
    fun configure(backend: Backend, options: Options = Options()) {
        // 切换实现：先清理旧实现，再替换
        impl.cancelAll()
        impl.cancelGlobal()
        impl = when (backend) {
            Backend.COROUTINES -> ThreadUtilsCoroutines(options)
            Backend.THREADS -> ThreadUtilsThreads(options)
        }
    }

    // ========== 委托到当前实现 ==========
    override fun mainHandler(): Handler = impl.mainHandler()
    override fun runOnIO(scope: CoroutineScope?, block: suspend CoroutineScope.() -> Unit) =
        impl.runOnIO(scope, block)
    override fun runOnMain(scope: CoroutineScope?, block: suspend CoroutineScope.() -> Unit) =
        impl.runOnMain(scope, block)
    override fun runOnIODelayed(delayTimeMillis: Long, scope: CoroutineScope?, block: suspend CoroutineScope.() -> Unit) =
        impl.runOnIODelayed(delayTimeMillis, scope, block)
    override fun runOnMainDelayed(delayTimeMillis: Long, scope: CoroutineScope?, block: suspend CoroutineScope.() -> Unit) =
        impl.runOnMainDelayed(delayTimeMillis, scope, block)
    override fun runOnIODelayedFlow(intervalMillis: Long, scope: CoroutineScope?, action: suspend (Long) -> Unit) =
        impl.runOnIODelayedFlow(intervalMillis, scope, action)
    override fun runOnMainDelayedFlow(intervalMillis: Long, scope: CoroutineScope?, action: suspend (Long) -> Unit) =
        impl.runOnMainDelayedFlow(intervalMillis, scope, action)
    override fun cancel(runId: String) = impl.cancel(runId)
    override fun cancelAll() = impl.cancelAll()
    override fun cancelGlobal() = impl.cancelGlobal()
}

// ======================== 实现 A：协程后端 ========================

private class ThreadUtilsCoroutines(private val options: Options = Options()) : ThreadUtilsApi {

    private val appMainHandler: Handler by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        Handler(SIKCore.getApplication().mainLooper)
    }

    override fun mainHandler(): Handler = appMainHandler

    /** 调度器集合 */
    private class DispatchersBox(options: Options) {
        val main: CoroutineDispatcher = Dispatchers.Main
        val io: CoroutineDispatcher
        private val deviceDispatcherOrNull: CoroutineDispatcher?
        private val deviceExecutorOrNull = if (options.useDeviceDispatcher) {
            Executors.newSingleThreadExecutor(namedFactory(options.deviceThreadName))
        } else null

        init {
            deviceDispatcherOrNull = deviceExecutorOrNull?.asCoroutineDispatcher()
            io = deviceDispatcherOrNull ?: Dispatchers.IO
        }

        fun close() {
            deviceExecutorOrNull?.shutdownNow()
        }
    }

    private val box = DispatchersBox(options)
    private val globalJob = SupervisorJob()
    private val globalScope = CoroutineScope(box.io + globalJob + CoroutineName("ThreadUtilsGlobal"))

    private val idCounter = AtomicLong(0)
    private val jobs = ConcurrentHashMap<String, Job>()

    private fun nextId(): String = "run-${idCounter.incrementAndGet()}"

    override fun runOnIO(scope: CoroutineScope?, block: suspend CoroutineScope.() -> Unit): String {
        val actual = scope ?: globalScope
        val id = nextId()
        val job = actual.launch(box.io + CoroutineName(id)) {
            try { block() } finally { jobs.remove(id) }
        }
        jobs[id] = job; return id
    }

    override fun runOnMain(scope: CoroutineScope?, block: suspend CoroutineScope.() -> Unit): String {
        val actual = scope ?: globalScope
        val id = nextId()
        val job = actual.launch(box.main + CoroutineName(id)) {
            try { block() } finally { jobs.remove(id) }
        }
        jobs[id] = job; return id
    }

    override fun runOnIODelayed(delayTimeMillis: Long, scope: CoroutineScope?, block: suspend CoroutineScope.() -> Unit): String {
        val actual = scope ?: globalScope
        val id = nextId()
        val job = actual.launch(box.io + CoroutineName(id)) {
            try { delay(delayTimeMillis); block() } finally { jobs.remove(id) }
        }
        jobs[id] = job; return id
    }

    override fun runOnMainDelayed(delayTimeMillis: Long, scope: CoroutineScope?, block: suspend CoroutineScope.() -> Unit): String {
        val actual = scope ?: globalScope
        val id = nextId()
        val job = actual.launch(box.main + CoroutineName(id)) {
            try { delay(delayTimeMillis); block() } finally { jobs.remove(id) }
        }
        jobs[id] = job; return id
    }

    override fun runOnIODelayedFlow(intervalMillis: Long, scope: CoroutineScope?, action: suspend (Long) -> Unit): String {
        val actual = scope ?: globalScope
        val id = nextId()
        val job = actual.launch(box.io + CoroutineName(id)) {
            try {
                var count = 0L
                while (isActive) {
                    action(count)
                    delay(intervalMillis)
                    count++
                }
            } finally { jobs.remove(id) }
        }
        jobs[id] = job; return id
    }

    override fun runOnMainDelayedFlow(intervalMillis: Long, scope: CoroutineScope?, action: suspend (Long) -> Unit): String {
        val actual = scope ?: globalScope
        val id = nextId()
        val job = actual.launch(box.main + CoroutineName(id)) {
            try {
                var count = 0L
                while (isActive) {
                    action(count)
                    delay(intervalMillis)
                    count++
                }
            } finally { jobs.remove(id) }
        }
        jobs[id] = job; return id
    }

    override fun cancel(runId: String): Boolean {
        val job = jobs.remove(runId) ?: return false
        job.cancel(); return true
    }

    override fun cancelAll() {
        jobs.keys.toList().forEach { id -> jobs.remove(id)?.cancel() }
    }

    override fun cancelGlobal() {
        globalJob.cancel()
        box.close()
    }

    companion object {
        private fun namedFactory(name: String): ThreadFactory = ThreadFactory { r ->
            Thread(r, name).apply { isDaemon = true }
        }
    }
}

// ======================== 实现 B：纯线程后端 ========================

private class ThreadUtilsThreads(private val options: Options = Options()) : ThreadUtilsApi {

    private val appMainHandler: Handler by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        val looper: Looper = SIKCore.getApplication().mainLooper
        Handler(looper)
    }

    override fun mainHandler(): Handler = appMainHandler

    private val idCounter = AtomicLong(0)
    private fun nextId(): String = "run-${idCounter.incrementAndGet()}"

    private val tasks = ConcurrentHashMap<String, Any /* Runnable | ScheduledFuture<*> */>()

    private val ioPool = Executors.newCachedThreadPool(namedFactory("IO-Worker"))
    private val scheduler = ScheduledThreadPoolExecutor(
        calcPoolSize(options.threadsIoMax), namedFactory("IO-Scheduler")
    ).apply { setRemoveOnCancelPolicy(true) }

    override fun runOnIO(scope: CoroutineScope?, block: suspend CoroutineScope.() -> Unit): String {
        val id = nextId()
        val runnable = Runnable {
            try { runBlocking { block(CoroutineScope(kotlin.coroutines.EmptyCoroutineContext)) } } catch (_: Throwable) {}
        }
        ioPool.execute(runnable)
        tasks[id] = runnable
        return id
    }

    override fun runOnMain(scope: CoroutineScope?, block: suspend CoroutineScope.() -> Unit): String {
        val id = nextId()
        val runnable = Runnable {
            try { runBlocking { block(CoroutineScope(kotlin.coroutines.EmptyCoroutineContext)) } } catch (_: Throwable) {}
        }
        appMainHandler.post(runnable)
        tasks[id] = runnable
        return id
    }

    override fun runOnIODelayed(delayTimeMillis: Long, scope: CoroutineScope?, block: suspend CoroutineScope.() -> Unit): String {
        val id = nextId()
        val future = scheduler.schedule({
            try { runBlocking { block(CoroutineScope(kotlin.coroutines.EmptyCoroutineContext)) } } catch (_: Throwable) {}
        }, delayTimeMillis, TimeUnit.MILLISECONDS)
        tasks[id] = future
        return id
    }

    override fun runOnMainDelayed(delayTimeMillis: Long, scope: CoroutineScope?, block: suspend CoroutineScope.() -> Unit): String {
        val id = nextId()
        val runnable = Runnable {
            try { runBlocking { block(CoroutineScope(kotlin.coroutines.EmptyCoroutineContext)) } } catch (_: Throwable) {}
        }
        appMainHandler.postDelayed(runnable, delayTimeMillis)
        tasks[id] = runnable
        return id
    }

    override fun runOnIODelayedFlow(intervalMillis: Long, scope: CoroutineScope?, action: suspend (Long) -> Unit): String {
        val id = nextId()
        val future = scheduler.scheduleAtFixedRate(object : Runnable {
            var count = 0L
            override fun run() {
                try { runBlocking { action(count) } } catch (_: Throwable) {}
                count++
            }
        }, 0L, intervalMillis, TimeUnit.MILLISECONDS)
        tasks[id] = future
        return id
    }

    override fun runOnMainDelayedFlow(intervalMillis: Long, scope: CoroutineScope?, action: suspend (Long) -> Unit): String {
        val id = nextId()
        val runnable = object : Runnable {
            var count = 0L
            override fun run() {
                try { runBlocking { action(count) } } catch (_: Throwable) {}
                count++
                appMainHandler.postDelayed(this, intervalMillis)
            }
        }
        appMainHandler.post(runnable)
        tasks[id] = runnable
        return id
    }

    override fun cancel(runId: String): Boolean {
        val v = tasks.remove(runId) ?: return false
        return when (v) {
            is ScheduledFuture<*> -> v.cancel(true)
            is Runnable -> appMainHandler.removeCallbacks(v).let { true }
            else -> false
        }
    }

    override fun cancelAll() { tasks.keys.toList().forEach { cancel(it) } }

    override fun cancelGlobal() {
        cancelAll()
        // 如确需彻底释放：
        // scheduler.shutdownNow(); ioPool.shutdownNow()
    }

    private fun calcPoolSize(max: Int): Int {
        if (max > 0) return max
        val cpu = Runtime.getRuntime().availableProcessors()
        return maxOf(2, cpu / 2)
    }

    companion object {
        private fun namedFactory(prefix: String): ThreadFactory = object : ThreadFactory {
            private val idx = AtomicLong(0)
            override fun newThread(r: Runnable): Thread = Thread(r, "$prefix-${idx.incrementAndGet()}").apply {
                isDaemon = true
            }
        }
    }
}
