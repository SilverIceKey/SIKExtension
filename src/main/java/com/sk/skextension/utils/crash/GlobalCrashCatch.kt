package com.sk.skextension.utils.crash

import android.content.Context
import android.os.Process
import com.sk.skextension.utils.explain.Explain

/**
 * 全局异常捕捉
 */
class GlobalCrashCatch : Thread.UncaughtExceptionHandler {
    @Explain(explainValue = "本地context")
    private lateinit var context: Context

    @Explain(explainValue = "默认异常处理")
    private lateinit var defaultHandler: Thread.UncaughtExceptionHandler

    @Explain(explainValue = "全局异常处理回调")
    private var globalCrashHandleCallback: GlobalCrashHandleCallback? = null

    companion object {
        @Explain(explainValue = "全局异常捕捉类")
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
        if (!handleCrash(e)) {//判断异常是否已经处理
            defaultHandler.uncaughtException(t, e)
        } else {
            Process.killProcess(Process.myPid())
            System.exit(1)
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
    fun handleCrash(e: Throwable): Boolean {
        return globalCrashHandleCallback?.crashHandler(e) ?: true
    }
}