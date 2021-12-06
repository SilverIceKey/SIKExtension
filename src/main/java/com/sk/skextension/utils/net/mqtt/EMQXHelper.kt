package com.sk.skextension.utils.net.mqtt

import android.util.Log
import com.sk.skextension.utils.device.DeviceUtil
import org.eclipse.paho.client.mqttv3.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Mqtt帮助类
 */
class EMQXHelper {
    /**
     * mqtt客户端
     */
    lateinit var mqttClient: MqttClient

    /**
     * emqx配置
     */
    lateinit var emqxConfig: EMQXConfig

    /**
     * mqtt回调
     */
    var mqttCallback: MqttCallback

    /**
     * emqx回调
     */
    var emqxCallback: EMQXCallback? = null

    /**
     * 日志工具
     */
    var log: Logger

    companion object {
        val instance: EMQXHelper by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            EMQXHelper()
        }
    }

    init {
        log = LoggerFactory.getLogger(this::class.java)
        mqttCallback = object : MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                log.info("mqtt连接丢失开始重连")
                emqxCallback?.connectionLost(cause)
                mqttClient.connect(emqxConfig.getMqttConnectOptions())
                mqttClient.subscribe(emqxConfig.topic)
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                log.info("收到消息:主题:${topic},Qos:${message?.qos},内容:${String(message?.payload!!)}")
                emqxCallback?.messageArrived(topic, message)
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                log.info("消息处理完成")
                emqxCallback?.deliveryComplete(token)
            }
        }
    }

    fun init(mqttConfig: EMQXConfig) {
        this.emqxConfig = mqttConfig
        this.emqxCallback = mqttConfig.getMqttCallback()
        try {
            mqttClient = MqttClient(
                mqttConfig.brokenUrl,
                DeviceUtil.getSN(),
                mqttConfig.getMemoryPersistence()
            )
            mqttClient.setCallback(mqttCallback)
            mqttClient.connect(mqttConfig.getMqttConnectOptions())
            mqttClient.subscribe(mqttConfig.topic)
        } catch (me: MqttException) {
            log.error("reason ", me.getReasonCode().toString())
            log.error("msg ", me.message.toString())
            log.error("loc ", me.getLocalizedMessage())
            log.error("cause ", me.cause.toString())
            log.error("excep ", me.toString())
            me.printStackTrace()
        }
    }

    /**
     * 释放mqtt客户端
     */
    fun release() {
        mqttClient.disconnect()
        mqttClient.close()
    }

    /**
     * EMQX回调类
     */
    interface EMQXCallback {
        /**
         * 连接丢失时回调
         */
        fun connectionLost(cause: Throwable?)

        /**
         * 消息送达时回调
         */
        fun messageArrived(topic: String?, message: MqttMessage?)

        /**
         * 消息处理完时回调
         */
        fun deliveryComplete(token: IMqttDeliveryToken?)
    }
}