package com.sik.siknet.tcp.netty.core.manager

import com.sik.siknet.tcp.netty.core.common.BaseNettyManager
import com.sik.siknet.tcp.netty.core.common.NettyConfig
import com.sik.siknet.tcp.netty.core.handler.LoggingHandler
import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.timeout.IdleStateHandler
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * NettyClientManager 处理客户端特定的功能，如连接和重连服务器。
 */
class NettyClientManager(config: NettyConfig) : BaseNettyManager(config) {
    /**
     * 重连次数
     */
    private var reconnectAttempts = 0

    /**
     * 是否在重连
     */
    private var isInReconnect = false

    /**
     * 重连任务句柄
     */
    private var reconnectFuture: ScheduledFuture<*>? = null

    override fun startInternal() {
        startClient()
    }

    /**
     * 启动 Netty 客户端并尝试连接服务器。
     */
    private fun startClient() {
        workerGroup = NioEventLoopGroup(config.threadGroupSize)

        try {
            val bootstrap = Bootstrap()
            bootstrap.group(workerGroup)
                .channel(NioSocketChannel::class.java)
                .handler(object : ChannelInitializer<SocketChannel>() {
                    @Throws(Exception::class)
                    override fun initChannel(ch: SocketChannel) {
                        ch.pipeline().addLast(LoggingHandler())
                        ch.pipeline().addLast(
                            IdleStateHandler(
                                config.heartbeatInterval,
                                config.heartbeatInterval,
                                config.heartbeatInterval,
                                TimeUnit.SECONDS
                            )
                        )
                        config.channelInit(ch)
                        config.plugins.forEach { it.install(ch, this@NettyClientManager) }
                    }
                })

            connect(bootstrap)
        } catch (e: Exception) {
            logger.error("客户端启动失败：{}", e.message)
        }
    }

    /**
     * 使用提供的 Bootstrap 尝试连接服务器。
     *
     * @param bootstrap Bootstrap 实例，用于客户端配置
     */
    private fun connect(bootstrap: Bootstrap) {
        bootstrap.connect(config.host, config.port)
            .addListener(ChannelFutureListener { future: ChannelFuture ->
                if (future.isSuccess) {
                    logger.info(" 已连接到 {}:{}", config.host, config.port)
                    channel = future.channel()
                    reconnectAttempts = 0
                    channel?.closeFuture()?.addListener {
                        if (!isManualDisconnect) {
                            reconnect(bootstrap)
                        } else {
                            logger.info("客户端已主动断开连接，不进行重连。")
                        }
                    }
                } else {
                    logger.info("连接失败：{}", future.cause().message)
                    reconnect(bootstrap)
                }
            })
    }

    /**
     * 在连接丢失后尝试重连服务器。
     *
     * @param bootstrap Bootstrap 实例，用于客户端配置
     */
    private fun reconnect(bootstrap: Bootstrap) {
        if (isInReconnect) return
        isInReconnect = true

        reconnectFuture?.cancel(false)
        // 保险：确保旧 channel 不再占着
        channel?.takeIf { it.isOpen }?.close()

        if (config.maxReconnectAttempts == -1 || reconnectAttempts < config.maxReconnectAttempts) {
            reconnectAttempts++
            logger.info("尝试第 {} 次重连...", reconnectAttempts)
            reconnectFuture = workerGroup!!.schedule({
                isInReconnect = false
                connect(bootstrap)
            }, config.reconnectInterval, TimeUnit.SECONDS)
        } else {
            isInReconnect = false
            logger.info("达到最大重连次数，停止重连。")
            stop()
        }
    }
}
