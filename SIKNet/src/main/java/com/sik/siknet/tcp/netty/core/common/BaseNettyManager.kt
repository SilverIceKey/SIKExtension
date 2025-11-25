package com.sik.siknet.tcp.netty.core.common

import android.util.Log
import io.netty.channel.Channel
import io.netty.channel.EventLoopGroup
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean


/**
 * BaseNettyManager 是一个抽象类，提供了 Netty 客户端和服务器管理器的公共功能。
 */
abstract class BaseNettyManager(protected val config: NettyConfig) {
    protected var bossGroup: EventLoopGroup? = null
    protected var workerGroup: EventLoopGroup? = null
    protected var channel: Channel? = null
    protected var isManualDisconnect: Boolean = false // 标志主动断开连接
    private var executor: ExecutorService? = null // 用于线程管理

    private val started = AtomicBoolean(false) //用于标记是否已经启动

    /**
     * 构造函数，初始化 Netty 配置和线程池。
     *
     * @param config Netty 配置对象
     */
    init {
        // 如果启用了自动线程切换，则初始化线程池
        if (config.isAutoSwitchThread) {
            executor = Executors.newSingleThreadExecutor()
        }
    }

    /**
     * 启动 Netty 客户端或服务器。
     */
    fun start() {
        if (!started.compareAndSet(false, true)) {
            Log.w("BaseNettyManager", "Already started, ignore duplicate start()")
            return
        }
        if (config.isAutoSwitchThread) {
            executor!!.submit { this.startInternal() }
            config.plugins.forEach { it.onStarted(this@BaseNettyManager) }
        } else {
            startInternal()
            config.plugins.forEach { it.onStarted(this@BaseNettyManager) }
        }
    }

    /**
     * 内部启动方法，由子类实现。
     */
    protected abstract fun startInternal()

    /**
     * 停止 Netty 客户端或服务器。
     */
    fun stop() {
        try {
            isManualDisconnect = true
            config.plugins.forEach { it.onStopping(this@BaseNettyManager) }

            if (executor != null && !executor!!.isShutdown) {
                executor!!.shutdown()
                if (!executor!!.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor!!.shutdownNow()
                }
            }

            if (channel != null && channel!!.isOpen) {
                channel!!.close().sync()
            }

            if (bossGroup != null) {
                bossGroup!!.shutdownGracefully().sync()
            }
            if (workerGroup != null) {
                workerGroup!!.shutdownGracefully().sync()
            }

            Log.i("BaseNettyManager", "Netty 已停止")
        } catch (e: InterruptedException) {
            Log.e("BaseNettyManager", "停止时出错：${e.message}", e)
            Thread.currentThread().interrupt()
        } finally {
            started.set(false)
        }
    }
}
