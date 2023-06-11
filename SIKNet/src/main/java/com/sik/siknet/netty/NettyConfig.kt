package com.sik.siknet.netty

import io.netty.channel.Channel
import io.netty.channel.ChannelInboundHandler
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.MessageToByteEncoder
import io.netty.handler.codec.MessageToMessageDecoder

/**
 * netty配置类
 */
abstract class NettyConfig<T> {
    /**
     * 主机地址
     */
    open val host: String = "";

    /**
     * 端口
     */
    open val port: Int = 80;

    /**
     * channel处理器
     */
    open val initChannel: (Channel) -> Unit = {}

    /**
     * 解码器
     */
    open val decoder: MessageToMessageDecoder<T>? = null

    /**
     * 编码器
     */
    open val encoder: MessageToByteEncoder<T>? = null

    /**
     * 发送者
     */
    open val sender: ChannelInboundHandler? = null

    /**
     * 处理器
     */
    open val process: SimpleChannelInboundHandler<T>? = null

    /**
     * 连接成功
     */
    open val connectSuccess: () -> Unit = {}

    /**
     * 重试次数
     * -1为一直重试
     */
    open val retryTimes: Int = 0

    /**
     * 重试时间间隔，毫秒
     */
    open val retryTime: Long = 10 * 1000L
}