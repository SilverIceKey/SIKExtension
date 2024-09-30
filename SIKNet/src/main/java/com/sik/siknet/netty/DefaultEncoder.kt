package com.sik.siknet.netty

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder

// 默认编码器
class DefaultEncoder<T> : MessageToByteEncoder<T>() {
    override fun encode(ctx: ChannelHandlerContext, msg: T, out: ByteBuf) {
        // 默认实现为将对象转为字节（需要自定义逻辑）
        out.writeBytes(msg.toString().toByteArray())
    }
}