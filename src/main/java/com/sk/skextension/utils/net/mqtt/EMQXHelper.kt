package com.sk.skextension.utils.net.mqtt

import com.blankj.utilcode.util.DeviceUtils
import com.sk.skextension.utils.eventbus.BusModel
import org.eclipse.paho.client.mqttv3.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.concurrent.thread

/**
 * Mqtt帮助类
 */
class EMQXHelper {
    /**
     * mqtt客户端
     */
    lateinit var mqttClient: MqttAsyncClient

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

    /**
     * mqtt连接状态
     */
    var isMqttConnect: Boolean = false

    companion object {
        /**
         * 事件总线类型
         */
        val EVENTBUS_TYPE: String = "ScreenStatusChange"

        /**
         * 屏幕打开
         */
        val SCREEN_ON: Int = 1
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
                thread {
                    mqttClient.reconnect()
                    while (!mqttClient.isConnected){
                        continue
                    }
                    mqttClient.subscribe(emqxConfig.topic, emqxConfig.qos())
                }.start()
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                log.info("收到消息:主题:${topic},Qos:${message?.qos},内容:${message?.toString()}")
                mqttClient.messageArrivedComplete(message?.id!!, message.qos)
                emqxCallback?.messageArrived(topic, message)
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                log.info("收到确认消息")
                emqxCallback?.deliveryComplete(token)
            }
        }
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    @Subscribe
    fun onScreenStatusChange(bus: BusModel) {
        if (bus.type == EVENTBUS_TYPE) {
            if (bus.code == SCREEN_ON) {
                if (!mqttClient.isConnected) {
                    init()
                }
            }
        }
    }

    fun init(mqttConfig: EMQXConfig = emqxConfig) {
        this.emqxConfig = mqttConfig
        this.emqxCallback = mqttConfig.getMqttCallback()
        try {
            mqttClient = MqttAsyncClient(
                mqttConfig.brokenUrl,
                DeviceUtils.getUniqueDeviceId(),
                mqttConfig.getMemoryPersistence()
            )
            mqttClient.setCallback(mqttCallback)
            mqttClient.connect(
                mqttConfig.getMqttConnectOptions(),
                this,
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        mqttClient.subscribe(mqttConfig.topic, mqttConfig.qos())
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {

                    }

                })
        } catch (me: MqttException) {
            log.error("reason:${me.getReasonCode()}")
            log.error("msg:${me.message}")
            log.error("loc:${me.getLocalizedMessage()}")
            log.error("cause:${me.cause}")
            log.error("excep:${me}")
            me.printStackTrace()
        }
    }

    /**
     * 释放mqtt客户端
     */
    fun release() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
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
         * 收到确认消息回调
         */
        fun deliveryComplete(token: IMqttDeliveryToken?)
    }
}