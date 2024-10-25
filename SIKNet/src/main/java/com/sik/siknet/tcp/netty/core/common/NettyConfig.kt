package com.sik.siknet.tcp.netty.core.common

import io.netty.channel.socket.SocketChannel


/**
 * Netty 配置抽象类
 */
abstract class NettyConfig {
    enum class Mode {
        CLIENT, SERVER
    }

    /**
     * 获取主机地址
     */
    abstract val host: String?

    /**
     * 获取端口号
     */
    abstract val port: Int

    /**
     * 获取运行模式（客户端或服务端）
     */
    abstract val mode: Mode?

    /**
     * 通道新建
     *
     * @param ch
     */
    abstract fun channelInit(ch: SocketChannel)

    open val threadGroupSize: Int
        /**
         * 获取线程池大小，默认使用 CPU 核心数的 2 倍
         */
        get() = Runtime.getRuntime().availableProcessors() * 2

    open val maxReconnectAttempts: Int
        /**
         * 获取重连次数，默认 5 次
         */
        get() = -1

    open val reconnectInterval: Long
        /**
         * 获取重连间隔（秒），默认 5 秒
         */
        get() = 5

    open val heartbeatInterval: Long
        /**
         * 获取心跳间隔（秒），默认 30 秒
         */
        get() = 30

    open val isAutoSwitchThread: Boolean
        /**
         * 是否启用自动线程切换，默认 true
         */
        get() = true
}
