package com.sik.siknet.http

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sik.sikcore.extension.globalGson
import com.sik.sikcore.extension.toJson
import com.sik.siknet.http.interceptor.ProgressInterceptor
import com.sik.siknet.http.interceptor.ProgressListener
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.reflect.full.memberProperties

inline fun <reified T> String.httpGet(params: Map<String, String> = emptyMap()): T {
    if (HttpUtils.isLoggerInRequest) {
        HttpUtils.logger.info(this)
        HttpUtils.logger.info(params.toJson())
    }
    // 构造带参数的URL
    val urlWithParams = buildString {
        append(this@httpGet)
        if (params.isNotEmpty()) {
            append('?')
            params.entries.joinTo(this, "&") { "${it.key}=${it.value}" }
        }
    }
    val request = Request.Builder().url(urlWithParams).get().build()
    return try {
        val response = HttpUtils.createOkHttpClient().newCall(request).execute()
        val body = response.body?.string() ?: ""
        response.close()
        globalGson.fromJson(
            body, object : TypeToken<T>() {}.type
        )
    } catch (e: Exception) {
        // 创建自定义异常 NetException
        val netException = NetException(request, e.message, e)
        // 全局异常处理器，返回是否处理成功的布尔值
        val globalNetExceptionHandler = HttpUtils.globalNetExceptionHandler(request, netException)
        if (globalNetExceptionHandler) {
            try {
                globalGson.fromJson<T>(
                    "{}", object : TypeToken<T>() {}.type
                )
            } catch (convertException: Exception) {
                globalGson.fromJson<T>(
                    "[]", object : TypeToken<T>() {}.type
                )
            }
        } else {
            throw netException
        }
    }
}

inline fun <reified T> String.httpPostForm(formParameters: Any?): T {
    if (HttpUtils.isLoggerInRequest) {
        HttpUtils.logger.info(this)
        HttpUtils.logger.info(formParameters.toJson())
    }
    val formBodyBuilder = FormBody.Builder()
    formParameters?.let {
        when {
            it is Map<*, *> -> {
                for ((key, value) in it) {
                    formBodyBuilder.add(key.toString(), value.toString())
                }
            }

            else -> {
                val params = it.toMap()
                for ((key, value) in params) {
                    formBodyBuilder.add(key, value)
                }
            }
        }

    }
    val request = Request.Builder().url(this).post(formBodyBuilder.build()).build()
    return try {
        val response = HttpUtils.createOkHttpClient().newCall(request).execute()
        val body = response.body?.string() ?: ""
        response.close()
        globalGson.fromJson(
            body, object : TypeToken<T>() {}.type
        )
    } catch (e: Exception) {
        // 创建自定义异常 NetException
        val netException = NetException(request, e.message, e)
        // 全局异常处理器，返回是否处理成功的布尔值
        val globalNetExceptionHandler = HttpUtils.globalNetExceptionHandler(request, netException)
        if (globalNetExceptionHandler) {
            try {
                globalGson.fromJson<T>(
                    "{}", object : TypeToken<T>() {}.type
                )
            } catch (convertException: Exception) {
                globalGson.fromJson<T>(
                    "[]", object : TypeToken<T>() {}.type
                )
            }
        } else {
            throw netException
        }
    }
}

inline fun <reified T> String.httpPostJson(data: Any? = null): T {

    val json = if (data is String) {
        data
    } else {
        if (data == null) {
            "{}"
        } else {
            globalGson.toJson(data)
        }
    }
    if (HttpUtils.isLoggerInRequest) {
        HttpUtils.logger.info(this)
        HttpUtils.logger.info(json)
    }
    val mediaType = "application/json; charset=utf-8".toMediaType()
    val requestBody: RequestBody = (json ?: "").toRequestBody(mediaType)
    val request = Request.Builder().url(this).method("POST", requestBody).build()
    return try {
        val response = HttpUtils.createOkHttpClient().newCall(request).execute()
        val body = response.body?.string() ?: ""
        response.close()
        globalGson.fromJson(
            body, object : TypeToken<T>() {}.type
        )
    } catch (e: Exception) {
        // 创建自定义异常 NetException
        val netException = NetException(request, e.message, e)
        // 全局异常处理器，返回是否处理成功的布尔值
        val globalNetExceptionHandler = HttpUtils.globalNetExceptionHandler(request, netException)
        if (globalNetExceptionHandler) {
            try {
                globalGson.fromJson<T>(
                    "{}", object : TypeToken<T>() {}.type
                )
            } catch (convertException: Exception) {
                globalGson.fromJson<T>(
                    "[]", object : TypeToken<T>() {}.type
                )
            }
        } else {
            throw netException
        }
    }
}

inline fun <reified T> String.httpUploadFile(
    fileParameterName: String, file: File, params: Map<String, String>
): T {
    if (HttpUtils.isLoggerInRequest) {
        HttpUtils.logger.info(this)
        HttpUtils.logger.info(params.toJson())
    }
    val fileBody = file.asRequestBody("application/octet-stream".toMediaType())
    val requestBodyBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
        .addFormDataPart(fileParameterName, file.name, fileBody)

    // 添加其他表单参数
    for ((key, value) in params) {
        requestBodyBuilder.addFormDataPart(key, value)
    }

    val requestBody = requestBodyBuilder.build()
    val request = Request.Builder().url(this).post(requestBody).build()

    return try {
        val response = HttpUtils.createOkHttpClient().newCall(request).execute()
        val body = response.body?.string() ?: ""
        response.close()
        globalGson.fromJson<T>(
            body, object : TypeToken<T>() {}.type
        )
    } catch (e: Exception) {
        // 创建自定义异常 NetException
        val netException = NetException(request, e.message, e)
        // 全局异常处理器，返回是否处理成功的布尔值
        val globalNetExceptionHandler = HttpUtils.globalNetExceptionHandler(request, netException)
        if (globalNetExceptionHandler) {
            try {
                globalGson.fromJson<T>(
                    "{}", object : TypeToken<T>() {}.type
                )
            } catch (convertException: Exception) {
                globalGson.fromJson<T>(
                    "[]", object : TypeToken<T>() {}.type
                )
            }
        } else {
            throw netException
        }
    }
}

