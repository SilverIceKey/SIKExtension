package com.sik.siknet.netty

import java.util.concurrent.ConcurrentHashMap

/**
 * netty帮助类
 */
class NettyClientHelper {
    /**
     * 根据配置存储netty请求
     */
    private val nettyClients: ConcurrentHashMap<Class<NettyConfig>, String> = ConcurrentHashMap()

    companion object {
        val instance: NettyClientHelper by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            NettyClientHelper()
        }
    }
}