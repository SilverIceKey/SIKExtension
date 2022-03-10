package com.sk.skextension.utils.net.retrofit

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okio.BufferedSink
import kotlin.Throws
import java.io.IOException
import java.nio.Buffer
import java.util.HashMap

/**
 * 头部和参数默认添加拦截器
 */
class HeaderParamsPreloadInterceptor : Interceptor {
    /**
     * 默认头部
     */
    private val mHeader = HashMap<String, String>()

    /**
     * 默认参数
     */
    private val mParams = HashMap<String, String>()

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder: Request.Builder = chain.request().newBuilder()
        for (key in mHeader.keys) {
            requestBuilder.header(key, mHeader[key]!!)
        }
        if ("POST" == chain.request().method) {
            var requestBody: RequestBody? = chain.request().body
            if (requestBody == null || requestBody is FormBody) {
                val formBodyBuilder: FormBody.Builder = FormBody.Builder()
                if (requestBody != null) {
                    for (i in 0 until (requestBody as FormBody).size) {
                        formBodyBuilder.addEncoded(
                            requestBody.encodedName(i),
                            requestBody.encodedValue(i)
                        )
                    }
                }
                for (key in mParams.keys) {
                    formBodyBuilder.addEncoded(key, mParams[key]!!)
                }
                requestBody = formBodyBuilder.build()
            } else if (requestBody.contentType().toString()==JsonBody.JSON.toString()) {
                val buffer:BufferedSink = okio.Buffer()
                requestBody.writeTo(buffer)
                val content: String = buffer.buffer.readUtf8()
                val hashMap: HashMap<String, String>? =
                    Gson().fromJson(content, object : TypeToken<HashMap<String, String>>() {}.type)
                for (key in mParams.keys) {
                    hashMap?.put(key, mParams[key]!!)
                }
                requestBody = JsonBody.create(Gson().toJson(hashMap))
            }
            requestBuilder.post(requestBody)
            return chain.proceed(requestBuilder.build())
        }
        return chain.proceed(requestBuilder.build())
    }

    /**
     * 添加默认头部
     * @param key
     * @param value
     * @return
     */
    fun addHeader(key: String, value: String): HeaderParamsPreloadInterceptor {
        mHeader[key] = value
        return this
    }

    /**
     * 批量添加默认头部
     * @param headers
     * @return
     */
    fun addHeader(headers: Map<String, String>?): HeaderParamsPreloadInterceptor {
        mHeader.putAll(headers!!)
        return this
    }

    /**
     * 添加默认参数
     * @param key
     * @param value
     * @return
     */
    fun addParams(key: String, value: String): HeaderParamsPreloadInterceptor {
        mParams[key] = value
        return this
    }

    /**
     * 批量添加默认参数
     * @param params
     * @return
     */
    fun addParams(params: Map<String, String>?): HeaderParamsPreloadInterceptor {
        mParams.putAll(params!!)
        return this
    }
}