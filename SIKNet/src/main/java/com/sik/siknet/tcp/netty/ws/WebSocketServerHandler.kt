package com.sik.siknet.tcp.netty.ws

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.websocketx.*
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent
import org.slf4j.LoggerFactory

class WebSocketServerHandler : SimpleChannelInboundHandler<WebSocketFrame>() {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun channelActive(ctx: ChannelHandlerContext) {
        log.info("有客户端连接: {}", ctx.channel().remoteAddress())
        super.channelActive(ctx)
    }

    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
        if (evt is IdleStateEvent && evt.state() == IdleState.READER_IDLE) {
            log.info("读空闲超时，主动关闭: {}", ctx.channel().remoteAddress())
            ctx.close()
        }
        super.userEventTriggered(ctx, evt)
    }

    override fun channelRead0(ctx: ChannelHandlerContext, frame: WebSocketFrame) {
        when (frame) {
            is TextWebSocketFrame -> {
                val text = frame.text()
                log.debug("收到文本: {}", text)
                ctx.writeAndFlush(TextWebSocketFrame("echo: $text"))
            }
            is BinaryWebSocketFrame -> {
                log.debug("收到二进制: {} bytes", frame.content().readableBytes())
                ctx.writeAndFlush(PongWebSocketFrame()) // 示例：回个PONG
            }
            is PingWebSocketFrame -> ctx.writeAndFlush(PongWebSocketFrame())
            is CloseWebSocketFrame -> {
                log.info("对端关闭: {}", ctx.channel().remoteAddress())
                ctx.close()
            }
            else -> log.warn("未处理帧: {}", frame.javaClass.simpleName)
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        log.error("WS 服务端异常: {}", cause.message, cause)
        ctx.close()
    }
}
