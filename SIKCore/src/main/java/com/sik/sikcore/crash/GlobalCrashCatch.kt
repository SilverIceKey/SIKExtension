package com.sik.sikcore.crash

import android.content.Context

/**
 * 全局异常捕捉
 */
class GlobalCrashCatch : Thread.UncaughtExceptionHandler {
    /**
     * Context
     * 本地context
     */
    private lateinit var context: Context

    /**
     * Default handler
     * 默认异常处理
     */
    private lateinit var defaultHandler: Thread.UncaughtExceptionHandler

    /**
     * Global crash handle callback
     * 全局异常处理回调
     */
    private var globalCrashHandleCallback: GlobalCrashHandleCallback? = null

    companion object {
        /**
         * Instance
         * 全局异常捕捉类
         */
        val instance: GlobalCrashCatch by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            GlobalCrashCatch()
        }
    }

    /**
     * 全局异常处理初始化
     */
    fun init(context: Context): GlobalCrashCatch {
        this.context = context
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler()!!
        Thread.setDefaultUncaughtExceptionHandler(this)
        return this
    }

    /**
     * 全局异常处理捕捉
     */
    override fun uncaughtException(t: Thread, e: Throwable) {
        if (!handleCrash(e)) {
            //异常未处理的情况下交由系统处理
            defaultHandler.uncaughtException(t, e)
        }
    }

    /**
     * 设置全局异常处理监听供第三方调用
     */
    fun setGlobalCrashHandlerListener(globalCrashHandleCallback: GlobalCrashHandleCallback): GlobalCrashCatch {
        this.globalCrashHandleCallback = globalCrashHandleCallback
        return this
    }

    /**
     * 默认全局异常捕捉处理
     */
    private fun handleCrash(e: Throwable): Boolean {
        return globalCrashHandleCallback?.crashHandler(e) ?: true
    }
}