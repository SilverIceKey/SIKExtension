package com.sik.siknet.mqtt

import android.util.Log
import com.google.gson.Gson
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
import java.nio.charset.Charset

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

    // Gson 用于 Any -> JSON
    private val gson: Gson = Gson()

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
                    // 重连异常
                    Log.e("EMQXHelper", "mqtt重连异常: ${e.message}")
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
                        Log.i("EMQXHelper", "topic:${config.topic},qos:${config.qos()}")
                        mqttClient.subscribe(config.topic, config.qos())
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        // 连接失败
                        Log.e("EMQXHelper", "mqtt连接失败: ${exception?.message}")
                    }
                })
        } catch (me: MqttException) {
            Log.e("EMQXHelper", "reason:${me.reasonCode}")
            Log.e("EMQXHelper", "msg:${me.message}")
            Log.e("EMQXHelper", "loc:${me.localizedMessage}")
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
     * 发送任意数据（Any）
     *
     * @param data  要发送的数据对象，会被序列化为 JSON
     * @param topic 发送主题，默认使用 emqxConfig.topic
     * @param qos   QOS，默认使用 emqxConfig.qos()
     */
    fun send(data: Any, topic: String = emqxConfig.topic, qos: Int = emqxConfig.qos()) {
        // 1. 确保配置与 client 已初始化
        if (!this::emqxConfig.isInitialized) {
            Log.e("EMQXHelper", "send 失败：EMQXConfig 未初始化")
            return
        }
        if (!this::mqttClient.isInitialized) {
            Log.e("EMQXHelper", "send 失败：mqttClient 未初始化")
            return
        }

        // 2. 序列化为 JSON -> 字节数组
        val payload: ByteArray = try {
            gson.toJson(data).toByteArray(Charset.defaultCharset())
        } catch (e: Exception) {
            Log.e("EMQXHelper", "send 序列化失败：${e.message}")
            return
        }

        // 3. 简单大小限制（例如 1MB）
        if (payload.size > 1024 * 1024) {
            Log.w("EMQXHelper", "发送消息体过大（${payload.size} bytes），取消发送")
            return
        }

        val mqttMessage = MqttMessage().apply {
            this.payload = payload
            this.qos = qos
        }

        try {
            if (!mqttClient.isConnected) {
                Log.w("EMQXHelper", "send 失败：当前未连接 mqtt，丢弃消息，建议上层触发重连")
                return
            }
            mqttClient.publish(topic, mqttMessage)
            Log.i("EMQXHelper", "send 成功：topic=$topic, qos=$qos, payload=${String(payload)}")
        } catch (e: Exception) {
            Log.e("EMQXHelper", "send 发送异常：${e.message}")
            // 如果你也想像另一个 Helper 那样做「延时重试」，可以在这里加重试逻辑
        }
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
