package com.sik.siknet.http.interceptor

import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Response

/**
 * Default header interceptor
 * 全局默认头部添加拦截器
 * @constructor Create empty Default header interceptor
 */
class DefaultHeaderInterceptor: Interceptor {
    companion object {
        val defaultHeaders: HashMap<String, String?> = hashMapOf()
    }

    override fun intercept(chain: Chain): Response {
        val request = chain.request().newBuilder()
            .apply {
                defaultHeaders.filter { it.value != null }.forEach { (t, u) ->
                    addHeader(t, u!!)
                }
            }
            .addHeader("Connection", "close").build()
        return chain.proceed(request)
    }
}