package com.sik.siknet.tcp.netty.ws

import com.sik.siknet.tcp.netty.core.common.NettyConfig
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler

class WebSocketServerConfig(
    override val host: String?,
    override val port: Int,
    private val path: String = "/ws",
    override val heartbeatInterval: Long = 30,
    override val isAutoSwitchThread: Boolean = true
) : NettyConfig() {

    override val mode: Mode? = Mode.SERVER

    override fun channelInit(ch: SocketChannel) {
        ch.pipeline().apply {
            addLast(HttpServerCodec())
            addLast(HttpObjectAggregator(64 * 1024))
            addLast(WebSocketServerProtocolHandler(path, null, true))
            addLast(WebSocketServerHandler())
        }
    }
}
