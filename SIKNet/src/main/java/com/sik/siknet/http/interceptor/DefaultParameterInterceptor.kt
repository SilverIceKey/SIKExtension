package com.sik.siknet.http.interceptor

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.sik.siknet.http.HttpUtils.CLIENT_MEDIA_TYPE
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.slf4j.LoggerFactory

/**
 * Default parameter interceptor
 * 全局默认参数添加拦截器
 * @constructor Create empty Default parameter interceptor
 */
class DefaultParameterInterceptor : Interceptor {
    companion object {
        var params: HashMap<String, String?> = hashMapOf()
    }

    private val gson = Gson()

    private val logger = LoggerFactory.getLogger(this::class.java.simpleName)

    override fun intercept(chain: Interceptor.Chain): Response {
        var originalRequest = chain.request()
        val method = originalRequest.method
        var originalHttpUrl = originalRequest.url

        // 对于GET请求，添加查询参数
        if (method.equals("GET", ignoreCase = true)) {
            originalHttpUrl = originalHttpUrl.newBuilder()
                .apply {
                    params.filter { it.value != null }.forEach {
                        addQueryParameter(it.key, it.value)
                    }
                }
                .build()
            originalRequest = originalRequest.newBuilder().url(originalHttpUrl).build()
        }
        // 对于POST请求，检查Content-Type并相应地修改请求体
        else if (method.equals("POST", ignoreCase = true) && originalRequest.body != null) {
            originalRequest.body?.let {
                if (it.contentType() == CLIENT_MEDIA_TYPE) {
                    // JSON提交
                    val buffer = okio.Buffer()
                    originalRequest.body!!.writeTo(buffer)
                    val originalRequestBody = buffer.readUtf8()
                    try {
                        val mergedParameters: JsonObject =
                            gson.fromJson(originalRequestBody, JsonObject::class.java)
                        params.filter { it.value != null }.forEach {
                            mergedParameters.addProperty(it.key, it.value)
                        }
                        val newRequestBody =
                            mergedParameters.toString().toRequestBody(CLIENT_MEDIA_TYPE)
                        originalRequest = originalRequest.newBuilder().post(newRequestBody).build()
                    } catch (e: Exception) {
                        try {
                            val mergedParameters: JsonArray =
                                gson.fromJson(originalRequestBody, JsonArray::class.java)
                            params.filter { it.value != null }.forEach {
                                for (mergedParameter in mergedParameters) {
                                    mergedParameter.asJsonObject.addProperty(it.key, it.value)
                                }
                            }
                            val newRequestBody =
                                mergedParameters.toString().toRequestBody(CLIENT_MEDIA_TYPE)
                            originalRequest =
                                originalRequest.newBuilder().post(newRequestBody).build()
                        } catch (e: Exception) {
                            logger.info("当前类型为非对象，无法放置参数")
                        }
                    }
                } else if (it.contentType() == "application/x-www-form-urlencoded".toMediaTypeOrNull()) {
                    // Form提交
                    val formBodyBuilder = FormBody.Builder()
                    params.filter { it.value != null }.filter { it.value != null }.forEach {
                        formBodyBuilder.add(it.key, it.value ?: "")
                    }
                    if (originalRequest.body is FormBody) {
                        val originalFormBody = originalRequest.body as FormBody
                        for (i in 0 until originalFormBody.size) {
                            formBodyBuilder.add(originalFormBody.name(i), originalFormBody.value(i))
                        }
                    }
                    originalRequest =
                        originalRequest.newBuilder().post(formBodyBuilder.build()).build()
                }
            }
            originalRequest.header("Content-Type")?.let {
                if (it.contains("application/json")) {
                    // JSON提交
                    val buffer = okio.Buffer()
                    originalRequest.body!!.writeTo(buffer)
                    val originalRequestBody = buffer.readUtf8()
                    try {
                        val mergedParameters: JsonObject =
                            gson.fromJson(originalRequestBody, JsonObject::class.java)
                        params.filter { it.value != null }.forEach {
                            mergedParameters.addProperty(it.key, it.value)
                        }
                        val newRequestBody =
                            mergedParameters.toString().toRequestBody(CLIENT_MEDIA_TYPE)
                        originalRequest = originalRequest.newBuilder().post(newRequestBody).build()
                    } catch (e: Exception) {
                        try {
                            val mergedParameters: JsonArray =
                                gson.fromJson(originalRequestBody, JsonArray::class.java)
                            params.filter { it.value != null }.forEach {
                                for (mergedParameter in mergedParameters) {
                                    mergedParameter.asJsonObject.addProperty(it.key, it.value)
                                }
                            }
                            val newRequestBody =
                                mergedParameters.toString().toRequestBody(CLIENT_MEDIA_TYPE)
                            originalRequest =
                                originalRequest.newBuilder().post(newRequestBody).build()
                        } catch (e: Exception) {
                            logger.info("当前类型为非对象，无法放置参数")
                        }
                    }
                } else if (it.contains("application/x-www-form-urlencoded")) {
                    // Form提交
                    val formBodyBuilder = FormBody.Builder()
                    params.filter { it.value != null }.forEach {
                        formBodyBuilder.add(it.key, it.value ?: "")
                    }
                    if (originalRequest.body is FormBody) {
                        val originalFormBody = originalRequest.body as FormBody
                        for (i in 0 until originalFormBody.size) {
                            formBodyBuilder.add(originalFormBody.name(i), originalFormBody.value(i))
                        }
                    }
                    originalRequest =
                        originalRequest.newBuilder().post(formBodyBuilder.build()).build()
                }
            }
        }

        return chain.proceed(originalRequest)
    }
}
