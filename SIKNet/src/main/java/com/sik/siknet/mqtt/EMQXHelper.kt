package com.sik.siknet.mqtt

import android.util.Log
import com.sik.sikcore.eventbus.DefaultBusModel
import com.sik.sikcore.receivers.ScreenStatusReceiver
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.greenrobot.eventbus.Subscribe

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
     * mqtt连接状态
     */
    var isMqttConnect: Boolean = false

    companion object {
        val instance: EMQXHelper by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            EMQXHelper()
        }
    }

    init {
        mqttCallback = object : MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                Log.i("EMQXHelper", "mqtt连接丢失开始重连")
                emqxCallback?.connectionLost(cause)
                try {
                    mqttClient.reconnect()
                    while (!mqttClient.isConnected) {
                        continue
                    }
                    Log.i("EMQXHelper", "qos:${emqxConfig.qos()}")
                    mqttClient.subscribe(emqxConfig.topic, emqxConfig.qos())
                } catch (e: MqttException) {
                    //重连异常
                }
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.i(
                    "EMQXHelper",
                    "收到消息:主题:${topic},Qos:${message?.qos},内容:${message?.toString()}"
                )
                mqttClient.messageArrivedComplete(message?.id!!, emqxConfig.qos())
                emqxCallback?.messageArrived(topic, message)
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                Log.i("EMQXHelper", "收到确认消息")
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
    fun init(mqttConfig: EMQXConfig? = null) {
        if (mqttConfig != null) {
            this.emqxConfig = mqttConfig
            this.emqxCallback = mqttConfig.getMqttCallback()
        } else if (!this::emqxConfig.isInitialized) {
            throw IllegalStateException("EMQXConfig is not initialized")
        }
        try {
            val config = emqxConfig
            mqttClient = MqttAsyncClient(
                config.brokenUrl,
                config.clientId,
                config.getMemoryPersistence()
            )
            mqttClient.setCallback(mqttCallback)
            mqttClient.connect(
                config.getMqttConnectOptions(),
                this,
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log.i("EMQXHelper","topic:${config.topic},qos:${config.qos()}")
                        mqttClient.subscribe(config.topic, config.qos())
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        //连接失败
                    }

                })
        } catch (me: MqttException) {
            Log.e("EMQXHelper", "reason:${me.getReasonCode()}")
            Log.e("EMQXHelper", "msg:${me.message}")
            Log.e("EMQXHelper", "loc:${me.getLocalizedMessage()}")
            Log.e("EMQXHelper", "cause:${me.cause}")
            Log.e("EMQXHelper", "excep:${me}")
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