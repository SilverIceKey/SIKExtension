package com.sik.siknet.http

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
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
import java.net.URLEncoder
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

// =====================================================
// 统一：错误处理 + 解析
// =====================================================

inline fun <reified T> emptyFallback(): T {
    return try {
        globalGson.fromJson("{}", object : TypeToken<T>() {}.type)
    } catch (_: Exception) {
        globalGson.fromJson("[]", object : TypeToken<T>() {}.type)
    }
}

fun buildHttpErrorMessage(code: Int, raw: String): String {
    return if (raw.isBlank()) "HTTP $code" else "HTTP $code, body=$raw"
}

/**
 * 解析并处理
 */
inline fun <reified T> parseResponseOrHandle(
    request: Request,
    response: Response
): T {
    val raw = response.body?.string().orEmpty()

    // 非2xx：不解析为 T，走全局兜底
    if (!response.isSuccessful) {
        val netEx = NetException(request, buildHttpErrorMessage(response.code, raw), null)
        val handled = HttpUtils.globalNetExceptionHandler(request, netEx)
        if (handled) return emptyFallback()
        throw netEx
    }

    // 2xx 但空 body：兜底给空对象/空数组（保持你原语义）
    if (raw.isBlank()) return emptyFallback()

    // Content-Type 非 json 且 body 也不像 json：不强行解析（避免 HTML/text 崩）
    val ct = response.header("Content-Type").orEmpty()
    val looksLikeJson =
        ct.contains("json", ignoreCase = true) ||
                raw.trimStart().startsWith("{") ||
                raw.trimStart().startsWith("[")

    if (!looksLikeJson) {
        val netEx = NetException(
            request,
            "Non-JSON response. Content-Type=$ct, body=$raw",
            null
        )
        val handled = HttpUtils.globalNetExceptionHandler(request, netEx)
        if (handled) return emptyFallback()
        throw netEx
    }

    return globalGson.fromJson(raw, object : TypeToken<T>() {}.type)
}

fun encodeQuery(params: Map<String, Any?>): String {
    return params.entries.joinToString("&") { (k, v) ->
        val ek = URLEncoder.encode(k, "UTF-8")
        val ev = URLEncoder.encode(v?.toString().orEmpty(), "UTF-8")
        "$ek=$ev"
    }
}

fun encodeQueryString(params: Map<String, String>): String {
    return params.entries.joinToString("&") { (k, v) ->
        val ek = URLEncoder.encode(k, "UTF-8")
        val ev = URLEncoder.encode(v, "UTF-8")
        "$ek=$ev"
    }
}

// =====================================================
// 同步请求
// =====================================================

inline fun <reified T> String.httpGet(params: Map<String, Any?> = emptyMap()): T {
    if (HttpUtils.isLoggerInRequest) {
        Log.i("HttpExtension", this)
        Log.i("HttpExtension", params.toJson())
    }

    val urlWithParams = if (params.isEmpty()) {
        this
    } else {
        val qs = encodeQuery(params)
        buildString {
            append(this@httpGet)
            append(if (this@httpGet.contains("?")) "&" else "?")
            append(qs)
        }
    }

    val request = Request.Builder().url(urlWithParams).get().build()

    return try {
        HttpUtils.createOkHttpClient().newCall(request).execute().use { resp ->
            parseResponseOrHandle(request, resp)
        }
    } catch (e: Exception) {
        val netEx = if (e is NetException) e else NetException(request, e.message, e)
        val handled = HttpUtils.globalNetExceptionHandler(request, netEx)
        if (handled) emptyFallback() else throw netEx
    }
}

