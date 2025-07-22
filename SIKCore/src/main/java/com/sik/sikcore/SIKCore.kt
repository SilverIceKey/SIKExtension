package com.sik.sikcore

import android.app.Application
import com.tencent.mmkv.MMKV
import org.slf4j.LoggerFactory

/**
 * SIKCore 核心库初始化器（优化版）
 *
 * 用于应用级全局初始化，如 MMKV、native lib、模块监听器等。
 * ⚠️ 注意：为避免冷启动卡顿，耗时操作已移至后台线程执行。
 */
object SIKCore {

    // 应用上下文
    @Volatile
    private var application: Application? = null

    // 日志器（基于 slf4j，可替换为你项目中的统一日志工具）
    private val logger = LoggerFactory.getLogger(SIKCore::class.java)

    // 初始化监听器列表（支持外部注册）
    private val initListeners = mutableListOf<InitListener>()

    // 是否已加载 native 库标志位
    private var nativeLibLoaded = false

    /**
     * 初始化入口（推荐在 Application.onCreate 中调用）
     *
     * 为保证冷启动性能，耗时操作将在子线程中执行。
     */
    fun init(application: Application) {
        SIKCore.application = application

        // 启动子线程执行耗时初始化逻辑
        Thread {
            safeLoadNativeLib()
            safeInitMMKV()
            initListeners.forEach {
                try {
                    it.init(application)
                } catch (e: Exception) {
                    logger.warn("InitListener failed: ${it.javaClass.simpleName}", e)
                }
            }
        }.start()
    }

    /**
     * 安全加载 Native 库（支持延迟加载，防止启动阻塞）
     */
    private fun safeLoadNativeLib() {
        if (!nativeLibLoaded) {
            try {
                System.loadLibrary("SIKCore")
                nativeLibLoaded = true
                logger.info("SIKCore native lib loaded successfully.")
            } catch (t: Throwable) {
                logger.error("Failed to load native library SIKCore", t)
            }
        }
    }

    /**
     * 初始化 MMKV 持久化组件（支持高性能 KV 存储）
     */
    private fun safeInitMMKV() {
        try {
            MMKV.initialize(application)
            logger.info("MMKV initialized.")
        } catch (e: Exception) {
            logger.error("MMKV initialization failed", e)
        }
    }

    /**
     * 注册初始化监听器（模块可实现 InitListener 接口并注册）
     * ⚠️ 注意：监听器执行在子线程，初始化逻辑请避免主线程依赖
     */
    fun registerInitListener(listener: InitListener) {
        initListeners.add(listener)
    }

    /**
     * 获取 Application 实例（确保初始化后调用）
     * @throws IllegalStateException 未初始化时抛出
     */
    fun getApplication(): Application {
        return application ?: throw IllegalStateException("请先调用 SIKCore.init(application)")
    }
}
