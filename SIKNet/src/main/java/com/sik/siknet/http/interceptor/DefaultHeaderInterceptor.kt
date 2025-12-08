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
        // 原来的
        val defaultHeaders: HashMap<String, String?> = hashMapOf()

        // 新增：按 host 存的默认 header
        private val hostHeaders: MutableMap<String, MutableMap<String, String?>> = mutableMapOf()

        fun headersForHost(host: String): MutableMap<String, String?> =
            hostHeaders.getOrPut(host) { mutableMapOf() }
    }

    override fun intercept(chain: Chain): Response {
        val originalRequest = chain.request()
        val builder = originalRequest.newBuilder()

        val merged = LinkedHashMap<String, String?>()

        // 1. 全局默认
        defaultHeaders.forEach { (k, v) ->
            if (v != null) merged[k] = v
        }

        // 2. 当前 host 默认，覆盖全局
        val host = originalRequest.url.host
        hostHeaders[host]?.forEach { (k, v) ->
            if (v != null) merged[k] = v
        }

        // 最终：只在原请求没有这个 header 时才加
        merged.forEach { (key, value) ->
            if (value != null && originalRequest.header(key) == null) {
                builder.addHeader(key, value)
            }
        }

        // Connection 维持现有逻辑
        if (originalRequest.header("Connection") == null) {
            builder.addHeader("Connection", "close")
        }

        return chain.proceed(builder.build())
    }
}

