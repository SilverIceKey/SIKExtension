package com.sik.siknet.retrofit

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * 通用返回处理
 */
abstract class ResponseCallback<T> : Callback<T> {
    //初始化日志工具
    var log: Logger = LoggerFactory.getLogger(this::class.java)

    /**
     * 服务器200之后对errorcode进行判断处理
     */
    override fun onResponse(call: Call<T>, response: Response<T>) {
        log.info("网络请求成功")
        val responseData = response.body()
        if (errorCheck(responseData as Any)) {
            onResponseSuccess(call, responseData)
        }
    }

    /**
     * 网络异常，无法达到服务器或服务器出现非200情况执行
     */
    override fun onFailure(call: Call<T>, t: Throwable) {
        log.warn("网络请求失败", t)
        networkError("网络请求异常", t)
    }

    /**
     * 请求成功之后错误码非错误执行
     */
    abstract fun onResponseSuccess(call: Call<T>, response: T)

    /**
     * 请求成功之后错误码有错误执行
     */
    open fun onResponseError(errorCode: Int?, errorMsg: String?) {
        log.info("请求错误,errcode:${errorCode},原因:${errorMsg}")
        checkErrorCode(errorCode)
    }

    companion object {
        /**
         * 检查错误代码
         * 重写此方法用于检查代码
         */
        @JvmStatic
        var checkErrorCode: (errorCode: Int?) -> Unit = {}

        /**
         * 错误检测
         */
        @JvmStatic
        var errorCheck: (response: Any) -> Boolean = { true }

        /**
         * 网络异常
         */
        @JvmStatic
        var networkError: (msg: String, t: Throwable) -> Unit = { _, _ -> }
    }
}