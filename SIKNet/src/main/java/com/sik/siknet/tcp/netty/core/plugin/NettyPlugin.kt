package com.sik.siknet.tcp.netty.core.plugin

import com.sik.siknet.tcp.netty.core.common.BaseNettyManager
import io.netty.channel.socket.SocketChannel

/**
 * 纯“旁路型”插件：不修改业务数据，只做观测/定时/心跳等。
 */
interface NettyPlugin {
    /** 在 channel 初始化时装配（建议只 addLast 自己的 handler，不改已有 handler） */
    fun install(ch: SocketChannel, manager: BaseNettyManager) {}

    /** 管理器启动后（已在专用线程） */
    fun onStarted(manager: BaseNettyManager) {}

    /** 管理器停止前 */
    fun onStopping(manager: BaseNettyManager) {}
}
