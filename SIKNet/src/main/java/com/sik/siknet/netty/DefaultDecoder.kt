package com.sik.siknet.netty

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder

// 默认解码器
class DefaultDecoder<T> : MessageToMessageDecoder<T>() {
    override fun decode(ctx: ChannelHandlerContext, msg: T, out: MutableList<Any>) {
        out.add(msg as Any) // 默认直接传递消息
    }
}