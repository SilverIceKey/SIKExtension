package com.sk.skextension.utils.net.mqtt

import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

/**
 * Mqtt配置类
 */
abstract class EMQXConfig(val brokenUrl: String, val topic: String) {
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