inline fun <reified T> String.httpPostForm(formParameters: Any?): T {
    if (HttpUtils.isLoggerInRequest) {
        Log.i("HttpExtension", this)
        Log.i("HttpExtension", formParameters.toJson())
    }

    val formBodyBuilder = FormBody.Builder()
    formParameters?.let {
        when (it) {
            is Map<*, *> -> it.forEach { (k, v) -> formBodyBuilder.add(k.toString(), v.toString()) }
            else -> it.toMap().forEach { (k, v) -> formBodyBuilder.add(k, v) }
        }
    }

    val request = Request.Builder().url(this).post(formBodyBuilder.build()).build()

    return try {
        HttpUtils.createOkHttpClient().newCall(request).execute().use { resp ->
            parseResponseOrHandle(request, resp)
        }
    } catch (e: Exception) {
        val netEx = if (e is NetException) e else NetException(request, e.message, e)
        val handled = HttpUtils.globalNetExceptionHandler(request, netEx)
        if (handled) emptyFallback() else throw netEx
    }
}

inline fun <reified T> String.httpPostJson(data: Any? = null): T {
    val json = data as? String ?: if (data == null) "{}" else globalGson.toJson(data)

    if (HttpUtils.isLoggerInRequest) {
        Log.i("HttpExtension", this)
        Log.i("HttpExtension", json)
    }

    val mediaType = "application/json; charset=utf-8".toMediaType()
    val requestBody: RequestBody = json.toRequestBody(mediaType)
    val request = Request.Builder().url(this).post(requestBody).build()

    return try {
        HttpUtils.createOkHttpClient().newCall(request).execute().use { resp ->
            parseResponseOrHandle(request, resp)
        }
    } catch (e: Exception) {
        val netEx = if (e is NetException) e else NetException(request, e.message, e)
        val handled = HttpUtils.globalNetExceptionHandler(request, netEx)
        if (handled) emptyFallback() else throw netEx
    }
}

inline fun <reified T> String.httpUploadFile(
    fileParameterName: String,
    file: File,
    params: Map<String, String>
): T {
    if (HttpUtils.isLoggerInRequest) {
        Log.i("HttpExtension", this)
        Log.i("HttpExtension", params.toJson())
    }

    val fileBody = file.asRequestBody("application/octet-stream".toMediaType())
    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart(fileParameterName, file.name, fileBody)
        .apply { params.forEach { (k, v) -> addFormDataPart(k, v) } }
        .build()

    val request = Request.Builder().url(this).post(requestBody).build()

    return try {
        HttpUtils.createOkHttpClient().newCall(request).execute().use { resp ->
            parseResponseOrHandle(request, resp)
        }
    } catch (e: Exception) {
        val netEx = if (e is NetException) e else NetException(request, e.message, e)
        val handled = HttpUtils.globalNetExceptionHandler(request, netEx)
        if (handled) emptyFallback() else throw netEx
    }
}

// =====================================================
// 同步下载（保持你原逻辑，只加失败时走 handler）
// =====================================================

