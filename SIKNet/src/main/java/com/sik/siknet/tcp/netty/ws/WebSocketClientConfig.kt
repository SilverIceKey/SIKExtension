package com.sik.siknet.tcp.netty.ws

import com.sik.siknet.tcp.netty.core.common.NettyConfig
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpClientCodec
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolConfig
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler
import io.netty.handler.codec.http.websocketx.WebSocketVersion
import io.netty.handler.ssl.SslContextBuilder
import java.net.URI

abstract class WebSocketClientConfig(
    wsUrl: String,
    override val heartbeatInterval: Long = 30,
    override val reconnectInterval: Long = 5,
    override val maxReconnectAttempts: Int = -1, // -1 表示无限重连，与现有语义一致
    override val isAutoSwitchThread: Boolean = true,
    val messageListener: MessageListener? = null
) : NettyConfig() {

    private val uri = URI(wsUrl)

    override val host: String? = uri.host
    override val port: Int = if (uri.port != -1) uri.port else if (uri.scheme == "wss") 443 else 80
    override val mode: Mode? = Mode.CLIENT

    private val wsConfig = WebSocketClientProtocolConfig.newBuilder()
        .webSocketUri(uri)
        .version(WebSocketVersion.V13)
        .allowExtensions(true)
        .handleCloseFrames(true)
        .build()

    override fun channelInit(ch: SocketChannel) {
        ch.pipeline().apply {
            if (uri.scheme.equals("wss", ignoreCase = true)) {
                val sslCtx = SslContextBuilder.forClient().build()
                addLast(sslCtx.newHandler(ch.alloc(), host, port))
            }
            addLast(HttpClientCodec())
            addLast(HttpObjectAggregator(64 * 1024))
            addLast(WebSocketClientProtocolHandler(wsConfig)) // 自动握手/close帧管理
            addLast(WebSocketClientHandler(messageListener))                 // 你的业务处理
        }
    }
}
