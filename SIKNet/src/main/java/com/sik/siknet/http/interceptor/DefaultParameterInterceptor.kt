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
        // 现有的，全局默认，不动
        var params: HashMap<String, String?> = hashMapOf()

        // 新增：按 host 分的默认参数
        private val hostParams: MutableMap<String, MutableMap<String, String?>> = mutableMapOf()

        /**
         * 获取 / 创建某个 host 对应的默认参数 map
         */
        fun paramsForHost(host: String): MutableMap<String, String?> =
            hostParams.getOrPut(host) { mutableMapOf() }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var originalRequest = chain.request()
        val method = originalRequest.method

        if (method.equals("GET", ignoreCase = true)) {
            originalRequest = handleGetRequest(originalRequest)
        } else if (method.equals("POST", ignoreCase = true) && originalRequest.body != null) {
            originalRequest = handlePostRequest(originalRequest)
        }

        return chain.proceed(originalRequest)
    }

    /**
     * 计算当前请求应该使用的“最终默认参数”
     * 优先级：全局默认 < host 默认；请求里显式传入的再覆盖它们
     */
    private fun resolvedParams(request: Request): Map<String, String?> {
        val result = LinkedHashMap<String, String?>()

        // 1. 全局默认
        params.forEach { (k, v) ->
            if (v != null) result[k] = v
        }

        // 2. 当前 host 的默认，覆盖全局
        val host = request.url.host
        hostParams[host]?.forEach { (k, v) ->
            if (v != null) result[k] = v
        }

        return result
    }

    private fun handleGetRequest(request: Request): Request {
        val urlBuilder = request.url.newBuilder()
        val finalParams = resolvedParams(request)

        finalParams.forEach { (key, value) ->
            // 只在原 URL 没有这个 key 时才补
            if (!request.url.queryParameterNames.contains(key) && value != null) {
                urlBuilder.addQueryParameter(key, value)
            }
        }
        return request.newBuilder().url(urlBuilder.build()).build()
    }

    private fun handlePostRequest(request: Request): Request {
        val body = request.body ?: return request
        val contentType = body.contentType()?.toString()

        return when {
            contentType == CLIENT_MEDIA_TYPE.toString() ->
                handleJsonPostRequest(request, body)

            contentType == "application/x-www-form-urlencoded" ->
                handleFormPostRequest(request, body)
            else -> request
        }
    }

    private fun handleJsonPostRequest(request: Request, body: RequestBody): Request {
        val buffer = okio.Buffer()
        body.writeTo(buffer)
        val requestBodyString = buffer.readUtf8()
        val finalParams = resolvedParams(request)

        return try {
            val mergedBody = mergeJsonParameters(requestBodyString, finalParams)
            request.newBuilder()
                .post(RequestBody.create(CLIENT_MEDIA_TYPE, mergedBody))
                .build()
        } catch (e: Exception) {
            Log.i("DefaultParameterInterceptor", "当前类型为非对象，无法放置参数")
            request
        }
    }

    private fun handleFormPostRequest(request: Request, body: RequestBody): Request {
        val formBodyBuilder = FormBody.Builder()
        val existingParams = mutableSetOf<String>()

        if (body is FormBody) {
            for (i in 0 until body.size) {
                val key = body.name(i)
                val value = body.value(i)
                formBodyBuilder.add(key, value)
                existingParams.add(key)
            }
        }

        val finalParams = resolvedParams(request)

        finalParams.forEach { (key, value) ->
            // 已经有的 key 不覆盖
            if (!existingParams.contains(key) && value != null) {
                formBodyBuilder.add(key, value)
            }
        }

        return request.newBuilder().post(formBodyBuilder.build()).build()
    }

    private fun mergeJsonParameters(
        bodyString: String,
        finalParams: Map<String, String?>
    ): String {
        return try {
            // 尝试解析为 JsonObject
            val jsonObject = globalGson.fromJson(bodyString, JsonObject::class.java)
            finalParams.filter { it.value != null }.forEach { (key, value) ->
                if (!jsonObject.has(key)) {
                    jsonObject.addProperty(key, value)
                }
            }
            jsonObject.toString()
        } catch (e: Exception) {
            try {
                // 尝试解析为 JsonArray
                val jsonArray = globalGson.fromJson(bodyString, JsonArray::class.java)
                finalParams.filter { it.value != null }.forEach { (key, value) ->
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