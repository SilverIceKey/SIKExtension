package com.sik.siknet.tcp.netty.core.plugin.simple

import android.util.Log
import com.sik.siknet.tcp.netty.core.common.BaseNettyManager
import com.sik.siknet.tcp.netty.core.plugin.NettyPlugin
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import io.netty.channel.socket.SocketChannel
import io.netty.handler.timeout.IdleStateEvent

/**
 * 简化版插件：把 install 里的繁琐活儿封装掉，只暴露直觉化的钩子。
 * 约束：所有钩子“只观察不改数据”，消息仍原样透传。
 */
abstract class SimplePlugin(
    private val name: String = "simple"
) : NettyPlugin {

    // ========== 生命周期（可选） ==========
    override fun onStarted(manager: BaseNettyManager) = Unit
    override fun onStopping(manager: BaseNettyManager) = Unit

    // ========== 连接态（可选） ==========
    protected open fun onConnect(ctx: ChannelHandlerContext) = Unit
    protected open fun onDisconnect(ctx: ChannelHandlerContext) = Unit

    // ========== 空闲事件（可选） ==========
    protected open fun onIdle(ctx: ChannelHandlerContext, evt: IdleStateEvent) = Unit

    // ========== 读/写四个关键钩子（核心） ==========
    /**
     * 读入前（channelRead 即将被调用）
     */
    protected open fun beforeRead(ctx: ChannelHandlerContext, msg: Any) = Unit

    /**
     * 读入后（channelRead 已透传）
     */
    protected open fun afterRead(ctx: ChannelHandlerContext, msg: Any) = Unit

    /**
     * 写出前（write 即将被调用）
     * 注意：不要修改 msg，也不要 retain/release。
     */
    protected open fun beforeWrite(ctx: ChannelHandlerContext, msg: Any) = Unit

    /**
     * 写出后（write 完成，包含成功/失败）
     */
    protected open fun afterWrite(ctx: ChannelHandlerContext, msg: Any, success: Boolean, cause: Throwable?) = Unit

    /**
     * 异常（可选）
     */
    protected open fun onException(ctx: ChannelHandlerContext, cause: Throwable) = Unit

    // ========== 安装：封装掉繁琐操作 ==========
    final override fun install(ch: SocketChannel, manager: BaseNettyManager) {
        val handlerName = "${name}-${System.identityHashCode(this)}"
        ch.pipeline().addLast(handlerName, object : ChannelDuplexHandler() {

            override fun channelActive(ctx: ChannelHandlerContext) {
                try { onConnect(ctx) } catch (t: Throwable) { safeLog("onConnect", t) }
                super.channelActive(ctx)
            }

            override fun channelInactive(ctx: ChannelHandlerContext) {
                try { onDisconnect(ctx) } catch (t: Throwable) { safeLog("onDisconnect", t) }
                super.channelInactive(ctx)
            }

            override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
                if (evt is IdleStateEvent) {
                    try { onIdle(ctx, evt) } catch (t: Throwable) { safeLog("onIdle", t) }
                }
                super.userEventTriggered(ctx, evt)
            }

            override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
                try { beforeRead(ctx, msg) } catch (t: Throwable) { safeLog("beforeRead", t) }
                super.channelRead(ctx, msg) // 透传
                try { afterRead(ctx, msg) } catch (t: Throwable) { safeLog("afterRead", t) }
            }

            override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
                try { beforeWrite(ctx, msg) } catch (t: Throwable) { safeLog("beforeWrite", t) }
                super.write(ctx, msg, promise) // 透传
                // 写完成回调
                promise.addListener { f ->
                    try { afterWrite(ctx, msg, f.isSuccess, f.cause()) } catch (t: Throwable) { safeLog("afterWrite", t) }
                }
            }

            override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
                try { onException(ctx, cause) } catch (_: Throwable) { /* 避免二次异常 */ }
                super.exceptionCaught(ctx, cause)
            }
        })
    }

    private fun safeLog(hook: String, t: Throwable) {
        Log.w("SimplePlugin", "[$name] $hook hook error: ${t.toString()}")
    }
}
