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
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

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
    @Volatile
    var isMqttConnect: Boolean = false
        private set

    // Gson 用于 Any -> JSON
    private val gson: Gson = Gson()

    // ===== 新增：串行执行器 + 状态标记 =====
    private val mqttIo: ScheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor { r ->
            Thread(r, "EMQX-IO").apply { isDaemon = true }
        }

    @Volatile
    private var isReconnecting: Boolean = false

    @Volatile
    private var released: Boolean = false

    // 缓存待发送的消息
    private val pendingMessages = mutableListOf<PendingMessage>()
    // ====================================

    companion object {
        val instance: EMQXHelper by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            EMQXHelper()
        }
    }

    init {
        mqttCallback = object : MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                Log.i("EMQXHelper", "mqtt连接丢失开始重连")
                isMqttConnect = false
                emqxCallback?.connectionLost(cause)
                reconnect()
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.i(
                    "EMQXHelper",
                    "收到消息:主题:${topic},Qos:${message?.qos},内容:${message?.toString()}"
                )
                try {
                    if (message != null) {
                        mqttClient.messageArrivedComplete(message.id, emqxConfig.qos())
                    }
                } catch (e: Exception) {
                    Log.e("EMQXHelper", "messageArrivedComplete 异常: ${e.message}")
                }
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
            (!this::mqttClient.isInitialized || !mqttClient.isConnected)
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

        runIo {
            if (released) return@runIo

            try {
                // 先关闭旧客户端（如果有）
                if (this::mqttClient.isInitialized) {
                    try {
                        mqttClient.disconnect()
                    } catch (_: Exception) {
                    }
                    try {
                        mqttClient.close()
                    } catch (_: Exception) {
                    }
                }
            } catch (_: Exception) {
            }

            try {
                val config = emqxConfig
                val client = MqttAsyncClient(
                    config.brokenUrl,
                    config.clientId,
                    config.getMemoryPersistence()
                )
                mqttClient = client
                client.setCallback(mqttCallback)
                client.connect(
                    config.getMqttConnectOptions(),
                    this,
                    object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken?) {
                            runIo {
                                try {
                                    Log.i("EMQXHelper", "连接成功 topic:${config.topic},qos:${config.qos()}")
                                    client.subscribe(config.topic, config.qos())
                                    isMqttConnect = true
                                    // 连接成功后发送缓存的消息
                                    sendPendingMessages()
                                } catch (e: Exception) {
                                    Log.e("EMQXHelper", "订阅异常: ${e.message}")
                                    isMqttConnect = false
                                    safeCloseClient()
                                    reconnect()
                                }
                            }
                        }

                        override fun onFailure(
                            asyncActionToken: IMqttToken?,
                            exception: Throwable?
                        ) {
                            Log.e("EMQXHelper", "mqtt连接失败: ${exception?.message}")
                            isMqttConnect = false
                            runIo {
                                safeCloseClient()
                                reconnect()
                            }
                        }
                    })
            } catch (me: MqttException) {
                Log.e("EMQXHelper", "reason:${me.reasonCode}")
                Log.e("EMQXHelper", "msg:${me.message}")
                Log.e("EMQXHelper", "loc:${me.localizedMessage}")
                Log.e("EMQXHelper", "cause:${me.cause}")
                Log.e("EMQXHelper", "excep:${me}")
                me.printStackTrace()
                isMqttConnect = false
                reconnect()
            }
        }
    }

    /**
     * 订阅
     */
    fun subscribe(topic: String?, qos: Int = 2) {
        runIo {
            try {
                mqttClient.subscribe(topic, qos)
            } catch (e: Exception) {
                Log.e("EMQXHelper", "订阅异常: ${e.message}")
            }
        }
    }

    /**
     * 取消订阅
     */
    fun unSubscribe(topic: String?) {
        runIo {
            try {
                mqttClient.unsubscribe(topic)
            } catch (e: Exception) {
                Log.e("EMQXHelper", "取消订阅异常: ${e.message}")
            }
        }
    }

    /**
     * 释放mqtt客户端
     */
    fun release() {
        released = true
        isMqttConnect = false

        runIo {
            safeCloseClient()
        }
        try {
            mqttIo.shutdownNow()
        } catch (_: Exception) {
        }
    }

    /**
     * 发送任意数据（Any）
     *
     * @param data  要发送的数据对象，会被序列化为 JSON
     * @param topic 发送主题，默认使用 emqxConfig.topic
     * @param qos   QOS，默认使用 emqxConfig.qos()
     */
    fun send(data: Any, topic: String = "", qos: Int = emqxConfig.qos()) {
        // 1. 确保client 已初始化
        if (!this::mqttClient.isInitialized) {
            Log.e("EMQXHelper", "send 失败：mqttClient 未初始化")
            // 添加到待发送队列
            addToPendingMessages(data, topic, qos)
            reconnect()
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

        runIo {
            try {
                if (!mqttClient.isConnected) {
                    Log.w("EMQXHelper", "send 失败：当前未连接 mqtt，触发重连")
                    isMqttConnect = false
                    // 添加到待发送队列
                    addToPendingMessages(data, topic, qos)
                    reconnect()
                    return@runIo
                }
                mqttClient.publish(topic, mqttMessage)
                Log.i(
                    "EMQXHelper",
                    "send 成功：topic=$topic, qos=$qos, payload=${String(payload)}"
                )
            } catch (e: Exception) {
                Log.e("EMQXHelper", "send 发送异常：${e.message}")
                isMqttConnect = false
                // 添加到待发送队列
                addToPendingMessages(data, topic, qos)
                reconnect()
            }
        }
    }

    // ================== 内部工具方法 ==================

    private fun runIo(block: () -> Unit) {
        if (released) return
        try {
            mqttIo.execute(block)
        } catch (e: Exception) {
            Log.e("EMQXHelper", "runIo 异常: ${e.message}")
        }
    }

    private fun reconnect() {
        if (released) return
        if (!this::emqxConfig.isInitialized) return
        if (!this::mqttClient.isInitialized) return
        if (isReconnecting) return

        isReconnecting = true
        Log.i("EMQXHelper", "开始执行重连逻辑")

        runIo {
            try {
                val c = mqttClient

                // 已经连上了就只补订阅一下
                if (c.isConnected) {
                    Log.i("EMQXHelper", "重连检查：客户端已连接，补订阅")
                    try {
                        c.subscribe(emqxConfig.topic, emqxConfig.qos())
                        isMqttConnect = true
                        // 发送缓存的消息
                        sendPendingMessages()
                    } catch (e: Exception) {
                        Log.e("EMQXHelper", "重连时订阅异常: ${e.message}")
                        isMqttConnect = false
                    }
                    isReconnecting = false
                    return@runIo
                }

                // 尝试调用 Paho 内置 reconnect
                try {
                    c.reconnect()
                } catch (e: Exception) {
                    Log.e("EMQXHelper", "调用 reconnect 异常: ${e.message}")
                }

                if (c.isConnected) {
                    Log.i("EMQXHelper", "mqtt重连成功，补订阅 topic=${emqxConfig.topic}")
                    try {
                        c.subscribe(emqxConfig.topic, emqxConfig.qos())
                        // 发送缓存的消息
                        sendPendingMessages()
                    } catch (e: Exception) {
                        Log.e("EMQXHelper", "重连成功但订阅失败: ${e.message}")
                    }
                    isMqttConnect = true
                    isReconnecting = false
                } else {
                    // 10 秒后再试
                    Log.i("EMQXHelper", "mqtt重连失败，10秒后重试")
                    scheduleReconnect(10_000L)
                }
            } catch (e: Exception) {
                Log.e("EMQXHelper", "重连过程异常: ${e.message}")
                isMqttConnect = false
                scheduleReconnect(10_000L)
            }
        }
    }

    private fun scheduleReconnect(delayMs: Long) {
        if (released) {
            isReconnecting = false
            return
        }
        try {
            mqttIo.schedule({
                isReconnecting = false
                reconnect()
            }, delayMs, TimeUnit.MILLISECONDS)
        } catch (_: Exception) {
            isReconnecting = false
        }
    }

    private fun safeCloseClient() {
        try {
            if (this::mqttClient.isInitialized) {
                try {
                    mqttClient.disconnect()
                } catch (_: Exception) {
                }
                try {
                    mqttClient.close()
                } catch (_: Exception) {
                }
            }
        } catch (_: Exception) {
        }
    }

    /**
     * 添加消息到待发送队列
     */
    private fun addToPendingMessages(data: Any, topic: String, qos: Int) {
        synchronized(pendingMessages) {
            pendingMessages.add(PendingMessage(data, topic, qos))
        }
    }

    /**
     * 发送缓存的消息
     */
    private fun sendPendingMessages() {
        synchronized(pendingMessages) {
            if (pendingMessages.isEmpty()) return

            Log.i("EMQXHelper", "开始发送缓存的 ${pendingMessages.size} 条消息")

            val messagesToSend = pendingMessages.toList()
            pendingMessages.clear()

            for ((index, message) in messagesToSend.withIndex()) {
                try {
                    // 给一些时间确保连接稳定
                    if (index > 0) {
                        Thread.sleep(100)
                    }

                    send(message.data, message.topic, message.qos)
                } catch (e: Exception) {
                    Log.e("EMQXHelper", "发送缓存消息失败: ${e.message}")
                    // 如果发送失败，重新加入队列
                    pendingMessages.add(message)
                }
            }
        }
    }

    // =================================================

    /**
     * 待发送消息数据类
     */
    private data class PendingMessage(val data: Any, val topic: String, val qos: Int)

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
