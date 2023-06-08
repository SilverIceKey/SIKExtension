package com.sik.siknet.netty

/**
 * netty配置类
 */
abstract class NettyConfig {
    /**
     * 主机地址
     */
    open val host: String = "";

    /**
     * 端口
     */
    open val port: Int = 80;
}