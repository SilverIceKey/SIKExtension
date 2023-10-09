package com.sik.sikcore

import android.app.Application
import com.tencent.mmkv.MMKV

/**
 * 扩展的初始化
 */
object SIKCore {
    @Volatile
    private var application: Application? = null

    /**
     * 初始化
     */
    fun init(application: Application) {
        SIKCore.application = application
        MMKV.initialize(application)
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