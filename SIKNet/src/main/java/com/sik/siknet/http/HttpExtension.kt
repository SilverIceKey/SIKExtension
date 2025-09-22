package com.sik.siknet.http

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.google.gson.reflect.TypeToken
import com.sik.sikcore.SIKCore
import com.sik.sikcore.extension.globalGson
import com.sik.sikcore.extension.toJson
import com.sik.siknet.http.interceptor.ProgressInterceptor
import com.sik.siknet.http.interceptor.ProgressListener
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.net.URLDecoder
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

inline fun <reified T> String.httpGet(params: Map<String, String> = emptyMap()): T {
    if (HttpUtils.isLoggerInRequest) {
        HttpUtils.logger.info(this)
        HttpUtils.logger.info(params.toJson())
    }
    // 构造带参数的URL
    val urlWithParams = StringBuilder().apply {
        append(this@httpGet)
        if (params.isNotEmpty()) {
            append('?')
            params.entries.joinTo(this, "&") { "${it.key}=${it.value}" }
        }
    }
    val request = Request.Builder().url(urlWithParams.toString()).get().build()
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
    val json = data as? String
        ?: if (data == null) {
            "{}"
        } else {
            globalGson.toJson(data)
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
    methodStr: String = "GET",
    headers: Map<String, String> = mapOf(),
    data: Any? = null,
    destinationFile: File,
    progressListener: ProgressListener
): Boolean {
    val json = data as? String ?: globalGson.toJson(data)
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
                val urlWithParams = StringBuilder().apply {
                    append(this@httpDownloadFile)
                    if (data.isNotEmpty()) {
                        append('?')
                        data.entries.joinTo(this, "&") { "${it.key}=${it.value}" }
                    }
                }
                url(urlWithParams.toString())
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
    return this::class.memberProperties.associate { p ->
        @Suppress("UNCHECKED_CAST")
        val k = p as KProperty1<T, *>
        val v = k.get(this)
        p.name to (v?.toString() ?: "")
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
    val urlWithParams = StringBuilder().apply {
        append(this@httpGetAsync)
        if (params.isNotEmpty()) {
            append('?')
            params.entries.joinTo(this, "&") { "${it.key}=${it.value}" }
        }
    }
    val request = Request.Builder().url(urlWithParams.toString()).get().build()
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

/**
 * suspend 版下载：
 * - 支持断点续传（已存在内容将使用 Range 追加）
 * - destinationFile 可为 null：则自动保存到 /sdcard/Download（Q+ 走 MediaStore）
 * - 成功返回实际 File（方便 adb pull）；失败返回 null
 *
 * @param context 仅当 destinationFile 为 null 时需要，用于写入公共 Download
 */
suspend fun String.httpDownloadFile(
    methodStr: String = "GET",
    headers: Map<String, String> = emptyMap(),
    data: Any? = null,
    destinationFile: File? = null,
    progressListener: ProgressListener = ProgressListener { _, _, _ -> },
    context: Context = SIKCore.getApplication()
): File? = withContext(kotlinx.coroutines.Dispatchers.IO) {
    val isGet = methodStr.equals("GET", ignoreCase = true)

    // 1) URL + Body（GET 参数做 URL 编码拼接）
    val finalUrl = if (isGet && data is Map<*, *>) {
        val params = data.entries.joinToString("&") { e ->
            val k = java.net.URLEncoder.encode(e.key?.toString() ?: "", "UTF-8")
            val v = java.net.URLEncoder.encode(e.value?.toString() ?: "", "UTF-8")
            "$k=$v"
        }
        StringBuilder().apply {
            append(this@httpDownloadFile)
            if (params.isNotEmpty()) {
                append(if (this@httpDownloadFile.contains("?")) "&" else "?")
                append(params)
            }
        }
    } else this@httpDownloadFile

    val reqBuilder = Request.Builder().url(finalUrl.toString())
    headers.forEach { (k, v) -> reqBuilder.addHeader(k, v) }
    val requestBody: RequestBody? = if (!isGet) {
        val json = (data as? String) ?: (if (data == null) "{}" else globalGson.toJson(data))
        (json ?: "").toRequestBody("application/json; charset=utf-8".toMediaType())
    } else null

    // 2) 目标：指定文件 or 公有 Download
    val target: DiskTarget = when {
        destinationFile != null -> DiskTarget.FilePath(destinationFile)
        else -> DiskTarget.Downloads(context)
    }

    // 3) 初步名（供续传与缺省命名）
    val guessedName = guessFileNameFromUrl(finalUrl.toString())

    // 4) 查询已存在（用于 Range）
    val existInfo = when (target) {
        is DiskTarget.FilePath -> {
            val f = target.file
            if (f.exists()) ResumeInfo(f.length(), f.name, null) else ResumeInfo(0L, f.name, null)
        }

        is DiskTarget.Downloads -> {
            val existing = findExistingInDownloads(target.ctx, guessedName)
            if (existing != null) ResumeInfo(existing.size, existing.displayName, existing.uri)
            else ResumeInfo(0L, guessedName, null)
        }
    }

    val req = (if (requestBody == null) reqBuilder.get() else reqBuilder.method(
        methodStr.uppercase(),
        requestBody
    ))
        .apply { if (existInfo.length > 0) header("Range", "bytes=${existInfo.length}-") }
        .build()

    // 5) 发请求
    val resp = try {
        HttpUtils.createOkHttpClientBuilder(5, TimeUnit.MINUTES).build().newCall(req).execute()
    } catch (e: IOException) {
        HttpUtils.globalNetExceptionHandler(
            req,
            NetException(req, e.message, e)
        ); return@withContext null
    }

    resp.use { response ->
        val code = response.code
        if (!response.isSuccessful && code !in arrayOf(200, 206)) return@withContext null
        val body = response.body ?: return@withContext null

        // 6) 确定最终文件名 & MIME
        val mime = response.header("Content-Type")?.substringBefore(';')?.trim().orEmpty()
        val nameFromHeader =
            parseFilenameFromContentDisposition(response.header("Content-Disposition"))
        val finalName = nameFromHeader ?: guessedName ?: fallbackName(finalUrl.toString(), mime)

        // 7) 计算总量（用于更准确的进度）
        val contentLength = body.contentLength().takeIf { it > 0 } ?: -1L
        val totalExpected = if (existInfo.length > 0 && code == 206) {
            parseTotalFromContentRange(response.header("Content-Range"))
                ?: (if (contentLength > 0) existInfo.length + contentLength else -1L)
        } else if (code == 200) contentLength else -1L

        // 8) 打开输出（覆盖/追加）
        val out: OutputSink = when (target) {
            is DiskTarget.FilePath -> {
                target.file.parentFile?.mkdirs()
                val append = (code == 206)
                OutputSink.FileStream(
                    FileOutputStream(target.file, append),
                    file = target.file,
                    append = append
                )
            }

            is DiskTarget.Downloads -> {
                openDownloadsOutput(
                    target.ctx,
                    finalName,
                    mime,
                    existInfo,
                    wantAppend = (code == 206)
                )
                    ?: return@withContext null
            }
        }

        // 9) 写入 + 进度
        var written = existInfo.length
        val buf = ByteArray(DEFAULT_BUFFER_SIZE)
        try {
            body.byteStream().use { input ->
                out.stream.use { output ->
                    while (true) {
                        val n = input.read(buf)
                        if (n <= 0) break
                        output.write(buf, 0, n)
                        written += n
                        progressListener.update(written, totalExpected, false)
                    }
                    output.flush()
                }
            }

            // Q+：新建的 MediaStore 条目，清理 IS_PENDING
            if (out is OutputSink.MediaStoreStream && out.newlyCreated) {
                finalizePendingDownload(out.ctx, out.uri)
            }

            progressListener.update(written, totalExpected, true)

            // 10) 返回可直接 adb pull 的 File
            return@withContext when (out) {
                is OutputSink.FileStream -> out.file
                is OutputSink.MediaStoreStream -> {
                    File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        finalName
                    )
                }
            }
        } catch (e: Throwable) {
            if (out is OutputSink.MediaStoreStream && out.newlyCreated) {
                runCatching { out.ctx.contentResolver.delete(out.uri, null, null) }
            }
            HttpUtils.globalNetExceptionHandler(req, NetException(req, e.message, e))
            return@withContext null
        }
    }
}

/* ====== 辅助类型/方法（HttpExtension 内部私有） ====== */

private sealed interface DiskTarget {
    data class FilePath(val file: File) : DiskTarget
    data class Downloads(val ctx: Context) : DiskTarget
}

private data class ResumeInfo(val length: Long, val displayName: String?, val uri: Uri?)
private data class ExistingDownload(val uri: Uri, val displayName: String, val size: Long)
private sealed interface OutputSink {
    val stream: OutputStream

    data class FileStream(override val stream: OutputStream, val file: File, val append: Boolean) :
        OutputSink

    data class MediaStoreStream(
        override val stream: OutputStream,
        val ctx: Context,
        val uri: Uri,
        val newlyCreated: Boolean
    ) : OutputSink
}

private fun guessFileNameFromUrl(url: String): String? {
    val path = url.substringBefore('#').substringBefore('?')
    val raw = path.substringAfterLast('/', "")
    if (raw.isBlank()) return null
    return try {
        URLDecoder.decode(raw, "UTF-8")
    } catch (_: Throwable) {
        raw
    }
}

private fun md5Hex(s: String): String {
    val md = MessageDigest.getInstance("MD5")
    return md.digest(s.toByteArray()).joinToString("") { "%02x".format(it) }
}

private fun fallbackName(url: String, mime: String): String {
    val base = "dl_${System.currentTimeMillis()}_${md5Hex(url).take(8)}"
    val ext = when (mime.lowercase()) {
        "image/png" -> ".png"
        "image/jpeg", "image/jpg" -> ".jpg"
        "image/webp" -> ".webp"
        "image/gif" -> ".gif"
        "image/bmp" -> ".bmp"
        "application/vnd.android.package-archive" -> ".apk"
        "image/svg+xml" -> ".svg"
        else -> ""
    }
    return base + ext
}

private fun parseFilenameFromContentDisposition(cd: String?): String? {
    if (cd.isNullOrBlank()) return null
    val star =
        Pattern.compile("filename\\*=(?:UTF-8'')?([^;]+)", Pattern.CASE_INSENSITIVE).matcher(cd)
    if (star.find()) {
        val v = star.group(1)?.trim()?.trim('"', '\'')
        return try {
            URLDecoder.decode(v, "UTF-8")
        } catch (_: Throwable) {
            v
        }
    }
    val plain = Pattern.compile("filename=\"?([^\";]+)\"?", Pattern.CASE_INSENSITIVE).matcher(cd)
    if (plain.find()) return plain.group(1)?.trim()
    return null
}

private fun parseTotalFromContentRange(cr: String?): Long? {
    if (cr.isNullOrBlank()) return null
    val p = Pattern.compile("bytes\\s+(\\d+)-(\\d+)/(\\d+|\\*)", Pattern.CASE_INSENSITIVE)
    val m = p.matcher(cr)
    return if (m.find()) m.group(3)?.takeIf { it != "*" }?.toLongOrNull() else null
}

private fun findExistingInDownloads(ctx: Context, displayName: String?): ExistingDownload? {
    if (displayName.isNullOrBlank()) return null
    return if (Build.VERSION.SDK_INT >= 29) {
        val proj = arrayOf(
            MediaStore.Downloads._ID,
            MediaStore.Downloads.DISPLAY_NAME,
            MediaStore.Downloads.SIZE
        )
        val base = MediaStore.Downloads.EXTERNAL_CONTENT_URI
        ctx.contentResolver.query(
            base, proj, "${MediaStore.Downloads.DISPLAY_NAME}=?",
            arrayOf(displayName), null
        )?.use { c ->
            if (c.moveToFirst()) {
                val id = c.getLong(0)
                ExistingDownload(
                    Uri.withAppendedPath(base, id.toString()),
                    c.getString(1),
                    c.getLong(2)
                )
            } else null
        }
    } else {
        val f = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            displayName
        )
        if (f.exists()) ExistingDownload(Uri.EMPTY, displayName, f.length()) else null
    }
}

private fun openDownloadsOutput(
    ctx: Context,
    displayName: String,
    mime: String,
    existInfo: ResumeInfo,
    wantAppend: Boolean
): OutputSink? {
    return if (Build.VERSION.SDK_INT >= 29) {
        val resolver = ctx.contentResolver
        if (existInfo.uri != null) {
            val stream = resolver.openOutputStream(existInfo.uri, if (wantAppend) "wa" else "w")
                ?: return null
            OutputSink.MediaStoreStream(stream, ctx, existInfo.uri, newlyCreated = false)
        } else {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, displayName)
                if (mime.isNotBlank()) put(MediaStore.Downloads.MIME_TYPE, mime)
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val uri =
                resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return null
            val stream = resolver.openOutputStream(uri, "w") ?: return null
            OutputSink.MediaStoreStream(stream, ctx, uri, newlyCreated = true)
        }
    } else {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, displayName)
        val fos = FileOutputStream(file, wantAppend && file.exists())
        OutputSink.FileStream(fos, file = file, append = wantAppend && file.exists())
    }
}

private fun finalizePendingDownload(ctx: Context, uri: Uri) {
    if (Build.VERSION.SDK_INT >= 29) {
        val v = ContentValues().apply { put(MediaStore.Downloads.IS_PENDING, 0) }
        ctx.contentResolver.update(uri, v, null, null)
    }
}