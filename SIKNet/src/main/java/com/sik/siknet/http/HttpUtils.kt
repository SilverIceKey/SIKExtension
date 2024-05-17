package com.sik.siknet.http

import com.sik.siknet.http.interceptor.AutoSaveCookieJar
import com.sik.siknet.http.interceptor.DefaultHeaderInterceptor
import com.sik.siknet.http.interceptor.DefaultParameterInterceptor
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object HttpUtils {

    val CLIENT_MEDIA_TYPE: MediaType? = "application/json; charset=utf-8".toMediaTypeOrNull()

    /**
     * 拦截器
     */
    val interceptor: MutableList<Interceptor> = mutableListOf()

    /**
     * 网络拦截器
     */
    val networkInterceptor: MutableList<Interceptor> = mutableListOf()

    /**
     * Create ok http client
     * 创建okhttpClient
     * @param timeoutTime
     * @param timeoutUnit
     * @return
     */
    fun createOkHttpClient(
        timeoutTime: Long = 60,
        timeoutUnit: TimeUnit = TimeUnit.SECONDS
    ): OkHttpClient {
        return createOkHttpClientBuilder(timeoutTime, timeoutUnit)
            .build()
    }

    /**
     * Create ok http client builder
     * 创建okhttp client建造器
     * @param timeoutTime
     * @param timeoutUnit
     * @return
     */
    fun createOkHttpClientBuilder(
        timeoutTime: Long = 60,
        timeoutUnit: TimeUnit = TimeUnit.SECONDS
    ): OkHttpClient.Builder {
        return OkHttpClient.Builder()
            .cookieJar(AutoSaveCookieJar())
            .addInterceptor(DefaultHeaderInterceptor())
            .addInterceptor(DefaultParameterInterceptor())
            .apply {
                if (interceptor.isNotEmpty()) {
                    interceptor.forEach {
                        addInterceptor(it)
                    }
                }
                if (networkInterceptor.isNotEmpty()) {
                    networkInterceptor.forEach {
                        addNetworkInterceptor(it)
                    }
                }
            }
            .connectTimeout(timeoutTime, timeoutUnit)
            .readTimeout(timeoutTime, timeoutUnit)
            .writeTimeout(timeoutTime, timeoutUnit)
            .followRedirects(true)
    }
}