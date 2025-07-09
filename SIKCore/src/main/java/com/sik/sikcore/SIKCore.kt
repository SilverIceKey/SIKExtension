package com.sik.sikcore

import android.app.Application
import com.tencent.mmkv.MMKV
import org.slf4j.LoggerFactory

/**
 * 扩展的初始化
 */
object SIKCore {
    @Volatile
    private var application: Application? = null
    private val logger = LoggerFactory.getLogger(SIKCore::class.java)

    private var initListener: MutableList<InitListener> = mutableListOf()

    /**
     * 初始化
     */
    fun init(application: Application) {
        SIKCore.application = application
        initListener.forEach {
            it.init(application)
        }
        MMKV.initialize(application)
        try {
            System.loadLibrary("SIKCore")
        } catch (t: Throwable) {
            logger.error("Failed to load native library SIKCore", t)
        }
    }

    /**
     * 注册监听器
     */
    fun registerInitListener(listener: InitListener) {
        this.initListener.add(listener)
    }

    /**
     * 获取application
     */
    fun getApplication(): Application {
        if (application == null) {
            throw NullPointerException("请先初始化SIKCore")
        } else {
            return application!!
        }
    }
}
