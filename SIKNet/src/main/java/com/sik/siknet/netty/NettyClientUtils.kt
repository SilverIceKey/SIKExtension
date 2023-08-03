package com.sik.siknet.netty

import com.sik.sikcore.log.LogUtils
import com.sik.sikcore.thread.ThreadUtils
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent
import io.netty.handler.timeout.IdleStateHandler
import java.util.concurrent.ConcurrentHashMap


/**
 * netty帮助类
 */
class NettyClientUtils {

    companion object {
        /**
         * 根据配置存储netty请求
         */
        private val nettyClients: ConcurrentHashMap<String, Channel?> =
            ConcurrentHashMap()

        /**
         * 连接中的客户端
         */
        private val nettyConnectingClients: ConcurrentHashMap<String, Bootstrap?> =
            ConcurrentHashMap()

        @JvmStatic
        val instance: NettyClientUtils by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            NettyClientUtils()
        }

        /**
         * 当前重试次数
         */
        @JvmStatic
        @Volatile
        private var currentRetryTimes: Int = 0

        /**
         * 根据配置获取通道
         */
        @JvmStatic
        fun getChannel(configName: String): Channel? {
            return nettyClients[configName]
        }
    }

    /**
     * 连接服务端
     */
    @JvmOverloads
    fun <T : NettyConfig<*>> connect(config: T, isFirstConnect: Boolean = true) {
        if (nettyClients[config::class.simpleName ?: "config"] != null ||
            nettyConnectingClients[config::class.simpleName ?: "config"] != null
        ) {
            return
        }
        if (isFirstConnect) {
            currentRetryTimes = 0
        }
        val client = Bootstrap()
        client.remoteAddress(config.host, config.port)
        nettyConnectingClients[config::class.simpleName ?: "config"] = client
        val eventLoopGroup = NioEventLoopGroup()
        client.group(eventLoopGroup)
            .channel(NioSocketChannel::class.java)
            .handler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    config.initChannel(ch)
                    val channelPipe = ch.pipeline()
                    channelPipe.addLast(IdleStateHandler(60, 0, 0))
                    channelPipe.addLast(object : ChannelDuplexHandler() {
                        override fun channelInactive(ctx: ChannelHandlerContext) {
                            nettyClients.remove(config::class.simpleName ?: "config")
                            nettyConnectingClients.remove(config::class.simpleName ?: "config")
                            ctx.close()
                        }

                        override fun userEventTriggered(ctx: ChannelHandlerContext?, evt: Any?) {
                            super.userEventTriggered(ctx, evt)
                            if (evt is IdleStateEvent) {
                                // 这是一个空闲状态事件
                                if (evt.state() == IdleState.READER_IDLE) {
                                    // 读空闲，可能对方已经断开连接
                                    LogUtils.logger.i("读空闲，关闭连接")
                                    ctx!!.close()
                                }
                            }
                        }

                        override fun exceptionCaught(
                            ctx: ChannelHandlerContext,
                            cause: Throwable?
                        ) {
                            nettyClients.remove(config::class.simpleName ?: "config")
                            nettyConnectingClients.remove(config::class.simpleName ?: "config")
                            ctx.close()
                        }
                    })
                    config.sender?.let {
                        channelPipe.addLast("Sender", config.sender)
                    }
                    config.decoder?.let {
                        channelPipe.addLast("Decoder", config.decoder)
                    }
                    config.process?.let {
                        channelPipe.addLast("Process", config.process)
                    }
                    config.encoder?.let {
                        channelPipe.addLast("Encoder", config.encoder)
                    }
                }
            })
        val channelFeature = client.connect()
        channelFeature.addListener {
            if (channelFeature.isSuccess) {
                nettyClients[config::class.simpleName ?: "config"] = channelFeature.channel()
                config.connectSuccess()
            } else {
                if (config.retryTimes != -1 && currentRetryTimes >= config.retryTimes) {
                    LogUtils.logger.i("连接失败，连接已到达连接次数")
                    return@addListener
                }
                ThreadUtils.mainHandler().postDelayed({
                    connect(config)
                    LogUtils.logger.i("连接失败，当前连接次数第${currentRetryTimes + 1}次")
                    currentRetryTimes++
                }, config.retryTime)
            }
        }
    }
}