fun String.httpDownloadFile(
    methodStr: String,
    headers: Map<String, String> = mapOf(),
    data: Any? = null,
    destinationFile: File,
    progressListener: ProgressListener
): Boolean {
    val json = if (data is String) {
        data
    } else {
        globalGson.toJson(data)
    }
    if (HttpUtils.isLoggerInRequest) {
        HttpUtils.logger.info(this)
        HttpUtils.logger.info(json)
    }
    val mediaType = "application/json; charset=utf-8".toMediaType()
    val requestBody: RequestBody = (json ?: "").toRequestBody(mediaType)
    val request = Request.Builder().apply {
        url(this@httpDownloadFile)
        headers.forEach { (t, u) ->
            addHeader(t, u)
        }
        if (methodStr == "GET") {
            if (data is Map<*, *>) {
                // 构造带参数的URL
                val urlWithParams = buildString {
                    append(this@httpDownloadFile)
                    if (data.isNotEmpty()) {
                        append('?')
                        data.entries.joinTo(this, "&") { "${it.key}=${it.value}" }
                    }
                }
                url(urlWithParams)
            }
            get()
        } else {
            method(methodStr, requestBody)
        }
    }.build()
    return try {
        val response = HttpUtils.createOkHttpClientBuilder(5, TimeUnit.MINUTES).apply {
            addNetworkInterceptor(ProgressInterceptor(progressListener, destinationFile))
        }.build().newCall(request).execute() // 执行同步网络请求
        if (!response.isSuccessful || (response.header("Content-Type")
                ?: "").contains("text/plain")
        ) {
            response.close()
            return false // 下载失败
        }
        response.close()
        true // 下载成功
    } catch (e: IOException) {
        // 创建自定义异常 NetException
        val netException = NetException(request, e.message, e)
        // 全局异常处理器，返回是否处理成功的布尔值
        HttpUtils.globalNetExceptionHandler(request, netException)
        false // 发生异常，下载失败
    }
}

fun <T : Any> T.toMap(): Map<String, String> {
    return this::class.memberProperties.associate { property ->
        property.name to property.getter.call(this).toString()
    }
}

// ---------------------------
// 异步调用版本，基于协程
// ---------------------------


/**
 * 异步 GET 请求
 */
suspend inline fun <reified T> String.httpGetAsync(
    params: Map<String, String> = emptyMap()
): T = suspendCancellableCoroutine { cont ->
    if (HttpUtils.isLoggerInRequest) {
        HttpUtils.logger.info(this)
        HttpUtils.logger.info(params.toJson())
    }
    val urlWithParams = buildString {
        append(this@httpGetAsync)
        if (params.isNotEmpty()) {
            append('?')
            params.entries.joinTo(this, "&") { "${it.key}=${it.value}" }
        }
    }
    val request = Request.Builder().url(urlWithParams).get().build()
    val call = HttpUtils.createOkHttpClient().newCall(request)
    cont.invokeOnCancellation { call.cancel() }
    call.enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            val netException = NetException(request, e.message, e)
            val handled = HttpUtils.globalNetExceptionHandler(request, netException)
            if (handled) {
                try {
                    cont.resume(
                        globalGson.fromJson("{}", object : TypeToken<T>() {}.type)
                    )
                } catch (_: Exception) {
                    cont.resume(
                        globalGson.fromJson("[]", object : TypeToken<T>() {}.type)
                    )
                }
            } else {
                cont.resumeWithException(netException)
            }
        }

        override fun onResponse(call: Call, response: Response) {
            response.use {
                val body = it.body?.string() ?: ""
                cont.resume(
                    globalGson.fromJson(body, object : TypeToken<T>() {}.type)
                )
            }
        }
    })
}

/**
 * 异步 POST JSON 请求
 */
suspend inline fun <reified T> String.httpPostJsonAsync(data: Any? = null): T =
    suspendCancellableCoroutine { cont ->
        val json = if (data is String) {
            data
        } else {
            if (data == null) "{}" else globalGson.toJson(data)
        }
        if (HttpUtils.isLoggerInRequest) {
            HttpUtils.logger.info(this)
            HttpUtils.logger.info(json)
        }
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = (json ?: "").toRequestBody(mediaType)
        val request = Request.Builder().url(this).method("POST", requestBody).build()
        val call = HttpUtils.createOkHttpClient().newCall(request)
        cont.invokeOnCancellation { call.cancel() }
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val netException = NetException(request, e.message, e)
                val handled = HttpUtils.globalNetExceptionHandler(request, netException)
                if (handled) {
                    try {
                        cont.resume(
                            globalGson.fromJson("{}", object : TypeToken<T>() {}.type)
                        )
                    } catch (_: Exception) {
                        cont.resume(
                            globalGson.fromJson("[]", object : TypeToken<T>() {}.type)
                        )
                    }
                } else {
                    cont.resumeWithException(netException)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val body = it.body?.string() ?: ""
                    cont.resume(
                        globalGson.fromJson(body, object : TypeToken<T>() {}.type)
                    )
                }
            }
        })
    }
