package com.sik.siknet.http.interceptor

import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Response

/**
 * Default header interceptor
 * 全局默认头部添加拦截器
 * @constructor Create empty Default header interceptor
 */
class DefaultHeaderInterceptor : Interceptor {
    companion object {
        val defaultHeaders: HashMap<String, String?> = hashMapOf()
    }

    override fun intercept(chain: Chain): Response {
        val originalRequest = chain.request()
        val builder = originalRequest.newBuilder()

        // 仅添加原请求中不存在的默认 header
        defaultHeaders.filter { it.value != null }.forEach { (key, value) ->
            if (originalRequest.header(key) == null) {
                builder.addHeader(key, value!!)
            }
        }

        // 仅当原请求中不存在 "Connection" header 时添加
        if (originalRequest.header("Connection") == null) {
            builder.addHeader("Connection", "close")
        }

        return chain.proceed(builder.build())
    }
}
