package com.sik.siknet.tcp.netty.ws

import android.util.Log
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import io.netty.handler.codec.http.websocketx.WebSocketFrame
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent

class WebSocketClientHandler(private val messageListener: MessageListener?) :
    SimpleChannelInboundHandler<WebSocketFrame>() {

    override fun channelActive(ctx: ChannelHandlerContext) {
        Log.i("WebSocketClientHandler", "WebSocket 客户端通道激活")
        super.channelActive(ctx)
    }

    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
        // 复用你在管理器里添加的 IdleStateHandler：空闲则发 Ping
        if (evt is IdleStateEvent && evt.state() == IdleState.WRITER_IDLE) {
            ctx.writeAndFlush(PingWebSocketFrame())
        }
        super.userEventTriggered(ctx, evt)
    }

    override fun channelRead0(ctx: ChannelHandlerContext, frame: WebSocketFrame) {
        when (frame) {
            is TextWebSocketFrame -> {
                val receivedText = frame.text()
                Log.d("WebSocketClientHandler", "收到文本: $receivedText")
                messageListener?.onMessage(receivedText)
            }

            is BinaryWebSocketFrame -> {
                val bytes = ByteArray(frame.content().readableBytes()).also {
                    frame.content().readBytes(it)
                }
                Log.d("WebSocketClientHandler", "收到二进制, 长度=${bytes.size}")
                messageListener?.onRawMessage(bytes)
            }

            is PongWebSocketFrame -> Log.d("WebSocketClientHandler", "收到 PONG")
            is CloseWebSocketFrame -> {
                Log.i(
                    "WebSocketClientHandler",
                    "收到 CLOSE 帧: code=${frame.statusCode()}, reason=${frame.reasonText()}"
                )
                ctx.close()
            }

            else -> Log.w("WebSocketClientHandler", "未处理帧类型: ${frame.javaClass.simpleName}")
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        Log.e("WebSocketClientHandler", "WS 客户端异常: ${cause.message}", cause)
        ctx.close()
    }
}
