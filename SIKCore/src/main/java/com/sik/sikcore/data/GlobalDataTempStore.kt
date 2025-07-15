package com.sik.sikcore.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class GlobalDataTempStore private constructor() {

    private val gson = Gson()

    companion object {
        private val INSTANCE by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            GlobalDataTempStore()
        }

        @JvmStatic
        fun getInstance(): GlobalDataTempStore {
            return INSTANCE
        }
    }

    external fun nativeSaveData(key: String, jsonValue: String): Boolean
    external fun nativeGetData(key: String, isDeleteAfterGet: Boolean): String?
    external fun nativeHasData(key: String): Boolean
    external fun nativeClearData(key: String): Boolean
    external fun nativeClearAll()

    fun saveData(key: String, value: Any?): Boolean {
        return value?.let {
            val json = gson.toJson(it)
            nativeSaveData(key, json)
        } == true
    }

    internal inline fun <reified T> getData(key: String, isDeleteAfterGet: Boolean = true): T? {
        val json = nativeGetData(key, isDeleteAfterGet)
        return json?.let {
            gson.fromJson(it, object : TypeToken<T>() {}.type)
        }
    }

    fun hasData(key: String): Boolean = nativeHasData(key)
    fun clearData(key: String): Boolean = nativeClearData(key)
    fun clearAll() = nativeClearAll()
}