fun String.httpDownloadFile(
    methodStr: String = "GET",
    headers: Map<String, String> = mapOf(),
    data: Any? = null,
    destinationFile: File,
    progressListener: ProgressListener
): Boolean {
    val json = data as? String ?: globalGson.toJson(data)
    if (HttpUtils.isLoggerInRequest) {
        Log.i("HttpExtension", this)
        Log.i("HttpExtension", json)
    }

    val mediaType = "application/json; charset=utf-8".toMediaType()
    val requestBody: RequestBody = json.toRequestBody(mediaType)

    val request = Request.Builder().apply {
        url(this@httpDownloadFile)
        headers.forEach { (k, v) -> addHeader(k, v) }

        if (methodStr.equals("GET", ignoreCase = true)) {
            if (data is Map<*, *>) {
                val urlWithParams = StringBuilder().apply {
                    append(this@httpDownloadFile)
                    if (data.isNotEmpty()) {
                        append(if (this@httpDownloadFile.contains("?")) "&" else "?")
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
        }.build().newCall(request).execute()

        response.use { resp ->
            val ct = resp.header("Content-Type").orEmpty()
            if (!resp.isSuccessful || ct.contains("text/plain")) {
                val raw = runCatching { resp.body?.string().orEmpty() }.getOrDefault("")
                HttpUtils.globalNetExceptionHandler(
                    request,
                    NetException(request, buildHttpErrorMessage(resp.code, raw), null)
                )
                return false
            }
            true
        }
    } catch (e: IOException) {
        val netEx = NetException(request, e.message, e)
        HttpUtils.globalNetExceptionHandler(request, netEx)
        false
    }
}

// =====================================================
// 反射 toMap
// =====================================================

fun <T : Any> T.toMap(): Map<String, String> {
    return this::class.memberProperties.associate { p ->
        @Suppress("UNCHECKED_CAST")
        val k = p as KProperty1<T, *>
        val v = k.get(this)
        p.name to (v?.toString() ?: "")
    }
}

// =====================================================
// 异步（协程）
// =====================================================

suspend inline fun <reified T> String.httpGetAsync(
    params: Map<String, String> = emptyMap()
): T = suspendCancellableCoroutine { cont ->
    if (HttpUtils.isLoggerInRequest) {
        Log.i("HttpExtension", this)
        Log.i("HttpExtension", params.toJson())
    }

    val urlWithParams = if (params.isEmpty()) {
        this
    } else {
        val qs = encodeQueryString(params)
        buildString {
            append(this@httpGetAsync)
            append(if (this@httpGetAsync.contains("?")) "&" else "?")
            append(qs)
        }
    }

    val request = Request.Builder().url(urlWithParams).get().build()
    val call = HttpUtils.createOkHttpClient().newCall(request)

    cont.invokeOnCancellation { call.cancel() }

    call.enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            val netEx = NetException(request, e.message, e)
            val handled = HttpUtils.globalNetExceptionHandler(request, netEx)
            if (handled) cont.resume(emptyFallback()) else cont.resumeWithException(netEx)
        }

        override fun onResponse(call: Call, response: Response) {
            response.use { resp ->
                try {
                    cont.resume(parseResponseOrHandle(request, resp))
                } catch (e: Throwable) {
                    cont.resumeWithException(e)
                }
            }
        }
    })
}

suspend inline fun <reified T> String.httpPostJsonAsync(data: Any? = null): T =
    suspendCancellableCoroutine { cont ->
        val json = data as? String ?: if (data == null) "{}" else globalGson.toJson(data)

        if (HttpUtils.isLoggerInRequest) {
            Log.i("HttpExtension", this)
            Log.i("HttpExtension", json)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = json.toRequestBody(mediaType)
        val request = Request.Builder().url(this).post(requestBody).build()

        val call = HttpUtils.createOkHttpClient().newCall(request)
        cont.invokeOnCancellation { call.cancel() }

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val netEx = NetException(request, e.message, e)
                val handled = HttpUtils.globalNetExceptionHandler(request, netEx)
                if (handled) cont.resume(emptyFallback()) else cont.resumeWithException(netEx)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { resp ->
                    try {
                        cont.resume(parseResponseOrHandle(request, resp))
                    } catch (e: Throwable) {
                        cont.resumeWithException(e)
                    }
                }
            }
        })
    }

// =====================================================
// suspend 下载（你原版本基本OK，补：失败走 handler）
// =====================================================

