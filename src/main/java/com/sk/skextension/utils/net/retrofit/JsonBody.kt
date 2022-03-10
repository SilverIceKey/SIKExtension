package com.sk.skextension.utils.net.retrofit

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.internal.checkOffsetAndCount
import okio.BufferedSink
import java.lang.NullPointerException
import java.nio.charset.Charset
import kotlin.text.Charsets.UTF_8

class JsonBody(private val content: String) : RequestBody() {
    companion object {
        val JSON: MediaType = "application/json; charset=UTF-8".toMediaType()
        private val charset: Charset = UTF_8

        fun create(content:String):RequestBody{
            return JsonBody(content)
        }
    }

    fun getContent():String{
        return content
    }

    override fun contentType(): MediaType? {
        return JSON
    }

    override fun writeTo(sink: BufferedSink) {
        val bytes: ByteArray = content.toByteArray(charset) ?: throw NullPointerException("content == null")
        checkOffsetAndCount(bytes.size.toLong(), 0, bytes.size.toLong())
        sink.write(bytes, 0, bytes.size)
    }
}