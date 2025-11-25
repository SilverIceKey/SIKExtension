package com.sik.sikcore.data

import android.util.Log
import com.google.gson.reflect.TypeToken
import com.sik.sikcore.extension.globalGson
import java.lang.reflect.Type

class GlobalDataTempStore private constructor() {

    companion object {
        private val INSTANCE by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            GlobalDataTempStore()
        }

        @JvmStatic
        fun getInstance(): GlobalDataTempStore {
            return INSTANCE
        }

    }

    // 是否已加载 native 库标志位
    private var nativeLibLoaded = false

    init {
        safeLoadNativeLib()
    }

    /**
     * 安全加载 Native 库（支持延迟加载，防止启动阻塞）
     */
    private fun safeLoadNativeLib() {
        if (!nativeLibLoaded) {
            try {
                System.loadLibrary("SIKCore")
                nativeLibLoaded = true
                Log.i("GlobalDataTempStore", "Native 库加载成功")
            } catch (t: Throwable) {
                Log.e("GlobalDataTempStore", "Native 库加载失败:${t.toString()}")
            }
        }
    }

    external fun nativeSaveData(key: String, jsonValue: String): Boolean
    external fun nativeGetData(key: String, isDeleteAfterGet: Boolean): String?
    external fun nativeHasData(key: String): Boolean
    external fun nativeClearData(key: String): Boolean
    external fun nativeClearAll()

    fun saveData(key: String, value: Any?): Boolean {
        return value?.let {
            val json = globalGson.toJson(it)
            nativeSaveData(key, json)
        } == true
    }

    inline fun <reified T> getData(key: String, isDeleteAfterGet: Boolean = true): T? {
        val json = nativeGetData(key, isDeleteAfterGet)
        return json?.let {
            globalGson.fromJson<T>(it, object : TypeToken<T>() {}.type)
        }
    }

    fun <T> getData(key: String, type: Type, isDeleteAfterGet: Boolean = true): T? {
        val json = nativeGetData(key, isDeleteAfterGet)
        return json?.let {
            globalGson.fromJson<T>(it, type)
        }
    }


    fun hasData(key: String): Boolean = nativeHasData(key)
    fun clearData(key: String): Boolean = nativeClearData(key)
    fun clearAll() = nativeClearAll()
}
