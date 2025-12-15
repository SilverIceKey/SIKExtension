package com.sik.sikcore.extension

import com.tencent.mmkv.MMKV

/**
 * Save m m k v data
 * 指定mmkv保存数据
 * @param T
 * @param key
 * @param value
 */
inline fun <reified T> MMKV.saveMMKVData(key: String, value: T) {
    when (value) {
        is Boolean -> this.encode(key, value)
        is Float -> this.encode(key, value)
        is Int -> this.encode(key, value)
        is Long -> this.encode(key, value)
        is String -> this.encode(key, value)
        is ByteArray -> this.encode(key, value)
        is Set<*> -> this.encode(key, value as Set<String>)
        else -> throw IllegalArgumentException("Unsupported type")
    }
}

/**
 * Get m m k v data
 * 指定mmkv获取数据
 * @param T
 * @param key
 * @param defaultValue
 * @return
 */
inline fun <reified T> MMKV.getMMKVData(key: String, defaultValue: T): T {
    return when (defaultValue) {
        is Boolean -> this.decodeBool(key, defaultValue) as T
        is Float -> this.decodeFloat(key, defaultValue) as T
        is Int -> this.decodeInt(key, defaultValue) as T
        is Long -> this.decodeLong(key, defaultValue) as T
        is String -> this.decodeString(key, defaultValue) as T
        is ByteArray -> this.decodeBytes(key, defaultValue) as T
        is Set<*> -> this.decodeStringSet(key, defaultValue as Set<String>) as T
        else -> throw IllegalArgumentException("Unsupported type")
    }
}

/**
 * 保存数据
 */
inline fun <reified T> String.saveMMKVData(value: T) {
    MMKV.defaultMMKV().saveMMKVData(this, value)
}

/**
 * 获取数据
 */
inline fun <reified T> String.getMMKVData(defaultValue: T): T {
    return MMKV.defaultMMKV().getMMKVData(this, defaultValue)
}

/**
 * 移除数据
 */
fun String.removeMMKVData(){
    MMKV.defaultMMKV().removeValueForKey(this)
}
