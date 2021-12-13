package com.sk.skextension.utils.net.mqtt

import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

/**
 * Mqtt配置类
 */
abstract class EMQXConfig(val brokenUrl: String, val topic: String) {
    /**
     * 登录用户名
     */
    open var username:String = ""

    /**
     * 登录密码
     */
    open var password:String = ""
    /**
     * 设置qos
     */
    open fun qos(): Int = 2

    /**
     * 获取持久化内存配置
     */
    abstract fun getMemoryPersistence(): MemoryPersistence

    /**
     * 获取连接配置
     */
    abstract fun getMqttConnectOptions(): MqttConnectOptions

    /**
     * 获取EMQX回调
     */
    abstract fun getMqttCallback(): EMQXHelper.EMQXCallback
}