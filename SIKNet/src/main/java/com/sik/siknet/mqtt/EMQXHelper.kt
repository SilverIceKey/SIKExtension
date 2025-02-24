package com.sik.siknet.mqtt

import com.sik.sikcore.eventbus.DefaultBusModel
import com.sik.sikcore.receivers.ScreenStatusReceiver
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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
                try {
                    mqttClient.reconnect()
                    while (!mqttClient.isConnected) {
                        continue
                    }
                    log.info("qos:${emqxConfig.qos()}")
                    mqttClient.subscribe(emqxConfig.topic, emqxConfig.qos())
                } catch (e: MqttException) {
                    //重连异常
                }
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                log.info("收到消息:主题:${topic},Qos:${message?.qos},内容:${message?.toString()}")
                mqttClient.messageArrivedComplete(message?.id!!, emqxConfig.qos())
                emqxCallback?.messageArrived(topic, message)
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                log.info("收到确认消息")
                emqxCallback?.deliveryComplete(token)
            }
        }
    }

    /**
     * 屏幕状态变化时接收到消息
     */
    @Subscribe
    fun onScreenStatusChange(bus: DefaultBusModel) {
        if (bus.type == ScreenStatusReceiver.EVENTBUS_TYPE &&
            bus.code == ScreenStatusReceiver.SCREEN_ON &&
            !mqttClient.isConnected
        ) {
            init()
        }
    }

    /**
     * 初始化
     */
    fun init(mqttConfig: EMQXConfig = emqxConfig) {
        this.emqxConfig = mqttConfig
        this.emqxCallback = mqttConfig.getMqttCallback()
        try {
            mqttClient = MqttAsyncClient(
                mqttConfig.brokenUrl,
                emqxConfig.clientId,
                mqttConfig.getMemoryPersistence()
            )
            mqttClient.setCallback(mqttCallback)
            mqttClient.connect(
                mqttConfig.getMqttConnectOptions(),
                this,
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        log.info("topic:${mqttConfig.topic},qos:${emqxConfig.qos()}")
                        mqttClient.subscribe(mqttConfig.topic, mqttConfig.qos())
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        //连接失败
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
     * 订阅
     */
    fun subscribe(topic: String?, qos: Int = 2) {
        mqttClient.subscribe(topic, qos)
    }

    /**
     * 取消订阅
     */
    fun unSubscribe(topic: String?) {
        mqttClient.unsubscribe(topic)
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
         * 收到确认消息回调
         */
        fun deliveryComplete(token: IMqttDeliveryToken?)
    }
}