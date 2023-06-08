package com.sik.siknet.netty

import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import java.util.concurrent.ConcurrentHashMap

/**
 * netty帮助类
 */
class NettyClientHelper {

    companion object {
        /**
         * 根据配置存储netty请求
         */
        private val nettyClients: ConcurrentHashMap<String, Channel> =
            ConcurrentHashMap()

        /**
         * 连接中的客户端
         */
        private val nettyConnectingClients: ConcurrentHashMap<String, Bootstrap> =
            ConcurrentHashMap()

        @JvmStatic
        val instance: NettyClientHelper by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            NettyClientHelper()
        }
    }

    /**
     * 连接服务端
     */
    fun <T : NettyConfig> connect(config: T) {
        if (nettyClients[config::class.simpleName ?: "config"] != null ||
            nettyConnectingClients[config::class.simpleName ?: "config"] != null
        ) {
            return
        }
        val client: Bootstrap = Bootstrap()
        nettyConnectingClients[config::class.simpleName ?: "config"] = client
        val eventLoopGroup = NioEventLoopGroup()
        client.group(eventLoopGroup)
            .channel(NioSocketChannel::class.java)
            .handler(object : ChannelInitializer<Channel>(){
                override fun initChannel(ch: Channel) {

                }
            })
    }
}