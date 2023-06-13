package com.sik.skextensionsample

import com.sik.siknet.netty.NettyConfig

class CustomNettyConfig : NettyConfig<String>() {
    override val host: String
        get() = "192.168.31.131"
    override val port: Int
        get() = 10090
    override val retryTimes: Int
        get() = -1
}