suspend fun String.httpDownloadFile(
    methodStr: String = "GET",
    headers: Map<String, String> = emptyMap(),
    data: Any? = null,
    destinationFile: File? = null,
    progressListener: ProgressListener = ProgressListener { _, _, _ -> },
    context: Context = SIKCore.getApplication()
): File? = withContext(kotlinx.coroutines.Dispatchers.IO) {
    val isGet = methodStr.equals("GET", ignoreCase = true)

    val finalUrl = if (isGet && data is Map<*, *>) {
        val params = data.entries.joinToString("&") { e ->
            val k = URLEncoder.encode(e.key?.toString() ?: "", "UTF-8")
            val v = URLEncoder.encode(e.value?.toString() ?: "", "UTF-8")
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
        json.toRequestBody("application/json; charset=utf-8".toMediaType())
    } else null

    val target: DiskTarget = when {
        destinationFile != null -> DiskTarget.FilePath(destinationFile)
        else -> DiskTarget.Downloads(context)
    }

    val guessedName = guessFileNameFromUrl(finalUrl.toString())

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

    val resp = try {
        HttpUtils.createOkHttpClientBuilder(5, TimeUnit.MINUTES).build().newCall(req).execute()
    } catch (e: IOException) {
        HttpUtils.globalNetExceptionHandler(req, NetException(req, e.message, e))
        return@withContext null
    }

    resp.use { response ->
        val code = response.code
        if (!(code == 200 || code == 206)) {
            val raw = runCatching { response.body?.string().orEmpty() }.getOrDefault("")
            HttpUtils.globalNetExceptionHandler(
                req,
                NetException(req, buildHttpErrorMessage(code, raw), null)
            )
            return@withContext null
        }

        val body = response.body ?: return@withContext null
        val isResume = (existInfo.length > 0 && code == 206)

        val mime = response.header("Content-Type")?.substringBefore(';')?.trim().orEmpty()
        val nameFromHeader =
            parseFilenameFromContentDisposition(response.header("Content-Disposition"))
        val finalName = nameFromHeader ?: guessedName ?: fallbackName(finalUrl.toString(), mime)

        val contentLength = body.contentLength().takeIf { it > 0 } ?: -1L
        val totalExpected = when {
            isResume -> {
                parseTotalFromContentRange(response.header("Content-Range"))
                    ?: (if (contentLength > 0) existInfo.length + contentLength else -1L)
            }
            else -> contentLength
        }

        val out: OutputSink = when (target) {
            is DiskTarget.FilePath -> {
                target.file.parentFile?.mkdirs()
                OutputSink.FileStream(
                    FileOutputStream(target.file, /* append = */ isResume),
                    file = target.file,
                    append = isResume
                )
            }
            is DiskTarget.Downloads -> {
                openDownloadsOutput(
                    target.ctx,
                    finalName,
                    mime,
                    existInfo,
                    wantAppend = isResume
                ) ?: return@withContext null
            }
        }

        var written = if (isResume) existInfo.length else 0L
        val buf = ByteArray(DEFAULT_BUFFER_SIZE)

        try {
            body.byteStream().use { input ->
                out.stream.use { output ->
                    while (true) {
                        val n = input.read(buf)
                        if (n <= 0) break
                        output.write(buf, 0, n)
                        written += n

                        val reported =
                            if (totalExpected > 0) minOf(written, totalExpected) else written
                        progressListener.update(reported, totalExpected, false)
                    }
                    output.flush()
                }
            }

            if (out is OutputSink.MediaStoreStream && out.newlyCreated) {
                finalizePendingDownload(out.ctx, out.uri)
            }

            val finalReported = if (totalExpected > 0) totalExpected else written
            progressListener.update(finalReported, totalExpected, true)

            return@withContext when (out) {
                is OutputSink.FileStream -> out.file
                is OutputSink.MediaStoreStream -> File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    finalName
                )
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

/* ====== 辅助类型/方法（保持你原实现） ====== */

private sealed interface DiskTarget {
    data class FilePath(val file: File) : DiskTarget
    data class Downloads(val ctx: Context) : DiskTarget
}

private data class ResumeInfo(val length: Long, val displayName: String?, val uri: Uri?)
private data class ExistingDownload(val uri: Uri, val displayName: String, val size: Long)

private sealed interface OutputSink {
    val stream: OutputStream

    data class FileStream(
        override val stream: OutputStream,
        val file: File,
        val append: Boolean
    ) : OutputSink

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

    val star = Pattern.compile(
        "filename\\*=(?:UTF-8'')?([^;]+)",
        Pattern.CASE_INSENSITIVE
    ).matcher(cd)

    if (star.find()) {
        val v = star.group(1)?.trim()?.trim('"', '\'')
        return try {
            URLDecoder.decode(v, "UTF-8")
        } catch (_: Throwable) {
            v
        }
    }

    val plain = Pattern.compile(
        "filename=\"?([^\";]+)\"?",
        Pattern.CASE_INSENSITIVE
    ).matcher(cd)

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
            base,
            proj,
            "${MediaStore.Downloads.DISPLAY_NAME}=?",
            arrayOf(displayName),
            null
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
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return null
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
