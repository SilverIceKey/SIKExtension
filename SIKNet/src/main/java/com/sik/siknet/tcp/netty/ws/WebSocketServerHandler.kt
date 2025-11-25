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

class WebSocketServerHandler : SimpleChannelInboundHandler<WebSocketFrame>() {

    override fun channelActive(ctx: ChannelHandlerContext) {
        Log.i("WebSocketServerHandler", "有客户端连接: ${ctx.channel().remoteAddress()}")
        super.channelActive(ctx)
    }

    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
        if (evt is IdleStateEvent && evt.state() == IdleState.READER_IDLE) {
            Log.i(
                "WebSocketServerHandler",
                "读空闲超时，主动关闭: ${ctx.channel().remoteAddress()}"
            )
            ctx.close()
        }
        super.userEventTriggered(ctx, evt)
    }

    override fun channelRead0(ctx: ChannelHandlerContext, frame: WebSocketFrame) {
        when (frame) {
            is TextWebSocketFrame -> {
                val text = frame.text()
                Log.d("WebSocketServerHandler", "收到文本: $text")
                ctx.writeAndFlush(TextWebSocketFrame("echo: $text"))
            }
            is BinaryWebSocketFrame -> {
                Log.d(
                    "WebSocketServerHandler",
                    "收到二进制: ${frame.content().readableBytes()} bytes"
                )
                ctx.writeAndFlush(PongWebSocketFrame()) // 示例：回个PONG
            }
            is PingWebSocketFrame -> ctx.writeAndFlush(PongWebSocketFrame())
            is CloseWebSocketFrame -> {
                Log.i("WebSocketServerHandler", "对端关闭: ${ctx.channel().remoteAddress()}")
                ctx.close()
            }

            else -> Log.w("WebSocketServerHandler", "未处理帧: ${frame.javaClass.simpleName}")
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        Log.e("WebSocketServerHandler", "WS 服务端异常: ${cause.message}", cause)
        ctx.close()
    }
}
