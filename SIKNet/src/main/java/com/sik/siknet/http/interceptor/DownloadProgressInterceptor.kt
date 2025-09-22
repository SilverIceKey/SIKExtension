package com.sik.siknet.http.interceptor

import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.buffer
import okio.sink
import java.io.File
import java.io.IOException

// 定义一个接口用于进度更新的回调
fun interface ProgressListener {
    fun update(bytesRead: Long, contentLength: Long, done: Boolean)
}

class ProgressInterceptor(
    private val progressListener: ProgressListener,
    private val destinationFile: File
) : Interceptor {
    @Volatile private var totalBytesRead = 0L

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse = chain.proceed(chain.request())

        if (originalResponse.isSuccessful && originalResponse.header("Content-Type")?.contains("text/plain") != true) {
            val source = originalResponse.body?.source() ?: return originalResponse
            destinationFile.parentFile?.mkdirs()
            val fileSink = destinationFile.sink().buffer()
            val buffer = Buffer()
            val contentLength = originalResponse.body?.contentLength() ?: 0L

            var bytesRead = 0L
            try {
                while (source.read(buffer, 8192).apply { bytesRead = this }.toInt() != -1) {
                    fileSink.write(buffer, bytesRead)
                    totalBytesRead += bytesRead
                    progressListener.update(totalBytesRead, contentLength, false)
                }
                fileSink.flush()
                progressListener.update(totalBytesRead, contentLength, true)
            } finally {
                fileSink.close()
            }
            return originalResponse.newBuilder()
                .body(EmptyResponseBody(originalResponse.body?.contentType(), 0L))
                .build()
        }
        return originalResponse
    }
}

internal class EmptyResponseBody(
    private val contentType: MediaType?,
    private val contentLength: Long
) : ResponseBody() {
    override fun contentType(): MediaType? = contentType
    override fun contentLength(): Long = contentLength
    override fun source(): BufferedSource = Buffer().apply { close() }.buffer()
}
