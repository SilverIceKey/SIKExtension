package com.sik.siknet.tcp.netty.ws

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent
import io.netty.handler.codec.http.websocketx.*
import org.slf4j.LoggerFactory

class WebSocketClientHandler(private val messageListener: MessageListener?) :
    SimpleChannelInboundHandler<WebSocketFrame>() {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun channelActive(ctx: ChannelHandlerContext) {
        log.info("WebSocket 客户端通道激活")
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
                log.debug("收到文本: {}", receivedText)
                messageListener?.onMessage(receivedText)
            }

            is BinaryWebSocketFrame -> {
                val bytes = ByteArray(frame.content().readableBytes()).also {
                    frame.content().readBytes(it)
                }
                log.debug("收到二进制, 长度={}", bytes.size)
                messageListener?.onRawMessage(bytes)
            }

            is PongWebSocketFrame -> log.trace("收到 PONG")
            is CloseWebSocketFrame -> {
                log.info(
                    "收到 CLOSE 帧: code={}, reason={}",
                    frame.statusCode(),
                    frame.reasonText()
                )
                ctx.close()
            }

            else -> log.warn("未处理帧类型: {}", frame.javaClass.simpleName)
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        log.error("WS 客户端异常: {}", cause.message, cause)
        ctx.close()
    }
}
