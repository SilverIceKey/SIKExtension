package com.sik.siknet.tcp.netty.core.handler

import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import org.slf4j.Logger
import org.slf4j.LoggerFactory


/**
 * 使用 SLF4J 实现的 Netty 日志处理器。
 * 记录连接的生命周期事件和异常信息。
 */
class LoggingHandler : ChannelDuplexHandler() {
    /**
     * 当通道激活（连接建立）时触发。
     *
     * @param ctx 通道上下文
     * @throws Exception 如果处理失败则抛出异常
     */
    @Throws(Exception::class)
    override fun channelActive(ctx: ChannelHandlerContext) {
        logger.info("连接已建立: {}", ctx.channel().remoteAddress())
        super.channelActive(ctx)
    }

    /**
     * 当通道失活（连接关闭）时触发。
     *
     * @param ctx 通道上下文
     * @throws Exception 如果处理失败则抛出异常
     */
    @Throws(Exception::class)
    override fun channelInactive(ctx: ChannelHandlerContext) {
        logger.info("连接已关闭: {}", ctx.channel().remoteAddress())
        super.channelInactive(ctx)
    }

    /**
     * 当捕获到异常时触发。
     *
     * @param ctx 通道上下文
     * @param cause 捕获的异常
     * @throws Exception 如果处理失败则抛出异常
     */
    @Throws(Exception::class)
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        logger.error("捕获到异常: {}", cause.message, cause)
        ctx.close() // 关闭连接防止资源泄漏
    }

    /**
     * 当通道发送消息时触发。
     *
     * @param ctx 通道上下文
     * @param msg 要发送的消息
     * @param promise 发送结果的回调
     * @throws Exception 如果处理失败则抛出异常
     */
    @Throws(Exception::class)
    override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
        logger.debug("发送消息: {}", msg)
        super.write(ctx, msg, promise)
    }

    /**
     * 当通道读取到消息时触发。
     *
     * @param ctx 通道上下文
     * @param msg 读取到的消息
     * @throws Exception 如果处理失败则抛出异常
     */
    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        logger.debug("接收到消息: {}", msg)
        super.channelRead(ctx, msg)
    }

    companion object {
        // 使用 SLF4J 获取日志记录器
        private val logger: Logger = LoggerFactory.getLogger(LoggingHandler::class.java)
    }
}



