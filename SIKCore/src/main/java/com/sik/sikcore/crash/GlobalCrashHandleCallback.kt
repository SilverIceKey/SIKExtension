package com.sik.sikcore.crash

/**
 * 全局异常捕捉回调
 */
interface GlobalCrashHandleCallback {
    /**
     * 异常处理
     */
    fun crashHandler(e:Throwable):Boolean
}