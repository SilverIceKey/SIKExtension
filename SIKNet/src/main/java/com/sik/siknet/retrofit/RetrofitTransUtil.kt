package com.sik.siknet.retrofit

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

/**
 * retrofit转换工具类
 */
class RetrofitTransUtil {

    companion object {
        /**
         * 文件转part
         */
        fun file2Part(file: File): MultipartBody.Part? {
            return file2Part("file", file)
        }

        /**
         * 文件转part
         */
        fun file2Part(name: String, file: File): MultipartBody.Part? {
            val bodySingle: RequestBody = RequestBody.create(MultipartBody.FORM, file)
            return MultipartBody.Part.createFormData(
                name, file.name, bodySingle
            )
        }

        /**
         * 字符串转请求体
         */
        fun string2RequestBody(param: String): RequestBody? {
            return RequestBody.create("text/plain".toMediaTypeOrNull(), param)
        }

        /**
         * map转请求体
         */
        fun map2RequestBody(params: Map<String, Any?>?): Map<String, RequestBody?>? {
            val map: MutableMap<String, RequestBody?> = HashMap()
            if (!params.isNullOrEmpty()) {
                for ((mapKey, mapValue) in params) {
                    if (mapValue != null) {
                        if (mapValue is String) {
                            map[mapKey] = string2RequestBody(mapValue.toString())
                        }
                    }
                }
            }
            return map
        }

        /**
         * map转part
         */
        fun map2Multipart(params: Map<String, String>?): List<MultipartBody.Part?>? {
            val parts: MutableList<MultipartBody.Part?> = ArrayList()
            if (!params.isNullOrEmpty()) {
                for ((mapKey, mapValue) in params) {
                    if (mapValue.isNotEmpty()) {
                        val file = File(mapValue)
                        if (file.exists()) {
                            parts.add(file2Part(mapKey, file))
                        } else {
                            return null
                        }
                    }
                }
            }
            return parts
        }
    }
}