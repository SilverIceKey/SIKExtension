package com.sik.siknet.tcp.netty.core.manager

import android.util.Log
import com.sik.siknet.tcp.netty.core.common.BaseNettyManager
import com.sik.siknet.tcp.netty.core.common.NettyConfig
import com.sik.siknet.tcp.netty.core.handler.LoggingHandler
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.timeout.IdleStateHandler
import java.util.concurrent.TimeUnit

/**
 * NettyServerManager 处理服务器特定的功能，如启动服务器和管理客户端通道。
 */
class NettyServerManager(config: NettyConfig) : BaseNettyManager(config) {
    /**
     * 启动服务
     */
    override fun startInternal() {
        startServer()
    }

    /**
     * 启动 Netty 服务器，开始监听客户端连接。
     */
    private fun startServer() {
        bossGroup = NioEventLoopGroup(1)
        workerGroup = NioEventLoopGroup(config.threadGroupSize)

        try {
            val bootstrap = ServerBootstrap()
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .childHandler(object : ChannelInitializer<SocketChannel>() {
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
                        config.plugins.forEach { it.install(ch, this@NettyServerManager) }
                    }
                })
            val future = bootstrap.bind(config.host, config.port).sync()
            channel = future.channel()
            Log.i("NettyServerManager", "服务端启动在 ${config.host}:${config.port}")
            future.channel().closeFuture().sync()
        } catch (e: InterruptedException) {
            Log.i("NettyServerManager", "服务端启动失败：${e.message}", e)
            Thread.currentThread().interrupt()
        } finally {
            stop()
        }
    }
}
