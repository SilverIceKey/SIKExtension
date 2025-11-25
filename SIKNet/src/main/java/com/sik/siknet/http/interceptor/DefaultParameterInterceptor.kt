package com.sik.siknet.http.interceptor

import android.util.Log
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.sik.sikcore.extension.globalGson
import com.sik.siknet.http.HttpUtils.CLIENT_MEDIA_TYPE
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response

/**
 * Default parameter interceptor
 * 全局默认参数添加拦截器
 * @constructor Create empty Default parameter interceptor
 */
class DefaultParameterInterceptor : Interceptor {
    companion object {
        var params: HashMap<String, String?> = hashMapOf()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var originalRequest = chain.request()
        val method = originalRequest.method

        if (method.equals("GET", ignoreCase = true)) {
            // 处理 GET 请求，添加查询参数（仅当原请求中不存在该参数时）
            originalRequest = handleGetRequest(originalRequest)
        } else if (method.equals("POST", ignoreCase = true) && originalRequest.body != null) {
            // 处理 POST 请求，修改请求体
            originalRequest = handlePostRequest(originalRequest)
        }

        return chain.proceed(originalRequest)
    }

    private fun handleGetRequest(request: Request): Request {
        val urlBuilder = request.url.newBuilder()
        // 遍历预设参数，只有在原 URL 中不存在该参数时才添加
        params.filter { it.value != null }.forEach { (key, value) ->
            if (!request.url.queryParameterNames.contains(key)) {
                urlBuilder.addQueryParameter(key, value)
            }
        }
        return request.newBuilder().url(urlBuilder.build()).build()
    }

    private fun handlePostRequest(request: Request): Request {
        val body = request.body ?: return request
        val contentType = body.contentType()?.toString()

        return when {
            contentType == CLIENT_MEDIA_TYPE.toString() -> handleJsonPostRequest(request, body)
            contentType == "application/x-www-form-urlencoded" -> handleFormPostRequest(
                request,
                body
            )

            else -> request
        }
    }

    private fun handleJsonPostRequest(request: Request, body: RequestBody): Request {
        val buffer = okio.Buffer()
        body.writeTo(buffer)
        val requestBodyString = buffer.readUtf8()

        return try {
            val mergedBody = mergeJsonParameters(requestBodyString)
            request.newBuilder().post(RequestBody.create(CLIENT_MEDIA_TYPE, mergedBody)).build()
        } catch (e: Exception) {
            Log.i("DefaultParameterInterceptor","当前类型为非对象，无法放置参数")
            request
        }
    }

    private fun handleFormPostRequest(request: Request, body: RequestBody): Request {
        val formBodyBuilder = FormBody.Builder()
        val existingParams = mutableSetOf<String>()

        // 将原有的参数复制，并记录已有的参数名
        if (body is FormBody) {
            for (i in 0 until body.size) {
                val key = body.name(i)
                val value = body.value(i)
                formBodyBuilder.add(key, value)
                existingParams.add(key)
            }
        }

        // 添加新的参数（仅当原请求中不存在该参数时）
        params.filter { it.value != null }.forEach { (key, value) ->
            if (!existingParams.contains(key)) {
                formBodyBuilder.add(key, value ?: "")
            }
        }

        return request.newBuilder().post(formBodyBuilder.build()).build()
    }

    private fun mergeJsonParameters(bodyString: String): String {
        return try {
            // 尝试解析为 JsonObject
            val jsonObject = globalGson.fromJson(bodyString, JsonObject::class.java)
            params.filter { it.value != null }.forEach { (key, value) ->
                if (!jsonObject.has(key)) {
                    jsonObject.addProperty(key, value)
                }
            }
            jsonObject.toString()
        } catch (e: Exception) {
            try {
                // 尝试解析为 JsonArray
                val jsonArray = globalGson.fromJson(bodyString, JsonArray::class.java)
                params.filter { it.value != null }.forEach { (key, value) ->
                    jsonArray.forEach { element ->
                        if (!element.asJsonObject.has(key)) {
                            element.asJsonObject.addProperty(key, value)
                        }
                    }
                }
                jsonArray.toString()
            } catch (e: Exception) {
                Log.i("DefaultParameterInterceptor","解析JSON异常，无法合并参数")
                bodyString // 无法解析时返回原始 body 字符串
            }
        }
    }
}