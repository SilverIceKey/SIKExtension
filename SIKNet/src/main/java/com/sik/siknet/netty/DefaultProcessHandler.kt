package com.sik.siknet.netty

import com.sik.sikcore.log.LogUtils
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

// 默认处理器
class DefaultProcessHandler<T> : SimpleChannelInboundHandler<T>() {
    private val logger = LogUtils.getLogger(DefaultProcessHandler::class)
    override fun channelRead0(ctx: ChannelHandlerContext, msg: T) {
        // 默认处理接收到的消息
        logger.i("收到消息: $msg")
    }
}